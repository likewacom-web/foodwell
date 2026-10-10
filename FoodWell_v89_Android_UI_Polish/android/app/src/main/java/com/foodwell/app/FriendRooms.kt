package com.foodwell.app

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

/**
 * Friend challenges: a room per invite code that friends join to race the same challenge.
 *
 * Layout: rooms/{code} = {cid, title, start, days, need, createdBy, createdAt}
 *         rooms/{code}/members/{uid} = {name, e, passed, need, state, last, updatedAt}
 * Members only hold a display name and challenge progress — never food or health records.
 * Codes this phone joined are remembered so deleting the account can remove its member entries.
 */
class FriendRooms(private val context: Context, private val emit: (fn: String, arg: String) -> Unit) {
    private val prefs = context.getSharedPreferences("foodwell_rooms", Context.MODE_PRIVATE)
    private val db get() = FirebaseFirestore.getInstance()
    private val auth get() = FirebaseAuth.getInstance()
    private val watchers = mutableMapOf<String, ListenerRegistration>()

    private fun room(code: String) = db.collection("rooms").document(code)
    private fun uid() = auth.currentUser?.uid ?: throw IllegalStateException(Lang.t(context, "เข้าสู่ระบบด้วย Google ก่อน (ตั้งค่า → ข้อมูลและซิงก์)", "Sign in with Google first (Settings → Data & sync)"))
    private fun joined() = prefs.getStringSet("codes", emptySet())!!.toMutableSet()
    private fun remember(code: String, on: Boolean) =
        prefs.edit().putStringSet("codes", joined().apply { if (on) add(code) else remove(code) }).apply()

    /** Flat JSON object -> Firestore map (strings, numbers, booleans only). */
    private fun flat(json: String): MutableMap<String, Any> {
        val o = JSONObject(json)
        val m = mutableMapOf<String, Any>()
        for (k in o.keys()) when (val v = o.get(k)) {
            is String -> m[k] = v.take(80)
            is Number, is Boolean -> m[k] = v
        }
        return m
    }

    /** Creates a room for a challenge and returns its new invite code. */
    suspend fun create(roomJson: String, memberJson: String): String {
        val uid = uid()
        repeat(6) {
            val code = (1..6).map { ALPHABET.random() }.joinToString("")
            if (!room(code).get().await().exists()) {
                room(code).set(flat(roomJson) + mapOf("createdBy" to uid, "createdAt" to System.currentTimeMillis())).await()
                update(code, memberJson)
                return code
            }
        }
        throw IllegalStateException(Lang.t(context, "สร้างห้องไม่สำเร็จ ลองอีกครั้ง", "Couldn't create the room, try again"))
    }

    /** Joins with a code; returns the room (challenge, start date …) as JSON. */
    suspend fun join(code: String, memberJson: String): String {
        uid()
        val snap = room(code).get().await()
        if (!snap.exists()) throw IllegalStateException(Lang.t(context, "ไม่พบรหัสห้องนี้ · ตรวจรหัสอีกครั้ง", "No room with this code · check the code"))
        update(code, memberJson)
        return JSONObject(snap.data ?: emptyMap<String, Any>()).put("code", code).toString()
    }

    suspend fun update(code: String, memberJson: String) {
        val uid = uid()
        room(code).collection("members").document(uid)
            .set(flat(memberJson) + mapOf("updatedAt" to System.currentTimeMillis())).await()
        remember(code, true)
    }

    /** Streams the room's members as onRoomData(code, [{uid, me, name, e, passed, …}]). */
    fun watch(code: String) {
        val me = auth.currentUser?.uid ?: return
        watchers.remove(code)?.remove()
        watchers[code] = room(code).collection("members").addSnapshotListener { snap, err ->
            if (err != null) {
                emit("onRoomError", JSONObject.quote(code) + "," + JSONObject.quote(err.message ?: ""))
                return@addSnapshotListener
            }
            val arr = JSONArray()
            snap?.documents?.forEach { d -> arr.put(JSONObject(d.data ?: emptyMap<String, Any>()).put("uid", d.id).put("me", d.id == me)) }
            emit("onRoomData", JSONObject.quote(code) + "," + JSONObject.quote(arr.toString()))
        }
    }

    fun unwatch(code: String) { watchers.remove(code)?.remove() }

    fun stopAll() { watchers.values.forEach { it.remove() }; watchers.clear() }

    suspend fun leave(code: String) {
        unwatch(code)
        auth.currentUser?.uid?.let { room(code).collection("members").document(it).delete().await() }
        remember(code, false)
    }

    /** Before the account is deleted: remove this user from every room it joined. */
    suspend fun leaveAll() {
        stopAll()
        for (code in joined()) runCatching { leave(code) }
    }

    companion object {
        private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
