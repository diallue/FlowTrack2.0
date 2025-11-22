// mis-actividades.js
// Usa module para mantener scope limpio
// Asume backend endpoints:
// GET  /api/strava/activities?per_page=50&page=1
// GET  /api/strava/activities/:id/streams?keys=...&key_by_type=true

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

/* ------------- Estado UI ------------- */
let state = {
  perPage: 30,
  page: 1,
  loading: false,
  reachedEnd: false,
  activities: [],
  filters: {},
  sort: 'start_date_local_desc'
};

/* ------------- DOM ------------- */
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
  refreshBtn: document.getElementById('refresh-all'),

  modal: document.getElementById('activity-modal'),
  modalClose: document.getElementById('modal-close'),
  actTitle: document.getElementById('act-title'),
  actSubtitle: document.getElementById('act-subtitle'),
  mapContainer: document.getElementById('map'),
  rawJson: document.getElementById('raw-json'),
  rawToggle: document.getElementById('open-raw'),
  exportActivityCsv: document.getElementById('export-activity-csv'),
  activityStats: document.getElementById('activity-stats'),
};

/* ------------- Event wiring ------------- */
el.loadMore.addEventListener('click', () => loadNextPage());
el.applyFilters.addEventListener('click', () => applyFilters());
el.clearFilters.addEventListener('click', () => clearFilters());
el.exportCsv.addEventListener('click', () => exportActivitiesCSV(state.activities));
el.sortBy.addEventListener('change', (e)=>{ state.sort = e.target.value; reloadAll(); });
el.refreshBtn.addEventListener('click', ()=> reloadAll());
//el.modalClose.addEventListener('click', ()=> hideModal());
document.getElementById('activities').addEventListener('click', onActivityClick);

// Debounce search input
let searchTimeout = null;
el.filterSearch.addEventListener('input', (e)=>{
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(()=> {
    applyFilters();
  }, 600);
});

/* ------------- Helpers ------------- */
function formatDistance(meters){
  return (meters/1000).toFixed(2) + ' km';
}
function formatTime(seconds){
  const h = Math.floor(seconds/3600);
  const m = Math.floor((seconds%3600)/60);
  const s = seconds%60;
  return `${h>0?h+'h ':''}${m}m ${s}s`;
}
function dateShort(d){
  return new Date(d).toLocaleString();
}

/* ------------- Rendering ------------- */
function renderActivities(list, append = true){
  if (!append) el.activities.innerHTML = '';
  const frag = document.createDocumentFragment();
  list.forEach(act => {
    const cardLink = document.createElement('a');
    cardLink.className = 'activity-card-link'; // Clase opcional para quitar subrayado en CSS
    cardLink.href = `actividad.html?id=${act.id}`; // <-- ENLACE A LA NUEVA PÁGINA
    cardLink.style.textDecoration = 'none';
    cardLink.style.color = 'inherit';
    cardLink.style.display = 'block';

    const card = document.createElement('div');
    card.className = 'activity-card';

    const title = document.createElement('div');
    title.className = 'act-title';
    title.textContent = act.name || `${act.type || 'Actividad'} — ${dateShort(act.start_date_local)}`;

    const meta = document.createElement('div');
    meta.className = 'act-meta';

    const left = document.createElement('div');
    left.innerHTML = `<span class="badge">${act.type}</span> ${dateShort(act.start_date_local)}`;

    const right = document.createElement('div');
    right.innerHTML = `${formatDistance(act.distance || 0)} • <span class="muted">${formatTime(act.elapsed_time || 0)}</span>`;

    meta.appendChild(left);
    meta.appendChild(right);

    card.appendChild(title);
    card.appendChild(meta);

    // quick stats row
    const statsRow = document.createElement('div');
    statsRow.style.display = 'flex';
    statsRow.style.justifyContent = 'space-between';
    statsRow.style.marginTop = '8px';
    statsRow.innerHTML = `
      <div><small class="muted">Avg speed</small><div>${act.average_speed ? (act.average_speed*3.6).toFixed(1)+' km/h' : '—'}</div></div>
      <div><small class="muted">Elev gain</small><div>${act.total_elevation_gain?act.total_elevation_gain+' m':'—'}</div></div>
    `;
    card.appendChild(statsRow);

    frag.appendChild(card);
    cardLink.appendChild(card);
    frag.appendChild(cardLink);
  });
  el.activities.appendChild(frag);
}

/* ------------- Data loading ------------- */
async function loadNextPage(){
  if (state.loading || state.reachedEnd) return;
  state.loading = true;
  showLoading(true);
  try {
    const resp = await API.listActivities(state.perPage, state.page, {
      type: state.filters.type,
      date_from: state.filters.date_from,
      date_to: state.filters.date_to,
      distance_min: state.filters.distance_min,
      q: state.filters.q,
      sort: state.sort
    });

    // resp assumed { activities: [...], total: N } or plain array
    const activities = Array.isArray(resp) ? resp : (resp.activities || []);
    if (activities.length === 0) {
      state.reachedEnd = true;
      el.noMore.classList.remove('hidden');
    } else {
      renderActivities(activities, true);
      state.activities.push(...activities);
      state.page += 1;
    }

    // update meta
    el.totalActivities.textContent = `Actividades (cargadas): ${state.activities.length}`;
    el.loadedPage.textContent = `Página: ${state.page - 1}`;
  } catch (err){
    console.error('Error cargando actividades', err);
    const mensaje = err.error || err.message || JSON.stringify(err);
    alert('Detalle del error: ' + mensaje);
    
    // Si el error es de autenticación, mandamos al login
    if (mensaje === "No autenticado" || mensaje.includes("Usuario no autenticado")) {
        window.location.href = './login.html';
    }
  } finally {
    state.loading = false;
    showLoading(false);
  }
}

function showLoading(v){
  if (v){
    el.loadingIndicator.classList.remove('hidden');
    el.loadMore.disabled = true;
  } else {
    el.loadingIndicator.classList.add('hidden');
    el.loadMore.disabled = false;
  }
}

/* ------------- Filters ------------- */
function applyFilters(){
  const type = el.filterType.value;
  const date_from = el.filterFrom.value || null;
  const date_to = el.filterTo.value || null;
  const distance_min = el.filterDistance.value ? parseFloat(el.filterDistance.value) : null;
  const q = el.filterSearch.value.trim() || null;

  state.filters = { type, date_from, date_to, distance_min, q };
  reloadAll();
}

function clearFilters(){
  el.filterType.value = '';
  el.filterFrom.value = '';
  el.filterTo.value = '';
  el.filterDistance.value = '';
  el.filterSearch.value = '';
  state.filters = {};
  reloadAll();
}

function reloadAll(){
  // reset
  state.page = 1;
  state.activities = [];
  state.reachedEnd = false;
  el.noMore.classList.add('hidden');
  el.activities.innerHTML = '';
  loadNextPage();
}

/* ------------- Activity click & modal ------------- */
async function onActivityClick(e){
  const card = e.target.closest('.activity-card');
  if (!card) return;
  const id = card.dataset.id;
  const activity = state.activities.find(a => String(a.id) === String(id));
  if (!activity) return;
  openActivityModal(activity);
}

let currentMap = null;
let currentCharts = [];

async function openActivityModal(activity){
  showModal();
  el.actTitle.textContent = activity.name || 'Actividad';
  el.actSubtitle.textContent = `${dateShort(activity.start_date_local)} • ${formatDistance(activity.distance||0)} • ${formatTime(activity.elapsed_time||0)}`;

  // basic stats
  el.activityStats.innerHTML = `
    <div class="stats-row"><strong>Tipo</strong> ${activity.type}</div>
    <div class="stats-row"><strong>Distancia</strong> ${formatDistance(activity.distance||0)}</div>
    <div class="stats-row"><strong>Tiempo</strong> ${formatTime(activity.moving_time || activity.elapsed_time || 0)}</div>
    <div class="stats-row"><strong>Desnivel</strong> ${activity.total_elevation_gain || 0} m</div>
    <div class="stats-row"><strong>Vel media</strong> ${activity.average_speed ? (activity.average_speed*3.6).toFixed(1)+' km/h' : '—'}</div>
  `;

  // hide raw JSON initially
  el.rawJson.classList.add('hidden');
  el.rawJson.textContent = '';

  // fetch streams
  try {
    const streams = await API.getStreams(activity.id);
    // streams may be either an object keyed by type or array of stream objects depending on key_by_type
    // handle both shapes
    let time = null, latlng = null, altitude = null, speed = null, heartrate = null, watts = null, cadence = null;
    if (Array.isArray(streams)) {
      // array of stream objects {type, data}
      streams.forEach(s => {
        const k = s.type || s.key || s.stream_type || s.stream_type;
        if (k === 'time' || k === 'time') time = s.data;
        if (k === 'latlng' || k === 'latlng') latlng = s.data;
        if (k === 'altitude' || k === 'altitude') altitude = s.data;
        if (k === 'velocity_smooth' || k === 'velocity_smooth') speed = s.data;
        if (k === 'heartrate' || k === 'heartrate') heartrate = s.data;
        if (k === 'watts' || k === 'watts') watts = s.data;
        if (k === 'cadence' || k === 'cadence') cadence = s.data;
      });
    } else {
      // object keyed by stream type
      time = streams.time?.data || streams.time;
      latlng = streams.latlng?.data || streams.latlng;
      altitude = streams.altitude?.data || streams.altitude;
      speed = streams.velocity_smooth?.data || streams.velocity_smooth;
      heartrate = streams.heartrate?.data || streams.heartrate;
      watts = streams.watts?.data || streams.watts;
      cadence = streams.cadence?.data || streams.cadence;
    }

    // render map
    renderMap(latlng);

    // render charts
    teardownCharts();
    renderCharts({
      time, altitude, speed, heartrate, watts, cadence
    });

    // raw json view
    el.rawJson.textContent = JSON.stringify({ activity, streams }, null, 2);
    el.rawToggle.onclick = () => el.rawJson.classList.toggle('hidden');

    // export one activity csv
    el.exportActivityCsv.onclick = () => {
      exportActivityCSV(activity, { time, latlng, altitude, speed, heartrate, watts, cadence });
    };

  } catch (err) {
    console.error('Error fetching streams', err);
    alert('Error al cargar datos de la actividad: ' + (err.message || err));
  }
}

function showModal(){ el.modal.classList.remove('hidden'); el.modal.setAttribute('aria-hidden','false'); }
function hideModal(){ el.modal.classList.add('hidden'); el.modal.setAttribute('aria-hidden','true'); }

/* ------------- Map (Leaflet) ------------- */
function renderMap(latlng){
  if (!latlng || !Array.isArray(latlng) || latlng.length === 0) {
    el.mapContainer.innerHTML = '<div style="padding:20px;color:#bbb">No hay datos GPS para esta actividad.</div>';
    return;
  }

  // clear existing map container
  el.mapContainer.innerHTML = '';

  // init map
  if (currentMap) {
    try { currentMap.remove(); } catch(e){}
    currentMap = null;
  }

  const map = L.map(el.mapContainer, { zoomControl: true, attributionControl: false }).setView([latlng[0][0], latlng[0][1]], 13);

  // tile layer (OpenStreetMap)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19
  }).addTo(map);

  // LatLng array is [[lat, lng], ...] according to Strava streams
  const latLngs = latlng.map(pair => [pair[0], pair[1]]);
  const poly = L.polyline(latLngs, { color: '#00ff88', weight: 4, opacity: 0.9 }).addTo(map);

  // fit bounds
  map.fitBounds(poly.getBounds(), { padding: [20,20] });

  // start and end markers
  L.circleMarker(latLngs[0], { radius:6, fillColor:'#00ff88', color:'#fff', weight:1 }).addTo(map).bindPopup('Start');
  L.circleMarker(latLngs[latLngs.length - 1], { radius:6, fillColor:'#ff5a5a', color:'#fff', weight:1 }).addTo(map).bindPopup('End');

  currentMap = map;
}

/* ------------- Charts (Chart.js) ------------- */
function teardownCharts(){
  currentCharts.forEach(c => c.destroy && c.destroy());
  currentCharts = [];
}

function renderCharts(streams){
  const { time, altitude, speed, heartrate, watts, cadence } = streams;
  // use index as x axis if time missing
  const labels = (time && time.length) ? time.map(t => (typeof t === 'number' ? Math.round(t) : t)) : (altitude||speed||heartrate||cadence||watts||[]).map((_,i)=>i);

  // elevation
  if (altitude && altitude.length){
    const ctx = document.getElementById('chart-elevation').getContext('2d');
    currentCharts.push(new Chart(ctx, {
      type: 'line',
      data: { labels, datasets: [{ label: 'Altitud (m)', data: altitude, fill:true, tension:0.15 }]},
      options: { plugins:{legend:{display:false}}, scales:{x:{display:false}} }
    }));
  } else {
    document.getElementById('chart-elevation').style.display = 'none';
  }

  // speed -> convert m/s to km/h
  if (speed && speed.length){
    const speedKmh = speed.map(s => s*3.6);
    const ctx2 = document.getElementById('chart-speed').getContext('2d');
    currentCharts.push(new Chart(ctx2, {
      type: 'line',
      data: { labels, datasets: [{ label: 'Velocidad (km/h)', data: speedKmh, tension:0.12 }]},
      options: { plugins:{legend:{display:false}}, scales:{x:{display:false}} }
    }));
  } else {
    document.getElementById('chart-speed').style.display = 'none';
  }

  // heart rate / cadence / watts overlay small
  const hrAvailable = heartrate && heartrate.length;
  const cadenceAvailable = cadence && cadence.length;
  const wattsAvailable = watts && watts.length;

  if (hrAvailable || cadenceAvailable || wattsAvailable){
    const datasets = [];
    if (hrAvailable) datasets.push({ label:'HR', data: heartrate, tension:0.12 });
    if (cadenceAvailable) datasets.push({ label:'Cadence', data: cadence, tension:0.12 });
    if (wattsAvailable) datasets.push({ label:'Watts', data: watts, tension:0.12 });

    const ctx3 = document.getElementById('chart-hr').getContext('2d');
    currentCharts.push(new Chart(ctx3, {
      type: 'line',
      data: { labels, datasets },
      options: { plugins:{legend:{display:true}}, scales:{x:{display:false}} }
    }));
  } else {
    document.getElementById('chart-hr').style.display = 'none';
  }
}

/* ------------- CSV Export helpers ------------- */
function exportActivitiesCSV(activities){
  if (!activities || activities.length === 0) return alert('No hay actividades cargadas');
  const headers = ['id','name','type','start_date_local','distance_m','elapsed_time_s','elev_gain_m','avg_speed_m_s'];
  const rows = activities.map(a => [
    a.id, a.name, a.type, a.start_date_local, a.distance || '', a.elapsed_time || '', a.total_elevation_gain || '', a.average_speed || ''
  ]);
  downloadCSV([headers, ...rows], 'activities_export.csv');
}

function exportActivityCSV(activity, streams){
  // create rows combining time, latlng, altitude, speed, hr, watts, cadence
  const { time, latlng, altitude, speed, heartrate, watts, cadence } = streams || {};
  const rows = [];
  const headers = ['index','time_s','lat','lng','altitude_m','speed_m_s','heartrate','watts','cadence'];
  const length = Math.max(
    time?.length||0,
    latlng?.length||0,
    altitude?.length||0,
    speed?.length||0,
    heartrate?.length||0,
    watts?.length||0,
    cadence?.length||0
  );
  for (let i=0;i<length;i++){
    const lat = latlng && latlng[i] ? latlng[i][0] : '';
    const lng = latlng && latlng[i] ? latlng[i][1] : '';
    rows.push([i, time?.[i] ?? '', lat, lng, altitude?.[i] ?? '', speed?.[i] ?? '', heartrate?.[i] ?? '', watts?.[i] ?? '', cadence?.[i] ?? '']);
  }
  downloadCSV([headers, ...rows], `activity_${activity.id}.csv`);
}

function downloadCSV(table, filename){
  const csv = table.map(r => r.map(c => `"${String(c ?? '').replace(/"/g,'""')}"`).join(',')).join('\n');
  const blob = new Blob([csv], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

/* ------------- Init ------------- */
function init(){
  // initial load
  reloadAll();

  // keyboard escape closes modal
  window.addEventListener('keydown', e => {
    if (e.key === 'Escape' && !el.modal.classList.contains('hidden')) hideModal();
  });

  // logout simple
  document.getElementById('logout').addEventListener('click', async () => {
    // call backend logout if exists
    try {
      await fetch('./logout', { method: 'POST', credentials: 'same-origin' });
    } catch (e) {}
    window.location.href = '/login.html';
  });
}

init();
