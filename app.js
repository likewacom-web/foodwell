const $=id=>document.getElementById(id);
const key="foodwell_v1";
let state=JSON.parse(localStorage.getItem(key)||'{"foods":[],"water":0,"exercise":[]}');
const save=()=>{localStorage.setItem(key,JSON.stringify(state));render()};
const esc=s=>String(s??"").replace(/[&<>"']/g,m=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[m]));

document.querySelectorAll(".tab").forEach(b=>b.onclick=()=>{
 document.querySelectorAll(".tab").forEach(x=>x.classList.remove("active"));
 document.querySelectorAll(".page").forEach(x=>x.classList.remove("active"));
 b.classList.add("active");$(b.dataset.page).classList.add("active");
});

$("addFood").onclick=()=>{
 const name=$("foodName").value.trim(); if(!name)return alert("ใส่ชื่ออาหารก่อน");
 const f={id:Date.now(),name,meal:$("meal").value,kcal:$("kcal").value,protein:$("protein").value,carbs:$("carbs").value,fat:$("fat").value,sodium:$("sodium").value,fiber:$("fiber").value,sugar:$("sugar").value,note:$("note").value,time:new Date().toLocaleTimeString("th-TH",{hour:"2-digit",minute:"2-digit"})};
 const file=$("photo").files[0];
 if(file){const r=new FileReader();r.onload=()=>{f.photo=r.result;state.foods.unshift(f);save()};r.readAsDataURL(file)}
 else{state.foods.unshift(f);save()}
 ["foodName","kcal","protein","carbs","fat","sodium","fiber","sugar","note"].forEach(id=>$(id).value="");
 $("photo").value="";
};
$("addEx").onclick=()=>{
 const name=$("exName").value.trim(),min=$("exMin").value;
 if(!name||!min)return alert("ใส่กิจกรรมและเวลา");
 state.exercise.unshift({id:Date.now(),name,min,time:new Date().toLocaleTimeString("th-TH",{hour:"2-digit",minute:"2-digit"})});save();$("exName").value="";$("exMin").value="";
};
document.querySelectorAll("[data-water]").forEach(b=>b.onclick=()=>{state.water+=Number(b.dataset.water);save()});
$("waterReset").onclick=()=>{state.water=0;save()};
$("resetBtn").onclick=()=>{if(confirm("ล้างข้อมูล FoodWell ทั้งหมดในเครื่องนี้?")){localStorage.removeItem(key);state={foods:[],water:0,exercise:[]};render()}};

function render(){
 $("waterTotal").textContent=state.water.toLocaleString();
 $("foodList").innerHTML=state.foods.length?state.foods.map(f=>`<div class="item">${f.photo?`<img src="${f.photo}">`:"🍱"}<div class="meta"><b>${esc(f.name)}</b><div>${esc(f.meal)} · ${esc(f.time)} · ${f.kcal?esc(f.kcal)+" kcal":"ไม่ระบุ"}</div><small>P ${esc(f.protein||0)}g · C ${esc(f.carbs||0)}g · F ${esc(f.fat||0)}g · Na ${esc(f.sodium||0)}mg · Fiber ${esc(f.fiber||0)}g · Sugar ${esc(f.sugar||0)}g</small>${f.note?`<small>${esc(f.note)}</small>`:""}</div><button class="delete" onclick="delFood(${f.id})">ลบ</button></div>`).join(""):"<p>ยังไม่มีรายการอาหารวันนี้</p>";
 $("exList").innerHTML=state.exercise.length?state.exercise.map(e=>`<div class="item"><div class="meta"><b>${esc(e.name)}</b><div>${esc(e.min)} นาที · ${esc(e.time)}</div></div><button class="delete" onclick="delEx(${e.id})">ลบ</button></div>`).join(""):"<p>ยังไม่มีกิจกรรมวันนี้</p>";
}
window.delFood=id=>{state.foods=state.foods.filter(x=>x.id!==id);save()};
window.delEx=id=>{state.exercise=state.exercise.filter(x=>x.id!==id);save()};
render();