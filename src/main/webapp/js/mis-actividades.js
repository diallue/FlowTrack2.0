const API = {
  listActivities: (perPage = 30, page = 1, filters = {}) => {
    const params = new URLSearchParams();
    params.set('per_page', perPage);
    params.set('page', page);

    if (filters.type) params.set('type', filters.type);
    if (filters.date_from) params.set('date_from', filters.date_from);
    if (filters.date_to) params.set('date_to', filters.date_to);
    if (filters.distance_min) params.set('distance_min', filters.distance_min);
    if (filters.q) params.set('q', filters.q);
    if (filters.sort) params.set('sort', filters.sort);

    return fetch(`./api/activities?${params.toString()}`, { credentials: 'same-origin' })
      .then(checkJson);
  },
  getStreams: (activityId, keys = 'time,latlng,altitude,velocity_smooth,heartrate,watts,cadence', keyByType = true) => {
    const params = new URLSearchParams({ keys, key_by_type: keyByType ? 'true' : 'false' });
    params.set('d', activityId);
    return fetch(`./api/streams?${params.toString()}`, { credentials: 'same-origin' })
      .then(checkJson);
  }
};

function checkJson(res){
  if (!res.ok) {
    return res.json().catch(()=>{ throw new Error(`HTTP ${res.status}`) }).then(err => { throw err; });
  }
  return res.json();
}

let state = {
  perPage: 30,
  page: 1,
  loading: false,
  reachedEnd: false,
  activities: [],
  filters: {},
  sort: 'start_date_local_desc'
};

const el = {
  activities: document.getElementById('activities'),
  loadMore: document.getElementById('load-more'),
  loadingIndicator: document.getElementById('loading-indicator'),
  noMore: document.getElementById('no-more'),
  totalActivities: document.getElementById('total-activities'),
  loadedPage: document.getElementById('loaded-page'),
  applyFilters: document.getElementById('apply-filters'),
  clearFilters: document.getElementById('clear-filters'),
  filterType: document.getElementById('filter-type'),
  filterFrom: document.getElementById('filter-date-from'),
  filterTo: document.getElementById('filter-date-to'),
  filterDistance: document.getElementById('filter-distance-min'),
  filterSearch: document.getElementById('filter-search'),
  exportCsv: document.getElementById('export-csv'),
  sortBy: document.getElementById('sort-by'),
  refreshBtn: document.getElementById('refresh-all')
};

el.loadMore.addEventListener('click', () => loadNextPage());
el.applyFilters.addEventListener('click', () => applyFilters());
el.clearFilters.addEventListener('click', () => clearFilters());
el.exportCsv.addEventListener('click', () => exportActivitiesCSV(state.activities));
el.sortBy.addEventListener('change', (e)=>{ state.sort = e.target.value; reloadAll(); });
el.refreshBtn.addEventListener('click', ()=> reloadAll());

let searchTimeout = null;
el.filterSearch.addEventListener('input', (e)=>{
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(()=> { applyFilters(); }, 600);
});

function formatDistance(m){ return (m/1000).toFixed(2)+' km'; }
function formatTime(s){ const h=Math.floor(s/3600), m=Math.floor((s%3600)/60), sec=s%60; return `${h>0?h+'h ':''}${m}m ${sec}s`; }
function dateShort(d){ return new Date(d).toLocaleString(); }

function renderActivities(list, append=true){
  if (!append) el.activities.innerHTML='';
  const frag = document.createDocumentFragment();
  list.forEach(act=>{
    const cardLink=document.createElement('a');
    cardLink.className='activity-card-link';
    cardLink.href=`actividad.html?id=${act.id}`;
    cardLink.style.textDecoration='none';
    cardLink.style.color='inherit';
    cardLink.style.display='block';

    const card=document.createElement('div');
    card.className='activity-card';
    card.dataset.id = act.id;

    const title=document.createElement('div');
    title.className='act-title';
    title.textContent=act.name||`${act.type||'Actividad'} — ${dateShort(act.start_date_local)}`;

    const meta=document.createElement('div');
    meta.className='act-meta';
    meta.innerHTML=`<div><span class="badge">${act.type}</span> ${dateShort(act.start_date_local)}</div><div>${formatDistance(act.distance||0)} • <span class="muted">${formatTime(act.elapsed_time||0)}</span></div>`;

    card.appendChild(title);
    card.appendChild(meta);
    cardLink.appendChild(card);
    frag.appendChild(cardLink);
  });
  el.activities.appendChild(frag);
}

async function loadNextPage(){
  if (state.loading || state.reachedEnd) return;
  state.loading=true;
  showLoading(true);
  try {
    const resp = await API.listActivities(state.perPage, state.page, {...state.filters, sort: state.sort});
    const activities = Array.isArray(resp) ? resp : (resp.activities||[]);
    if (activities.length===0){ state.reachedEnd=true; el.noMore.classList.remove('hidden'); }
    else { renderActivities(activities,true); state.activities.push(...activities); state.page+=1; }

    el.totalActivities.textContent=`Actividades (cargadas): ${state.activities.length}`;
    el.loadedPage.textContent=`Página: ${state.page-1}`;
  } catch(err){
    console.error('Error cargando actividades',err);
    const mensaje=err.error||err.message||JSON.stringify(err);
    alert('Detalle del error: '+mensaje);
    if (mensaje==="No autenticado"||mensaje.includes("Usuario no autenticado")) window.location.href='./login.html';
  } finally { state.loading=false; showLoading(false); }
}

function showLoading(v){ if(v){ el.loadingIndicator.classList.remove('hidden'); el.loadMore.disabled=true; } else { el.loadingIndicator.classList.add('hidden'); el.loadMore.disabled=false; } }

function applyFilters(){
  const type=el.filterType.value;
  const date_from=el.filterFrom.value||null;
  const date_to=el.filterTo.value||null;
  const distance_min=el.filterDistance.value ? parseFloat(el.filterDistance.value) : null;
  const q=el.filterSearch.value.trim()||null;

  state.filters={type,date_from,date_to,distance_min,q};
  reloadAll();
}

function clearFilters(){
  el.filterType.value=''; el.filterFrom.value=''; el.filterTo.value=''; el.filterDistance.value=''; el.filterSearch.value='';
  state.filters={};
  reloadAll();
}

function reloadAll(){ state.page=1; state.activities=[]; state.reachedEnd=false; el.noMore.classList.add('hidden'); el.activities.innerHTML=''; loadNextPage(); }

function exportActivitiesCSV(activities){
  if(!activities||activities.length===0)return alert('No hay actividades cargadas');
  const headers=['id','name','type','start_date_local','distance_m','elapsed_time_s','elev_gain_m','avg_speed_m_s'];
  const rows=activities.map(a=>[a.id,a.name,a.type,a.start_date_local,a.distance||'',a.elapsed_time||'',a.total_elevation_gain||'',a.average_speed||'']);
  downloadCSV([headers,...rows],'activities_export.csv');
}

function downloadCSV(table,filename){
  const csv=table.map(r=>r.map(c=>`"${String(c??'').replace(/"/g,'""')}"`).join(',')).join('\n');
  const blob=new Blob([csv],{type:'text/csv'});
  const url=URL.createObjectURL(blob);
  const a=document.createElement('a');
  a.href=url;
  a.download=filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

function init(){ reloadAll(); window.addEventListener('keydown',e=>{ if(e.key==='Escape'){} }); document.getElementById('logout').addEventListener('click',async()=>{ try{ await fetch('./logout',{method:'POST',credentials:'same-origin'}); } catch(e){} window.location.href='./login.html'; }); }

init();
