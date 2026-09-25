const DAYS=[["Lun","Lunedì"],["Mar","Martedì"],["Mer","Mercoledì"],["Gio","Giovedì"],["Ven","Venerdì"],["Sab","Sabato"],["Dom","Domenica"]];
const COLORS=["#3B82F6","#6366F1","#8B5CF6","#EC4899","#10B981","#06B6D4","#F59E0B","#EF4444"];
const KEY="routine-pwa-v2";
let data=JSON.parse(localStorage.getItem(KEY)||"null");
let selected=(new Date().getDay()+6)%7, view="day", editingType="EVENT", selectedDays=new Set([selected]), selectedColor=COLORS[1];
const starter=[
["Meditazione & Risveglio","Respirazione e stretching leggero",0,"12:20","12:40","Salute & Relax","Media","COMPLETED",20,"#06B6D4"],
["AI & Automazioni","Studio LLM, agenti, n8n e workflow",0,"14:00","16:00","Studio","Alta","PENDING",null,"#6366F1"],
["Marketing & Business","Content marketing, offerte e crescita",0,"16:15","18:00","Lavoro","Alta","PENDING",null,"#3B82F6"],
["Allenamento / Calcio","Sessione tecnica o partita",1,"18:30","21:30","Sport & Fitness","Alta","PENDING",null,"#10B981"],
["AI & Automazioni","Studio e costruzione automazioni",2,"14:00","16:00","Studio","Alta","PENDING",null,"#6366F1"],
["Marketing & Business","Strategia, contenuti e clienti",2,"16:15","18:00","Lavoro","Alta","PENDING",null,"#3B82F6"],
["Allenamento / Calcio","Sessione tecnica o partita",3,"18:30","21:30","Sport & Fitness","Alta","PENDING",null,"#10B981"],
["AI & Automazioni","Progetto pratico",4,"14:00","16:00","Studio","Alta","PENDING",null,"#6366F1"],
["Allenamento / Calcio","Sessione tecnica o partita",4,"18:30","21:30","Sport & Fitness","Alta","PENDING",null,"#10B981"],
["Preparazione atletica","Lavoro tecnico e condizionale",5,"16:00","18:00","Sport & Fitness","Media","PENDING",null,"#10B981"],
["Partita","Partita / giornata calcio",6,"13:00","19:00","Sport & Fitness","Alta","PENDING",null,"#10B981"],
["Riflessione settimanale","Review, pianificazione e reset",6,"21:00","21:30","Personale","Media","PENDING",null,"#EC4899"]
];
if(!data){data=starter.map(s=>({id:crypto.randomUUID(),title:s[0],notes:s[1],type:"EVENT",days:[s[2]],start:s[3],end:s[4],category:s[5],priority:s[6],status:s[7],actual:s[8],color:s[9],reminder:false}));save()}
function save(){localStorage.setItem(KEY,JSON.stringify(data))}
const $=s=>document.querySelector(s), $$=s=>Array.from(document.querySelectorAll(s));
function mins(t){if(!t)return 15;const p=t.split(":").map(Number);return p[0]*60+p[1]}
function planned(x){return x.start&&x.end?Math.max(15,mins(x.end)-mins(x.start)):15}
function fmt(n){const h=Math.floor(n/60),m=n%60;return h?(h+"h"+(m?" "+m+"m":"")):m+"m"}
function doneM(x){return x.status==="COMPLETED"?(x.actual==null?planned(x):x.actual):x.status==="MISSED"?(x.actual||0):0}
function missedM(x){return x.status==="MISSED"?Math.max(0,planned(x)-doneM(x)):x.status==="COMPLETED"?Math.max(0,planned(x)-doneM(x)):0}
function esc(s){return String(s==null?"":s).replace(/[&<>"']/g,function(c){return {"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[c]})}
function itemsFor(d){return data.filter(x=>x.days.includes(d)).sort((a,b)=>mins(a.start)-mins(b.start))}
function renderDays(){const el=$("#dayStrip");el.innerHTML=DAYS.map(function(d,i){const a=itemsFor(i),n=a.filter(x=>x.status==="COMPLETED").length;return '<button class="day '+(i===selected?"active":"")+'" data-day="'+i+'"><b>'+d[0]+'</b><small>'+n+'/'+a.length+'</small></button>'}).join("");$$(".day").forEach(function(b){b.onclick=function(){selected=+b.dataset.day;renderDay()}})}
function renderDay(){renderDays();$("#selectedDayTitle").textContent=DAYS[selected][1];const items=itemsFor(selected),done=items.filter(x=>x.status==="COMPLETED").length,pct=items.length?Math.round(done/items.length*100):0;$("#daySummary").textContent=items.length+" attività · "+done+" completate";$("#dayPercent").textContent=pct+"%";$(".progress-ring").style.setProperty("--p",pct+"%");$("#timeline").innerHTML=items.length?items.map(function(x){return '<article class="item '+(x.status==="COMPLETED"?"done":"")+'" data-id="'+x.id+'"><div class="time">'+(x.start||"—")+'<br><span>'+(x.start&&x.end?fmt(planned(x)):"")+'</span></div><div class="stripe" style="background:'+x.color+'"></div><div><div class="item-title">'+esc(x.title)+'</div><div class="item-note">'+esc(x.notes||x.category)+'</div><div class="meta"><span class="pill">'+esc(x.category)+'</span><span class="pill priority-'+x.priority.toLowerCase()+'">'+esc(x.priority)+'</span>'+(x.reminder?'<span class="pill">🔔</span>':"")+'</div></div><button class="check '+(x.status==="COMPLETED"?"on":"")+'" data-check="'+x.id+'">'+(x.status==="COMPLETED"?"✓":x.status==="MISSED"?"!":"○")+'</button></article>'}).join(""):'<div class="empty">Nessuna attività per questo giorno.<br>Premi “＋ Aggiungi” per crearne una.</div>';$$("[data-check]").forEach(function(b){b.onclick=function(e){e.stopPropagation();cycleStatus(b.dataset.check)}});$$(".item").forEach(function(el){el.onclick=function(){openModal(data.find(function(x){return x.id===el.dataset.id}))}});renderStats()}
function cycleStatus(id){const x=data.find(function(a){return a.id===id});x.status=x.status==="PENDING"?"COMPLETED":x.status==="COMPLETED"?"MISSED":"PENDING";if(x.status==="COMPLETED"&&!x.actual)x.actual=planned(x);save();render()}
function renderWeek(){const grid=$("#weekGrid");grid.innerHTML=DAYS.map(function(d,i){const arr=itemsFor(i);return '<div class="week-col"><div class="week-col-head"><strong>'+d[1]+'</strong><span>'+arr.filter(function(x){return x.status==="COMPLETED"}).length+'/'+arr.length+'</span></div>'+(arr.map(function(x){return '<div class="week-item '+(x.status==="COMPLETED"?"done":"")+'" style="border-left-color:'+x.color+'" data-id="'+x.id+'"><b>'+esc(x.title)+'</b><small>'+(x.start||"—")+' · '+esc(x.category)+'</small></div>'}).join("")||'<div class="empty">—</div>')+'</div>'}).join("");$$(".week-item").forEach(function(el){el.onclick=function(){openModal(data.find(function(x){return x.id===el.dataset.id}))}})}
function renderStats(){const total=data.length,done=data.filter(function(x){return x.status==="COMPLETED"}).length,plannedT=data.reduce(function(s,x){return s+planned(x)},0),doneT=data.reduce(function(s,x){return s+doneM(x)},0),missedT=data.reduce(function(s,x){return s+missedM(x)},0),pct=total?Math.round(done/total*100):0;$("#doneCount").textContent=done+"/"+total;$("#doneTime").textContent=fmt(doneT);$("#plannedTime").textContent=fmt(plannedT);$("#missedTime").textContent=fmt(missedT);$("#weekPercent").textContent=pct+"%";$("#totalCount").textContent=total+" attività";$("#weekBar").style.width=pct+"%";const cats=Array.from(new Set(data.map(function(x){return x.category})));$("#categoryStats").innerHTML=cats.map(function(c){const a=data.filter(function(x){return x.category===c}),d=a.filter(function(x){return x.status==="COMPLETED"}).length,t=a.reduce(function(s,x){return s+planned(x)},0);return '<div class="cat-row"><div class="cat-main"><i class="dot" style="background:'+a[0].color+'"></i>'+esc(c)+'</div><div class="cat-values"><b>'+d+'/'+a.length+'</b>'+fmt(t)+'</div></div>'}).join("");$("#timeRows").innerHTML='<div class="time-row"><span>Pianificato</span><b>'+fmt(plannedT)+'</b></div><div class="time-row"><span>Fatto</span><b>'+fmt(doneT)+'</b></div><div class="time-row"><span>Non fatto / perso</span><b>'+fmt(missedT)+'</b></div>'}
function render(){renderDay();renderWeek();renderStats()}
function switchView(v){view=v;$$(".tab").forEach(function(t){t.classList.toggle("active",t.dataset.view===v)});$$(".view").forEach(function(x){x.classList.toggle("active",x.id==="view-"+v)});if(v==="week")renderWeek();if(v==="stats")renderStats()}
$$(".tab").forEach(function(t){t.onclick=function(){switchView(t.dataset.view)}})
$("#addTop").onclick=$("#addInline").onclick=$("#weekAdd").onclick=function(){openModal()};
function buildPickers(){$("#dayPicker").innerHTML=DAYS.map(function(d,i){return '<button type="button" class="day-pick '+(selectedDays.has(i)?"active":"")+'" data-pick="'+i+'">'+d[0]+'</button>'}).join("");$$(".day-pick").forEach(function(b){b.onclick=function(){const d=+b.dataset.pick;if(selectedDays.has(d)&&selectedDays.size>1)selectedDays.delete(d);else selectedDays.add(d);buildPickers()}});$("#colorPicker").innerHTML=COLORS.map(function(c){return '<button type="button" class="color '+(selectedColor===c?"active":"")+'" style="background:'+c+'" data-color="'+c+'"></button>'}).join("");$$(".color").forEach(function(b){b.onclick=function(){selectedColor=b.dataset.color;buildPickers()}})}
function setType(t){editingType=t;$$(".type").forEach(function(b){b.classList.toggle("active",b.dataset.type===t)});$("#timeFields").style.display=t==="EVENT"?"grid":"none"}
function openModal(item){$("#modal").classList.remove("hidden");$("#editId").value=item?item.id:"";$("#modalTitle").textContent=item?"Modifica attività":"Nuova attività";editingType=item&&item.type?item.type:"EVENT";$("#title").value=item?item.title:"";$("#notes").value=item?item.notes:"";selectedDays=new Set(item&&item.days?item.days:[selected]);$("#start").value=item&&item.start?item.start:"";$("#end").value=item&&item.end?item.end:"";$("#category").value=item?item.category:"Personale";$("#priority").value=item?item.priority:"Media";selectedColor=item&&item.color?item.color:COLORS[1];$("#actual").value=item&&item.actual!=null?item.actual:"";$("#reminder").checked=!!(item&&item.reminder);$("#status").value=item&&item.status?item.status:"PENDING";$("#deleteBtn").classList.toggle("hidden",!item);setType(editingType);buildPickers()}
$$(".type").forEach(function(b){b.onclick=function(){setType(b.dataset.type)}});$("#closeModal").onclick=function(){$("#modal").classList.add("hidden")};$("#modal").onclick=function(e){if(e.target.id==="modal")$("#modal").classList.add("hidden")};
$("#routineForm").onsubmit=function(e){e.preventDefault();const id=$("#editId").value,obj={id:id||crypto.randomUUID(),title:$("#title").value.trim(),notes:$("#notes").value.trim(),type:editingType,days:Array.from(selectedDays),start:$("#start").value||null,end:$("#end").value||null,category:$("#category").value,priority:$("#priority").value,color:selectedColor,actual:$("#actual").value===""?null:Number($("#actual").value),reminder:$("#reminder").checked,status:$("#status").value};if(id)data=data.map(function(x){return x.id===id?obj:x});else data.push(obj);save();$("#modal").classList.add("hidden");render()};
$("#deleteBtn").onclick=function(){const id=$("#editId").value;data=data.filter(function(x){return x.id!==id});save();$("#modal").classList.add("hidden");render()};
$$("[data-prompt]").forEach(function(b){b.onclick=function(){sendCoach(b.dataset.prompt)}});
$("#coachForm").onsubmit=function(e){e.preventDefault();const v=$("#coachText").value.trim();if(v)sendCoach(v);$("#coachText").value=""};
async function sendCoach(text){
  const box=$("#coachMessages");
  box.insertAdjacentHTML("beforeend",'<div class="bubble user">'+esc(text)+'</div>');
  const history=Array.from(box.querySelectorAll(".bubble")).slice(-11,-1).map(function(el){
    return {role:el.classList.contains("user")?"user":"model",text:el.textContent||""};
  });
  const pending=document.createElement("div");
  pending.className="bubble ai";
  pending.textContent="Sto analizzando la tua routine…";
  box.appendChild(pending);
  box.scrollTop=box.scrollHeight;
  try{
    const response=await fetch("https://aexmdxnmlqxkqkvcjazb.supabase.co/functions/v1/focus-coach",{
      method:"POST",
      headers:{
        "Content-Type":"application/json",
        "apikey":"sb_publishable_ct-z7YfE81qD1ecrWQST2A_N2eIB5dl"
      },
      body:JSON.stringify({message:text,history:history,routine:data})
    });
    const result=await response.json().catch(function(){return{}});
    if(!response.ok) throw new Error(result.error||"Errore nel collegamento a Gemini.");
    pending.textContent=result.reply||"Gemini non ha restituito una risposta.";
  }catch(error){
    pending.textContent="Non riesco a collegarmi a Gemini in questo momento. "+(error&&error.message?error.message:"Riprova tra poco.");
  }
  box.scrollTop=box.scrollHeight;
}
$("#exportBtn").onclick=function(){const rows=[["Titolo","Giorni","Inizio","Fine","Categoria","Priorità","Stato","Pianificato min","Fatto min","Non fatto min"]].concat(data.map(function(x){return [x.title,x.days.map(function(d){return DAYS[d][1]}).join(" / "),x.start||"",x.end||"",x.category,x.priority,x.status,planned(x),doneM(x),missedM(x)]}));const csv=rows.map(function(r){return r.map(function(v){return '"'+String(v).replace(/"/g,'""')+'"'}).join(",")}).join("\\n");const a=document.createElement("a");a.href=URL.createObjectURL(new Blob([csv],{type:"text/csv;charset=utf-8"}));a.download="routine-report.csv";a.click();URL.revokeObjectURL(a.href)};
render();if("serviceWorker"in navigator)navigator.serviceWorker.register("./sw.js");