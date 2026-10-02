package com.foodwell.app

import androidx.activity.ComponentActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

/**
 * Gmail sign-in (Firebase Auth) + key/value sync of the page's localStorage through Firestore.
 *
 * Layout: users/{uid}/kv/{key}[~n] — one document per chunk of a storage value, written in a
 * single batch so readers see all parts with the same updatedAt. The page decides what is newer
 * (it owns the per-key timestamps); this class only transports values.
 *
 * Works only when the build includes google-services.json; otherwise [configured] is false.
 */
class CloudSync(private val activity: ComponentActivity, private val emit: (fn: String, arg: String) -> Unit) {
    private val app: FirebaseApp? = try {
        FirebaseApp.getApps(activity).firstOrNull() ?: FirebaseApp.initializeApp(activity)
    } catch (e: Exception) {
        null
    }
    val configured get() = app != null && webClientId != null

    /** Why sign-in can't work yet, in Thai for the page; null when ready. */
    val notReadyReason: String?
        get() = when {
            app == null -> NOT_CONFIGURED
            // google-services.json only carries the web OAuth client after Google sign-in is
            // enabled in Firebase Authentication (and SHA-1 added); without it there's no ID token.
            webClientId == null -> NO_WEB_CLIENT
            else -> null
        }

    private val webClientId: String? by lazy {
        val id = activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)
        if (id == 0) null else activity.getString(id)
    }
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null
    private val partsSeen = mutableMapOf<String, Int>()

    fun userJson(): JSONObject? {
        if (!configured) return null
        val u = auth.currentUser ?: return null
        return JSONObject().put("uid", u.uid).put("email", u.email ?: "")
            .put("name", u.displayName ?: "").put("photo", u.photoUrl?.toString() ?: "")
    }

    suspend fun signIn(): JSONObject {
        val clientId = webClientId ?: throw IllegalStateException(notReadyReason ?: NO_WEB_CLIENT)
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
            .build()
        val cred = try {
            CredentialManager.create(activity).getCredential(activity, request).credential
        } catch (e: GetCredentialCancellationException) {
            throw IllegalStateException("ยกเลิกการเข้าสู่ระบบ")
        } catch (e: NoCredentialException) {
            throw IllegalStateException("ไม่พบบัญชี Google ในเครื่อง · เพิ่มบัญชีในการตั้งค่ามือถือก่อน")
        } catch (e: GetCredentialException) {
            val m = e.message ?: ""
            throw IllegalStateException(
                if ("28444" in m || "Developer console" in m || "10:" in m)
                    "ตั้งค่า Firebase ยังไม่ครบ (SHA-1 / เปิด Google sign-in) · ดู FIREBASE_SYNC.md"
                else "เข้าสู่ระบบไม่สำเร็จ: $m"
            )
        }
        if (cred !is CustomCredential || cred.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            throw IllegalStateException("ได้ข้อมูลบัญชีที่ไม่รองรับ")
        }
        val idToken = GoogleIdTokenCredential.createFrom(cred.data).idToken
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
        return userJson() ?: throw IllegalStateException("เข้าสู่ระบบไม่สำเร็จ")
    }

    suspend fun signOut() {
        stop()
        if (!configured) return
        auth.signOut()
        runCatching { CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest()) }
    }

    private fun kv(uid: String) = db.collection("users").document(uid).collection("kv")

    /** Streams remote values to the page as onCloudSnapshot([{key,value,updatedAt,device}]). */
    fun start() {
        if (!configured) return
        val uid = auth.currentUser?.uid ?: return
        stop()
        listener = kv(uid).addSnapshotListener { snap, err ->
            if (err != null) {
                emit("onCloudError", JSONObject.quote(errorText(err)))
                return@addSnapshotListener
            }
            if (snap == null || snap.metadata.hasPendingWrites()) return@addSnapshotListener
            val byId = snap.documents.associateBy { it.id }
            val changedKeys = snap.documentChanges.mapNotNull { it.document.getString("key") }.toSet()
            val items = JSONArray()
            for (key in changedKeys) {
                val head = byId[key] ?: continue
                val parts = (head.getLong("parts") ?: 1L).toInt()
                val updatedAt = head.getLong("updatedAt") ?: 0L
                partsSeen[key] = parts
                val sb = StringBuilder(head.getString("data") ?: "")
                var complete = true
                for (i in 1 until parts) {
                    val d = byId["$key~$i"]
                    if (d == null || d.getLong("updatedAt") != updatedAt) { complete = false; break }
                    sb.append(d.getString("data") ?: "")
                }
                if (!complete) continue
                items.put(
                    JSONObject().put("key", key).put("value", sb.toString())
                        .put("updatedAt", updatedAt).put("device", head.getString("device") ?: "")
                )
            }
            emit("onCloudSnapshot", JSONObject.quote(items.toString()))
        }
    }

    fun stop() {
        listener?.remove()
        listener = null
    }

    /** Writes one storage value (split into <1 MB documents) atomically. */
    suspend fun push(key: String, value: String, updatedAt: Long, device: String) {
        if (!configured) return
        val uid = auth.currentUser?.uid ?: return
        val chunks = value.chunked(CHUNK).ifEmpty { listOf("") }
        val col = kv(uid)
        val batch = db.batch()
        chunks.forEachIndexed { i, c ->
            batch.set(
                col.document(if (i == 0) key else "$key~$i"),
                mapOf("key" to key, "part" to i, "parts" to chunks.size, "updatedAt" to updatedAt, "device" to device, "data" to c)
            )
        }
        for (i in chunks.size until (partsSeen[key] ?: 0)) batch.delete(col.document("$key~$i"))
        batch.commit().await()
        partsSeen[key] = chunks.size
    }

    fun errorText(e: Exception): String {
        val m = e.message ?: ""
        return when {
            "PERMISSION_DENIED" in m -> "Firestore ปฏิเสธการเข้าถึง · ตรวจกฎ (Rules) ตาม FIREBASE_SYNC.md"
            "NOT_FOUND" in m || "database" in m && "does not exist" in m -> "ยังไม่ได้สร้าง Firestore Database"
            "UNAVAILABLE" in m -> "ไม่มีอินเทอร์เน็ต · จะซิงก์เมื่อออนไลน์"
            else -> "ซิงก์ไม่สำเร็จ: $m"
        }
    }

    companion object {
        // Characters per document part: Thai text is up to 3 bytes/char in UTF-8, keep well under 1 MiB.
        private const val CHUNK = 250_000
        const val NOT_CONFIGURED = "แอปนี้ยังไม่ได้เชื่อม Firebase (ต้องมีไฟล์ google-services.json) · ดู FIREBASE_SYNC.md"
        const val NO_WEB_CLIENT = "Firebase ยังไม่ได้เปิด Google sign-in · เปิดที่ Authentication → Sign-in method → Google และใส่ SHA-1 แล้วดาวน์โหลด google-services.json ใหม่"
    }
}
