// MeowFit workout figure: a small vector character posed by joint angles and animated between keyframes.
// Used for every exercise that has no drawn/AI animation yet (see EXERCISES.md).
//
// Pose: x,y = hip position (y is re-grounded automatically unless the move anchors a joint),
// t = torso angle (0 upright, +90 lying with the head to the right, -90 head to the left),
// ra/la = near/far arm [upper-arm angle, elbow bend], rl/ll = near/far leg [thigh angle, knee bend],
// limb angles: 0 points down, +90 forward (right), 180 up, -90 back (left); f = extra foot angle, h = head tilt,
// b = spine bow (+ rounds the back), air = lift off the floor. Front-view moves mirror the near limbs to the left.
(function(){
 const W=200,FLOOR=186,TL=58,UA=29,FA=27,TH=38,SH=38,FT=11;
 const C={skin:'#F2C29B',skin2:'#D9A47E',shirt:'#E887AA',shirt2:'#C96A8E',shorts:'#2C3A6B',shorts2:'#1F2A50',hair:'#2B2B3A',line:'#2A2230',shoe:'#FFFFFF',prop:'#8C93A8',prop2:'#5B6378',pad:'#3E4660'};
 const D=Math.PI/180,rad=a=>a*D;
 const dir=a=>[Math.sin(rad(a)),Math.cos(rad(a))]; // limb direction for angle a (0 = down)
 const ST={x:100,y:110,t:0,h:0,b:0,f:0,air:0,ra:[4,14],la:[-4,14],rl:[1,-2],ll:[-1,-2]};
 const P=o=>Object.assign({},ST,o);
 // ---- moves: v = view ('s' side, 'f' front), k = keyframes [pose, ms to next], props, anchor ----
 const stand=P({}),squatLow=P({t:38,ra:[86,0],la:[84,0],rl:[88,-104],ll:[86,-104]});
 const plankUp=P({t:76,ra:[-2,0],la:[2,0],rl:[-76,0],ll:[-78,0]});
 const M={
  march:{v:'s',k:[[P({rl:[72,-95],ll:[-2,-2],ra:[-28,25],la:[34,40]}),420],[stand,300],[P({ll:[72,-95],rl:[-2,-2],la:[-28,25],ra:[34,40]}),420],[stand,300]]},
  armc:{v:'f',k:[[P({ra:[70,0],la:[70,0]}),350],[P({ra:[100,0],la:[100,0]}),350],[P({ra:[80,0],la:[80,0]}),350],[P({ra:[110,0],la:[110,0]}),350]]},
  legsw:{v:'s',props:['wall'],k:[[P({x:90,ra:[78,8],la:[74,10],rl:[42,-6]}),550],[P({x:90,ra:[78,8],la:[74,10],rl:[-36,-14]}),550]]},
  squat:{v:'s',k:[[P({ra:[80,0],la:[78,0]}),700],[squatLow,700]]},
  kpush:{v:'s',padx:14,k:[[P({x:110,t:66,ra:[-4,0],la:[0,0],rl:[-38,-62],ll:[-40,-62]}),650],[P({x:110,t:80,ra:[-58,62],la:[-55,60],rl:[-52,-48],ll:[-54,-48]}),650]]},
  push:{v:'s',padx:14,k:[[plankUp,650],[P({t:86,ra:[-60,62],la:[-58,60],rl:[-86,0],ll:[-88,0]}),650]]},
  bridge:{v:'s',k:[[P({x:110,t:-90,ra:[92,0],la:[90,0],rl:[136,-118],ll:[134,-118]}),700],[P({x:110,t:-112,ra:[70,0],la:[68,0],rl:[100,-96],ll:[98,-96]}),700]]},
  lunge:{v:'s',k:[[P({ra:[8,30],la:[-6,30]}),650],[P({t:3,ra:[8,30],la:[-6,30],rl:[78,-84],ll:[-18,-96]}),650]]},
  plank:{v:'s',padx:14,k:[[P({t:84,ra:[0,92],la:[2,92],rl:[-85,0],ll:[-86,0]}),1300],[P({t:82,ra:[0,92],la:[2,92],rl:[-84,0],ll:[-85,0]}),1300]]},
  superman:{v:'s',padx:14,k:[[P({x:84,t:90,ra:[90,0],la:[90,0],rl:[-90,0],ll:[-90,0]}),700],[P({x:84,t:80,ra:[104,0],la:[102,0],rl:[-100,0],ll:[-100,0]}),700]]},
  deadbug:{v:'s',k:[[P({x:110,t:-90,ra:[-180,0],la:[-180,0],rl:[178,-88],ll:[176,-88]}),650],[P({x:110,t:-90,ra:[-100,0],la:[-180,0],rl:[178,-88],ll:[100,0]}),650],[P({x:110,t:-90,ra:[-180,0],la:[-180,0],rl:[178,-88],ll:[176,-88]}),650],[P({x:110,t:-90,ra:[-180,0],la:[-100,0],rl:[100,0],ll:[176,-88]}),650]]},
  dips:{v:'s',props:['chair'],anchor:['rh',72,118],k:[[P({t:2,ra:[-12,0],la:[-12,0],rl:[84,-84],ll:[82,-84]}),650],[P({t:10,ra:[-78,72],la:[-76,70],rl:[62,-58],ll:[60,-58]}),650]]},
  wallsit:{v:'s',props:['wall-back'],k:[[P({x:90,ra:[34,112],la:[30,112],rl:[88,-88],ll:[86,-88]}),1200],[P({x:90,t:2,ra:[34,112],la:[30,112],rl:[88,-88],ll:[86,-88]}),1200]]},
  calf:{v:'s',k:[[P({ra:[4,30],la:[-4,30]}),600],[P({ra:[4,30],la:[-4,30],f:-64}),600]]},
  splank:{v:'f',k:[[P({x:96,t:64,ra:[0,0],la:[160,0],rl:[64,0],ll:[-62,0]}),1200],[P({x:96,t:60,ra:[2,0],la:[162,0],rl:[60,0],ll:[-58,0]}),1200]]},
  stepup:{v:'s',pad:30,props:['box'],k:[[P({x:78,ra:[10,20],la:[-8,20]}),500],[P({x:84,t:8,rl:[66,-92],ll:[-4,-4],ra:[-20,30],la:[26,30]}),550],[P({x:132,ra:[6,20],la:[-6,20]}),550],[P({x:84,t:8,rl:[66,-92],ll:[-4,-4],ra:[-20,30],la:[26,30]}),500]]},
  jacks:{v:'f',pad:16,k:[[P({ra:[12,0],la:[12,0],rl:[4,0],ll:[4,0]}),330],[P({ra:[168,0],la:[168,0],rl:[24,0],ll:[24,0],air:-6}),330]]},
  knees:{v:'s',k:[[P({t:4,rl:[84,-100],ll:[-6,-6],ra:[-40,95],la:[40,95],air:-3}),240],[P({t:4,ll:[84,-100],rl:[-6,-6],la:[-40,95],ra:[40,95],air:-3}),240]]},
  climb:{v:'s',padx:14,k:[[P({t:70,ra:[0,0],la:[2,0],rl:[58,-118],ll:[-72,0]}),300],[P({t:70,ra:[0,0],la:[2,0],ll:[58,-118],rl:[-72,0]}),300]]},
  skater:{v:'f',k:[[P({x:72,t:-14,ra:[150,20],la:[40,20],rl:[10,-30],ll:[50,-40]}),480],[P({x:128,t:14,la:[150,20],ra:[40,20],ll:[10,-30],rl:[50,-40]}),480]]},
  burpee:{v:'s',pad:30,k:[[stand,350],[P({t:60,ra:[30,0],la:[28,0],rl:[100,-128],ll:[98,-128]}),350],[P({t:76,ra:[-2,0],la:[2,0],rl:[-76,0],ll:[-78,0]}),400],[P({t:60,ra:[30,0],la:[28,0],rl:[100,-128],ll:[98,-128]}),350],[P({ra:[176,0],la:[172,0],air:-12}),400]]},
  box:{v:'s',k:[[P({t:6,ra:[90,0],la:[34,128],rl:[16,-20],ll:[-16,-14]}),300],[P({t:6,ra:[32,130],la:[90,0],rl:[16,-20],ll:[-16,-14]}),300]]},
  gsquat:{v:'s',props:['db-chest'],k:[[P({ra:[22,140],la:[20,140]}),750],[P({t:34,ra:[30,135],la:[28,135],rl:[88,-104],ll:[86,-104]}),750]]},
  bench:{v:'s',props:['bench','db-hands'],anchor:['hip',112,138],k:[[P({t:-90,ra:[180,0],la:[178,0],rl:[100,-100],ll:[98,-100]}),750],[P({t:-90,ra:[62,118],la:[60,118],rl:[100,-100],ll:[98,-100]}),750]]},
  row:{v:'s',props:['row'],anchor:['hip',74,160],k:[[P({t:6,ra:[90,0],la:[88,0],rl:[104,-30],ll:[102,-30]}),700],[P({t:-6,ra:[-32,122],la:[-30,120],rl:[104,-30],ll:[102,-30]}),700]]},
  latpd:{v:'s',props:['lat'],anchor:['hip',100,148],k:[[P({t:-4,ra:[176,0],la:[174,0],rl:[88,-86],ll:[86,-86]}),700],[P({t:-10,ra:[24,148],la:[22,148],rl:[88,-86],ll:[86,-86]}),700]]},
  ohp:{v:'s',props:['seat','db-hands'],anchor:['hip',96,150],k:[[P({ra:[12,160],la:[10,160],rl:[88,-88],ll:[86,-88]}),700],[P({ra:[178,0],la:[176,0],rl:[88,-88],ll:[86,-88]}),700]]},
  rdl:{v:'s',props:['db-hands'],k:[[P({ra:[2,0],la:[0,0]}),800],[P({t:74,ra:[0,0],la:[-2,0],rl:[14,-16],ll:[12,-16]}),800]]},
  lpress:{v:'s',props:['legpress'],anchor:['hip',70,150],k:[[P({t:-42,ra:[40,30],la:[38,30],rl:[150,-12],ll:[148,-12]}),750],[P({t:-42,ra:[40,30],la:[38,30],rl:[176,-104],ll:[174,-104]}),750]]},
  curl:{v:'s',props:['db-hands'],k:[[P({ra:[2,6],la:[0,6]}),700],[P({ra:[6,146],la:[4,146]}),700]]},
  triext:{v:'s',pad:34,props:['db-hands'],k:[[P({ra:[172,0],la:[170,0]}),750],[P({ra:[170,158],la:[168,158]}),750]]},
  walk:{v:'s',k:[[P({rl:[24,-6],ll:[-22,-14],ra:[-24,16],la:[26,20]}),380],[P({rl:[2,-24],ll:[-2,-4],ra:[2,16],la:[0,16]}),380],[P({ll:[24,-6],rl:[-22,-14],la:[-24,16],ra:[26,20]}),380],[P({ll:[2,-24],rl:[-2,-4],la:[2,16],ra:[0,16]}),380]]},
  jog:{v:'s',k:[[P({t:8,rl:[36,-40],ll:[-26,-70],ra:[-36,90],la:[34,90],air:-4}),300],[P({t:8,ll:[36,-40],rl:[-26,-70],la:[-36,90],ra:[34,90],air:-4}),300]]},
  bike:{v:'s',props:['bike'],anchor:['hip',92,112],k:[[P({t:24,ra:[64,20],la:[62,20],rl:[78,-50],ll:[30,-80]}),320],[P({t:24,ra:[64,20],la:[62,20],rl:[60,-100],ll:[50,-30]}),320],[P({t:24,ra:[64,20],la:[62,20],rl:[30,-80],ll:[78,-50]}),320],[P({t:24,ra:[64,20],la:[62,20],rl:[50,-30],ll:[60,-100]}),320]]},
  ellip:{v:'s',props:['ellip'],k:[[P({t:4,rl:[16,-12],ll:[-14,-16],ra:[44,60],la:[20,70],air:-14}),500],[P({t:4,ll:[16,-12],rl:[-14,-16],la:[44,60],ra:[20,70],air:-14}),500]]},
  catcow:{v:'s',k:[[P({x:94,t:90,h:-32,b:-9,ra:[0,0],la:[2,0],rl:[0,-90],ll:[2,-90]}),900],[P({x:94,t:90,h:38,b:12,ra:[0,0],la:[2,0],rl:[0,-90],ll:[2,-90]}),900]]},
  child:{v:'s',k:[[P({x:78,t:110,h:12,ra:[84,0],la:[82,0],rl:[30,-120],ll:[28,-118]}),1300],[P({x:78,t:105,h:12,ra:[86,0],la:[84,0],rl:[30,-120],ll:[28,-118]}),1300]]},
  ham:{v:'s',k:[[P({x:72,t:20,ra:[78,0],la:[76,0],rl:[90,0],ll:[128,-150]}),1100],[P({x:72,t:50,ra:[84,0],la:[82,0],rl:[90,0],ll:[128,-150]}),1100]]},
  hip:{v:'s',k:[[P({ra:[40,40],la:[36,40],rl:[86,-86],ll:[-18,-74]}),1100],[P({x:106,t:-4,ra:[40,40],la:[36,40],rl:[96,-96],ll:[-30,-62]}),1100]]},
  chest:{v:'s',k:[[P({ra:[-24,0],la:[-26,0]}),1100],[P({t:-6,h:-8,ra:[-52,0],la:[-54,0]}),1100]]},
  twist:{v:'s',padx:14,anchor:['hip',110,172],k:[[P({x:110,t:-90,ra:[-92,0],la:[-88,0],rl:[150,-120],ll:[148,-120]}),1100],[P({x:110,t:-90,ra:[-92,0],la:[-88,0],rl:[104,-118],ll:[100,-116]}),1100]]},
  cobra:{v:'s',padx:14,k:[[P({x:92,t:90,ra:[-58,74],la:[-56,72],rl:[-90,0],ll:[-90,0]}),900],[P({x:92,t:52,h:-14,ra:[0,0],la:[2,0],rl:[-90,0],ll:[-90,0]}),900]]},
  // ---- more moves (v122) ----
  crunch:{v:'s',anchor:['hip',112,172],k:[[P({x:110,t:-90,ra:[100,0],la:[98,0],rl:[136,-118],ll:[134,-118]}),700],[P({x:110,t:-62,h:-8,ra:[116,0],la:[114,0],rl:[136,-118],ll:[134,-118]}),700]]},
  bicycle:{v:'s',anchor:['hip',112,172],k:[[P({x:110,t:-66,ra:[-150,-150],la:[-152,-150],rl:[156,-110],ll:[104,-4]}),520],[P({x:110,t:-66,ra:[-150,-150],la:[-152,-150],ll:[156,-110],rl:[104,-4]}),520]]},
  legraise:{v:'s',anchor:['hip',112,172],k:[[P({x:110,t:-90,ra:[94,0],la:[92,0],rl:[100,0],ll:[98,0]}),800],[P({x:110,t:-90,ra:[94,0],la:[92,0],rl:[172,0],ll:[170,0]}),800]]},
  flutter:{v:'s',anchor:['hip',112,172],k:[[P({x:110,t:-84,ra:[94,0],la:[92,0],rl:[104,0],ll:[124,0]}),260],[P({x:110,t:-84,ra:[94,0],la:[92,0],rl:[124,0],ll:[104,0]}),260]]},
  rtwist:{v:'s',anchor:['hip',96,170],k:[[P({t:-34,ra:[70,40],la:[100,40],rl:[112,-118],ll:[110,-116]}),480],[P({t:-34,h:6,ra:[118,40],la:[52,40],rl:[112,-118],ll:[110,-116]}),480]]},
  sumosq:{v:'f',pad:10,k:[[P({ra:[34,130],la:[34,130],rl:[22,0],ll:[22,0]}),700],[P({ra:[34,130],la:[34,130],rl:[58,-70],ll:[58,-70]}),700]]},
  jsquat:{v:'s',pad:44,k:[[squatLow,520],[P({ra:[170,0],la:[168,0],rl:[2,0],ll:[-2,0],f:-50,air:-22}),380],[stand,300]]},
  slunge:{v:'f',k:[[stand,450],[P({x:80,t:-6,ra:[30,120],la:[30,120],rl:[54,-72],ll:[34,0]}),650],[stand,450],[P({x:120,t:6,ra:[30,120],la:[30,120],ll:[54,-72],rl:[34,0]}),650]]},
  dkick:{v:'s',k:[[P({x:94,t:90,ra:[0,0],la:[2,0],rl:[0,-90],ll:[2,-90]}),600],[P({x:94,t:90,ra:[0,0],la:[2,0],rl:[-112,-68],ll:[2,-90]}),600]]},
  hipabd:{v:'f',k:[[P({ra:[30,140],la:[30,140]}),600],[P({t:5,ra:[30,140],la:[30,140],ll:[42,0]}),600],[P({ra:[30,140],la:[30,140]}),600],[P({t:-5,ra:[30,140],la:[30,140],rl:[42,0]}),600]]},
  shtap:{v:'s',padx:14,k:[[plankUp,420],[P({t:76,ra:[150,-150],la:[2,0],rl:[-76,0],ll:[-78,0]}),420],[plankUp,420],[P({t:76,ra:[-2,0],la:[150,-150],rl:[-76,0],ll:[-78,0]}),420]]},
  inch:{v:'s',padx:20,k:[[stand,700],[P({t:118,h:20,ra:[0,0],la:[2,0]}),800],[P({t:96,ra:[-4,0],la:[-2,0],rl:[-30,0],ll:[-32,0]}),700],[plankUp,800]]},
  wpush:{v:'s',props:['wall'],k:[[P({x:86,t:22,ra:[78,0],la:[76,0],rl:[-6,0],ll:[-8,0],f:-30}),700],[P({x:86,t:34,ra:[30,62],la:[28,62],rl:[-6,0],ll:[-8,0],f:-36}),700]]},
  sitstand:{v:'s',props:['chair'],k:[[P({x:92,ra:[80,0],la:[78,0]}),800],[P({x:86,t:30,ra:[80,0],la:[78,0],rl:[86,-98],ll:[84,-98]}),800]]},
  birddog:{v:'s',padx:16,k:[[P({x:94,t:90,ra:[0,0],la:[2,0],rl:[0,-90],ll:[2,-90]}),650],[P({x:94,t:90,ra:[92,0],la:[2,0],rl:[0,-90],ll:[-92,0]}),650],[P({x:94,t:90,ra:[0,0],la:[2,0],rl:[0,-90],ll:[2,-90]}),650],[P({x:94,t:90,ra:[0,0],la:[92,0],rl:[-92,0],ll:[2,-90]}),650]]},
  fold:{v:'s',pad:30,k:[[P({ra:[170,0],la:[168,0]}),1100],[P({t:122,h:16,ra:[2,0],la:[4,0]}),1100]]},
  quad:{v:'s',k:[[P({ra:[-30,60],la:[4,14],rl:[-8,-158],ll:[-1,-2],f:40}),1300],[P({t:2,ra:[-32,62],la:[60,0],rl:[-12,-158],ll:[-1,-2],f:40}),1300]]},
  sbend:{v:'f',k:[[P({t:-14,la:[150,30],ra:[14,0]}),1100],[P({t:14,ra:[150,30],la:[14,0]}),1100]]},
  neck:{v:'f',k:[[P({h:-34}),900],[P({h:0}),500],[P({h:34}),900],[P({h:0}),500]]},
  reach:{v:'f',pad:30,k:[[P({ra:[172,0],la:[172,0],f:0}),1100],[P({ra:[178,0],la:[178,0],air:-4}),1100]]},
  rope:{v:'f',k:[[P({ra:[24,40],la:[24,40],air:0}),200],[P({ra:[30,50],la:[30,50],air:-9}),200]]},
  butt:{v:'s',k:[[P({t:6,rl:[-14,-150],ll:[4,-6],ra:[-36,90],la:[36,90],air:-3}),240],[P({t:6,ll:[-14,-150],rl:[4,-6],la:[-36,90],ra:[36,90],air:-3}),240]]},
  kneeup:{v:'s',pad:30,k:[[P({ra:[176,0],la:[174,0]}),450],[P({t:6,ra:[46,100],la:[44,100],rl:[94,-100]}),450],[P({ra:[176,0],la:[174,0]}),450],[P({t:6,ra:[46,100],la:[44,100],ll:[94,-100]}),450]]},
  latraise:{v:'f',props:['db-hands'],k:[[P({ra:[10,4],la:[10,4]}),800],[P({ra:[84,4],la:[84,4]}),800]]},
  dbrow:{v:'s',props:['db-hands'],k:[[P({t:62,ra:[0,0],la:[2,0],rl:[14,-18],ll:[12,-18]}),700],[P({t:62,ra:[-46,50],la:[-44,50],rl:[14,-18],ll:[12,-18]}),700]]},
  rest:{v:'f',k:[[P({}),1400],[P({t:1,ra:[6,14],la:[6,14]}),1400]]},
 };
 // ---- geometry ----
 const lerp=(a,b,u)=>a+(b-a)*u;
 function mix(p,q,u){const o={};for(const k in p){const a=p[k],b=q[k];o[k]=Array.isArray(a)?a.map((v,i)=>lerp(v,b[i],u)):lerp(a,b,u)}return o}
 function joints(p,v){const td=[Math.sin(rad(p.t)),-Math.cos(rad(p.t))],H=[p.x,p.y];
  const at=(o,d,l)=>[o[0]+d[0]*l,o[1]+d[1]*l];
  const N=at(H,td,TL),Sh=at(H,td,TL-6),head=at(H,td,TL+17);
  const perp=[-td[1],td[0]]; // to the right of the spine
  const front=v==='f',sw=front?16:0,hw=front?8:0;
  const side=(o,s,w)=>[o[0]+perp[0]*w*s,o[1]+perp[1]*w*s];
  const mir=(a,s)=>front?a*s:a;
  const limb=(o,[a,bend],l1,l2,s)=>{const a1=mir(a,s),a2=mir(a+bend,s);const m=at(o,dir(a1),l1);return [o,m,at(m,dir(a2),l2),a2]};
  const R={H,N,head,td,
   ra:limb(side(Sh,-1,sw),p.ra,UA,FA,-1),la:limb(side(Sh,1,sw),p.la,UA,FA,1),
   rl:limb(side(H,-1,hw),p.rl,TH,SH,-1),ll:limb(side(H,1,hw),p.ll,TH,SH,1)};
  for(const k of ['rl','ll']){const l=R[k],fa=front?(k==='rl'?-1:1)*20:l[3]+90+p.f;R[k].push(at(l[2],dir(fa),front?6:FT))}
  return R}
 function shift(R,dx,dy){const mv=pt=>{pt[0]+=dx;pt[1]+=dy};mv(R.H);mv(R.N);mv(R.head);for(const k of ['ra','la','rl','ll'])R[k].forEach(x=>Array.isArray(x)&&mv(x))}
 function floorAt(m,x){if(m.props&&m.props.includes('box')&&x>112&&x<168)return FLOOR-30;return FLOOR}
 function place(m,p){const R=joints(p,m.v);
  if(m.anchor){const [j,ax,ay]=m.anchor,pt=j==='hip'?R.H:j==='rh'?R.ra[2]:R.H;shift(R,ax-pt[0],ay-pt[1]);return R}
  const pts=[[R.H,10],[R.N,10],[R.head,15],...['ra','la'].flatMap(k=>[[R[k][1],5],[R[k][2],5]]),...['rl','ll'].flatMap(k=>[[R[k][1],6],[R[k][2],4],[R[k][4],4]])];
  let d=Infinity;for(const [pt,r] of pts)d=Math.min(d,floorAt(m,pt[0])-(pt[1]+r));shift(R,0,d+p.air);return R}
 // ---- drawing ----
 const ns='http://www.w3.org/2000/svg';
 const pl=a=>a.map(q=>q[0].toFixed(1)+' '+q[1].toFixed(1)).join(' L ');
 function seg(a,b,u){return [a[0]+(b[0]-a[0])*u,a[1]+(b[1]-a[1])*u]}
 function build(svg,id){const m=M[id];svg.innerHTML='';const g={};
  if(m.pad||m.padx){const px=m.padx||0,py=m.pad||0;svg.setAttribute('viewBox',`${-px} ${-py} ${W+2*px} ${W+py}`)}
  const mk=(tag,attrs,parent)=>{const e=document.createElementNS(ns,tag);for(const k in attrs)e.setAttribute(k,attrs[k]);(parent||svg).appendChild(e);return e};
  const line=(w,c)=>mk('path',{fill:'none',stroke:c,'stroke-width':w,'stroke-linecap':'round','stroke-linejoin':'round'});
  mk('ellipse',{cx:100,cy:FLOOR+3,rx:70,ry:3,fill:'currentColor',opacity:.08});
  g.propsBack=mk('g',{});
  const limb=(far,arm)=>{const o=line(arm?12:14.5,C.line),s=line(arm?8.5:11,far?C.skin2:C.skin),c=line(arm?11:14,arm?(far?C.shirt2:C.shirt):(far?C.shorts2:C.shorts)),fo=arm?null:line(8.5,C.line),fs=arm?null:line(5.5,C.shoe);return {o,s,c,fo,fs}};
  const front=m.v==='f';
  g.ll=limb(!front,false);g.la=limb(!front,true);
  g.to=line(front?37:25,C.line);g.ts=line(front?33.5:21.5,C.shirt);g.tb=line(front?33.5:21.5,C.shorts);
  g.rl=limb(false,false);
  g.hd=mk('g',{});mk('circle',{r:15,fill:C.skin,stroke:C.line,'stroke-width':3},g.hd);
  mk('path',{d:front?'M -15.5 1 A 15.5 15.5 0 0 1 15.5 1 Q 8 -7 0 -6 Q -8 -7 -15.5 1 Z':'M -15.5 3 A 15.5 15.5 0 0 1 11 -11 Q 3 -6 -3 -4 Q -9 -1 -15.5 3 Z',fill:C.hair,stroke:C.line,'stroke-width':2.5,'stroke-linejoin':'round'},g.hd);
  if(front){mk('circle',{cx:-5,cy:1,r:1.8,fill:C.line},g.hd);mk('circle',{cx:5,cy:1,r:1.8,fill:C.line},g.hd);mk('path',{d:'M -4 7 Q 0 10 4 7',fill:'none',stroke:C.line,'stroke-width':1.6,'stroke-linecap':'round'},g.hd)}
  else{mk('circle',{cx:8,cy:0,r:1.9,fill:C.line},g.hd);mk('path',{d:'M 7 7 Q 10 8.5 12 6.5',fill:'none',stroke:C.line,'stroke-width':1.6,'stroke-linecap':'round'},g.hd);mk('circle',{cx:-3,cy:2,r:3,fill:C.skin,stroke:C.line,'stroke-width':1.6},g.hd)}
  g.ra=limb(false,true);
  g.propsFront=mk('g',{});
  svg.__g=g;
 }
 function drawLimb(L,j,arm){const [a,b,c]=j;const d='M'+pl([a,b,c]);L.o.setAttribute('d',d);L.s.setAttribute('d',d);L.c.setAttribute('d','M'+pl([a,seg(a,b,arm?.42:.5)]));
  if(!arm){const fd='M'+pl([c,j[4]]);L.fo.setAttribute('d',fd);L.fs.setAttribute('d',fd)}}
 function props(m,R,back,front){back.innerHTML='';front.innerHTML='';const P=m.props||[];
  const r=(g,x,y,w,h,f,rx)=>{const e=document.createElementNS(ns,'rect');Object.entries({x,y,width:w,height:h,rx:rx||2,fill:f||C.prop,stroke:C.line,'stroke-width':2}).forEach(([k,v])=>e.setAttribute(k,v));g.appendChild(e)};
  const ln=(g,a,b,w,c)=>{const e=document.createElementNS(ns,'path');e.setAttribute('d','M'+pl([a,b]));e.setAttribute('stroke',c||C.prop2);e.setAttribute('stroke-width',w||3);e.setAttribute('stroke-linecap','round');e.setAttribute('fill','none');g.appendChild(e)};
  const db=(g,h)=>{r(g,h[0]-8,h[1]-3,16,6,'#4A5068',2);r(g,h[0]-10,h[1]-6,5,12,'#2F3447',2);r(g,h[0]+5,h[1]-6,5,12,'#2F3447',2)};
  for(const p of P){
   if(p==='wall')r(back,150,40,14,FLOOR-40,'#C9CED9',3);
   if(p==='wall-back')r(back,R.H[0]-28,40,12,FLOOR-40,'#C9CED9',3);
   if(p==='box')r(back,112,FLOOR-30,56,30,'#C08A5B',4);
   if(p==='chair'){r(back,40,116,40,7,'#9C6B45',2);ln(back,[44,123],[44,FLOOR],4,'#7A5236');ln(back,[76,123],[76,FLOOR],4,'#7A5236');ln(back,[44,116],[44,70],4,'#7A5236')}
   if(p==='bench'){r(back,60,140,110,9,C.pad,3);ln(back,[72,149],[72,FLOOR],5);ln(back,[158,149],[158,FLOOR],5)}
   if(p==='seat'){r(back,76,150,40,9,C.pad,3);r(back,72,82,9,70,C.pad,3);ln(back,[96,159],[96,FLOOR],5)}
   if(p==='row'){r(back,50,160,46,8,C.pad,3);ln(back,[73,168],[73,FLOOR],5);r(back,170,120,10,66,C.prop2,2);ln(back,R.ra[2],[172,150],2,'#555');r(back,150,150,8,30,C.prop,2)}
   if(p==='lat'){r(back,82,148,40,8,C.pad,3);ln(back,[102,156],[102,FLOOR],5);r(back,118,124,22,9,C.pad,3);ln(back,[100,6],[R.ra[2][0],R.ra[2][1]],2,'#555');ln(front,[R.ra[2][0]-26,R.ra[2][1]],[R.ra[2][0]+26,R.ra[2][1]],4,'#3A4055')}
   if(p==='legpress'){r(back,40,150,50,9,C.pad,3);ln(back,[52,159],[52,FLOOR],5);const f=R.rl[4];ln(front,[f[0]-14,f[1]+16],[f[0]+14,f[1]-16],7,'#4A5068')}
   if(p==='bike'){ln(back,[92,118],[92,FLOOR-30],5);ln(back,[92,FLOOR-30],[150,FLOOR-30],5);ln(back,[150,FLOOR-30],[150,80],5);ln(back,[150,80],[136,80],5);r(back,80,112,24,7,C.pad,3);const c=document.createElementNS(ns,'circle');c.setAttribute('cx',112);c.setAttribute('cy',FLOOR-38);c.setAttribute('r',13);c.setAttribute('fill','none');c.setAttribute('stroke',C.prop2);c.setAttribute('stroke-width',4);back.appendChild(c);ln(back,[60,FLOOR],[160,FLOOR],5)}
   if(p==='ellip'){ln(back,[50,FLOOR],[170,FLOOR],6);ln(back,[150,FLOOR],[150,64],5);ln(front,R.ra[2],[150,70],3);ln(front,R.rl[4],[R.rl[4][0]+10,R.rl[4][1]],5,'#4A5068')}
   if(p==='db-hands'){db(front,R.ra[2]);db(back,R.la[2])}
   if(p==='db-chest'){r(front,R.ra[2][0]-4,R.ra[2][1]-11,8,22,'#4A5068',2)}
  }}
 function render(svg,id,ms){const m=M[id];if(!svg.__g)build(svg,id);const g=svg.__g;
  const k=m.k,total=k.reduce((a,x)=>a+x[1],0);let t=((ms%total)+total)%total,i=0;while(t>k[i][1]){t-=k[i][1];i++}
  const u=(1-Math.cos(Math.PI*t/k[i][1]))/2,p=mix(k[i][0],k[(i+1)%k.length][0],u),R=place(m,p);
  drawLimb(g.ll,R.ll,false);drawLimb(g.la,R.la,true);drawLimb(g.rl,R.rl,false);drawLimb(g.ra,R.ra,true);
  // torso with an optional bow (cat/cow), shorts on the lower part
  const H=R.H,N=R.N,mid=seg(H,N,.5),perp=[R.td[1],-R.td[0]],cp=[mid[0]+perp[0]*-p.b,mid[1]+perp[1]*-p.b];
  const td=`M ${H[0].toFixed(1)} ${H[1].toFixed(1)} Q ${cp[0].toFixed(1)} ${cp[1].toFixed(1)} ${N[0].toFixed(1)} ${N[1].toFixed(1)}`;
  g.to.setAttribute('d',td);g.ts.setAttribute('d',td);g.tb.setAttribute('d','M'+pl([seg(H,N,-.05),seg(H,N,.24)]));
  g.hd.setAttribute('transform',`translate(${R.head[0].toFixed(1)} ${R.head[1].toFixed(1)}) rotate(${(p.t+p.h).toFixed(1)})`);
  if(m.props)props(m,R,g.propsBack,g.propsFront)}
 // ---- mounting: one animation loop for every figure on screen ----
 const live=new Set();let raf=0;const t0=performance.now();
 const still=()=>{try{return matchMedia('(prefers-reduced-motion: reduce)').matches}catch(e){return false}};
 function loop(now){for(const s of live){if(!s.isConnected){live.delete(s);continue}render(s,s.dataset.rig,now-t0+(+s.dataset.off||0))}raf=live.size?requestAnimationFrame(loop):0}
 function scan(root){(root||document).querySelectorAll('svg[data-rig]').forEach(s=>{if(s.__on)return;s.__on=1;render(s,s.dataset.rig,0);if(!still())live.add(s)});if(live.size&&!raf)raf=requestAnimationFrame(loop)}
 const svg=(id,cls)=>`<svg class="fw-rig ${cls||''}" data-rig="${id}" viewBox="0 0 ${W} ${W}" aria-hidden="true"></svg>`;
 window.FWRig={has:id=>!!M[id],svg,scan,render,moves:Object.keys(M),cycle:id=>M[id].k.reduce((a,x)=>a+x[1],0)};
})();
