// The MeowFit Home cat, drawn as SVG in the app's art style (dark outline, flat fills, pink accents),
// with every cat-shop outfit drawn on (window.FWCatArt.svg({wear, pose})). Poses change the drawing
// (raised paws, a happy face) and get a matching CSS animation from index.html.
(function(){
 const O='#2A2230',FUR='#F4A65A',FUR2='#E08A3C',BELLY='#FCE3C4',PINK='#F49BB8',W=3;
 const st=`stroke="${O}" stroke-width="${W}" stroke-linejoin="round" stroke-linecap="round"`;
 // ---- outfits: [layer, svg]; layer "back" is drawn behind the head, "front" over everything ----
 const WEAR={
  bow:['front',`<g transform="translate(36 22) rotate(-18)"><path d="M0 0 C-14 -12 -18 8 -2 4 Z" fill="${PINK}" ${st}/><path d="M0 0 C14 -12 18 8 2 4 Z" fill="${PINK}" ${st}/><circle cx="0" cy="1.5" r="4" fill="#E06C97" ${st}/></g>`],
  flower:['front',`<g transform="translate(86 24)">${[0,72,144,216,288].map(a=>`<ellipse cx="0" cy="-7" rx="5" ry="7.5" fill="#FBC3D6" ${st} transform="rotate(${a})"/>`).join('')}<circle r="4.2" fill="#FFD25E" ${st}/></g>`],
  scarf:['front',`<path d="M32 74 Q60 86 88 74 L90 83 Q60 96 30 83 Z" fill="#E8557A" ${st}/><path d="M72 84 L78 106 L67 106 L64 87 Z" fill="#E8557A" ${st}/><path d="M38 79 L40 86 M48 82 L49 89 M58 83 L58 90 M68 82 L67 89 M78 80 L76 87" stroke="#FBD0DC" stroke-width="2.4" stroke-linecap="round" fill="none"/><path d="M68 98 h9 M66 102 h11" stroke="#FBD0DC" stroke-width="2" stroke-linecap="round"/>`],
  shades:['front',`<path d="M33 46 h22 q2 0 2 2 v3 q-1 9 -12 9 q-11 0 -12 -9 v-3 q0 -2 2 -2 Z" fill="#2A2230" ${st}/><path d="M65 46 h22 q2 0 2 2 v3 q-1 9 -12 9 q-11 0 -12 -9 v-3 q0 -2 2 -2 Z" fill="#2A2230" ${st}/><path d="M57 48 q3 -3 6 0" fill="none" ${st}/><path d="M37 49 l5 0 M69 49 l5 0" stroke="#8A8FA8" stroke-width="2.4" stroke-linecap="round"/>`],
  tophat:['front',`<g transform="translate(60 22) rotate(-6)"><ellipse cx="0" cy="0" rx="24" ry="5.5" fill="#2A2230" ${st}/><path d="M-14 0 V-27 Q0 -30 14 -27 V0 Z" fill="#2A2230" ${st}/><path d="M-14 -7 Q0 -4 14 -7 V-12 Q0 -9 -14 -12 Z" fill="${PINK}" ${st}/></g>`],
  phones:['front',`<path d="M27 47 Q27 9 60 9 Q93 9 93 47" fill="none" stroke="${O}" stroke-width="9" stroke-linecap="round"/><path d="M27 47 Q27 9 60 9 Q93 9 93 47" fill="none" stroke="#7FB8E8" stroke-width="4.5" stroke-linecap="round"/><rect x="18" y="38" width="15" height="22" rx="7" fill="#7FB8E8" ${st}/><rect x="87" y="38" width="15" height="22" rx="7" fill="#7FB8E8" ${st}/><rect x="22" y="42" width="5" height="14" rx="2.5" fill="#CFE6FA"/><rect x="93" y="42" width="5" height="14" rx="2.5" fill="#CFE6FA"/>`],
  medal:['front',`<path d="M46 74 L58 94 M74 74 L62 94" stroke="${O}" stroke-width="9" stroke-linecap="round"/><path d="M46 74 L58 94 M74 74 L62 94" stroke="#5B8DEF" stroke-width="5" stroke-linecap="round"/><circle cx="60" cy="98" r="9" fill="#FFC93C" ${st}/><path d="M60 92.5 l1.7 3.6 3.9 .4 -2.9 2.6 .8 3.9 -3.5 -2 -3.5 2 .8 -3.9 -2.9 -2.6 3.9 -.4 z" fill="#FFF1B8"/>`],
  santa:['front',`<g transform="translate(60 22) rotate(-8)"><path d="M-26 4 Q-20 -30 8 -26 Q30 -22 34 2 Z" fill="#E8394D" ${st}/><path d="M8 -26 Q26 -26 30 -10" fill="none" ${st}/><circle cx="31" cy="-8" r="6.5" fill="#fff" ${st}/><rect x="-30" y="-2" width="62" height="12" rx="6" fill="#fff" ${st}/></g>`],
  student:['front',`<path d="M33 112 Q31 84 44 78 L60 92 L76 78 Q89 84 87 112 Z" fill="#fff" ${st}/><path d="M44 78 L52 92 L60 84 Z M76 78 L68 92 L60 84 Z" fill="#fff" ${st}/><path d="M56 86 L64 86 L62 92 L58 92 Z" fill="#1F3A7A" ${st}/><path d="M58 92 L54 108 L60 113 L66 108 L62 92 Z" fill="#1F3A7A" ${st}/><rect x="70" y="96" width="11" height="9" rx="1.5" fill="#fff" ${st}/><path d="M72.5 99.5 h6 M72.5 102 h4" stroke="#5B8DEF" stroke-width="1.6" stroke-linecap="round"/><circle cx="47" cy="98" r="1.6" fill="${O}"/><circle cx="47" cy="105" r="1.6" fill="${O}"/>`],
  crown:['front',`<g transform="translate(60 20)"><path d="M-19 4 L-21 -16 L-10 -6 L0 -20 L10 -6 L21 -16 L19 4 Z" fill="#FFC93C" ${st}/><circle cx="-21" cy="-17" r="2.6" fill="#FFF1B8" ${st}/><circle cx="0" cy="-21" r="2.6" fill="#FFF1B8" ${st}/><circle cx="21" cy="-17" r="2.6" fill="#FFF1B8" ${st}/><circle cx="0" cy="-3" r="3" fill="#E8557A" ${st}/></g>`]
 };
 function face(pose,wear){
  if(wear==='shades')return `<path d="M54 64 l6 4 6 -4" fill="none" ${st}/>`;
  const happy=pose==='dance'||pose==='bounce';
  const eyes=happy
   ?`<path d="M38 52 q6 -7 12 0 M70 52 q6 -7 12 0" fill="none" ${st}/>`
   :`<ellipse cx="44" cy="51" rx="5" ry="6" fill="${O}"/><ellipse cx="76" cy="51" rx="5" ry="6" fill="${O}"/><circle cx="45.8" cy="48.6" r="1.8" fill="#fff"/><circle cx="77.8" cy="48.6" r="1.8" fill="#fff"/>`;
  const mouth=happy?`<path d="M53 63 q7 9 14 0 Z" fill="#E06C97" ${st}/>`:`<path d="M54 64 q3 4 6 0 q3 4 6 0" fill="none" ${st}/>`;
  return `${eyes}<ellipse cx="35" cy="60" rx="5.5" ry="3.4" fill="${PINK}" opacity=".75"/><ellipse cx="85" cy="60" rx="5.5" ry="3.4" fill="${PINK}" opacity=".75"/><path d="M57 58 h6 l-3 3.5 z" fill="#E06C97" ${st}/>${mouth}`}
 function paws(pose){
  if(pose==='dance')return `<path d="M30 84 Q16 70 20 56" fill="none" stroke="${O}" stroke-width="13" stroke-linecap="round"/><path d="M30 84 Q16 70 20 56" fill="none" stroke="${FUR}" stroke-width="8" stroke-linecap="round"/><path d="M90 84 Q104 70 100 56" fill="none" stroke="${O}" stroke-width="13" stroke-linecap="round"/><path d="M90 84 Q104 70 100 56" fill="none" stroke="${FUR}" stroke-width="8" stroke-linecap="round"/>`;
  if(pose==='bounce')return `<path d="M34 86 Q22 78 18 64" fill="none" stroke="${O}" stroke-width="13" stroke-linecap="round"/><path d="M34 86 Q22 78 18 64" fill="none" stroke="${FUR}" stroke-width="8" stroke-linecap="round"/><ellipse cx="72" cy="111" rx="9" ry="6" fill="${FUR}" ${st}/>`;
  return `<ellipse cx="48" cy="111" rx="9" ry="6" fill="${FUR}" ${st}/><ellipse cx="72" cy="111" rx="9" ry="6" fill="${FUR}" ${st}/><path d="M45 110 v3 M51 110 v3 M69 110 v3 M75 110 v3" stroke="${O}" stroke-width="1.6" stroke-linecap="round"/>`}
 function svg(o){o=o||{};const wear=o.wear||'',pose=o.pose||'',acc=WEAR[wear];
  const id='t'+Math.random().toString(36).slice(2,7);
  return `<svg class="fw-catsvg" viewBox="0 -8 120 128" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="MeowFit cat">
<g class="tail" style="transform-origin:86px 104px"><path d="M84 106 Q112 104 106 78 Q103 66 96 70" fill="none" stroke="${O}" stroke-width="13" stroke-linecap="round"/><path d="M84 106 Q112 104 106 78 Q103 66 96 70" fill="none" stroke="${FUR}" stroke-width="8" stroke-linecap="round"/><path d="M104 90 l5 -2 M106 80 l5 0" stroke="${FUR2}" stroke-width="2.6" stroke-linecap="round"/></g>
<path d="M32 112 Q28 76 60 74 Q92 76 88 112 Z" fill="${FUR}" ${st}/>
<path d="M44 112 Q44 88 60 87 Q76 88 76 112 Z" fill="${BELLY}"/>
${paws(pose)}
<path d="M26 34 L30 6 L50 22 Z" fill="${FUR}" ${st}/><path d="M32 26 L33 13 L43 22 Z" fill="${PINK}"/>
<path d="M94 34 L90 6 L70 22 Z" fill="${FUR}" ${st}/><path d="M88 26 L87 13 L77 22 Z" fill="${PINK}"/>
<rect x="22" y="18" width="76" height="62" rx="31" fill="${FUR}" ${st}/>
<path d="M52 22 v8 M60 20 v10 M68 22 v8" stroke="${FUR2}" stroke-width="3.2" stroke-linecap="round"/>
<path d="M22 58 l-10 -2 M22 63 l-10 2 M98 58 l10 -2 M98 63 l10 2" stroke="${O}" stroke-width="1.8" stroke-linecap="round"/>
${face(pose,wear)}
${acc?acc[1]:''}
${wear==='student'?paws(pose):''}
<title>${id}</title></svg>`}
 window.FWCatArt={svg,items:Object.keys(WEAR)};
})();
