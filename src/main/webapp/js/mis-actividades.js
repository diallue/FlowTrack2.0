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
document.getElementById('activities').addEventListener('click', onActivityClick);

// Debounce search input
let searchTimeout = null;
el.filterSearch.addEventListener('input', (e)=>{
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(()=> {
    applyFilters();
  }, 600);
});

/* ---------------------------------------------------------
   ★ FUNCIÓN NUEVA: carga TODAS LAS PÁGINAS del backend
--------------------------------------------------------- */
async function loadAllActivitiesFromStrava(filters = {}) {
  let page = 1;
  const perPage = 50;
  let all = [];
  let keepGoing = true;

  while (keepGoing) {
    const resp = await API.listActivities(perPage, page, filters);
    const activities = Array.isArray(resp) ? resp : (resp.activities || []);

    all = all.concat(activities);

    if (activities.length < perPage) {
      keepGoing = false;
    } else {
      page++;
    }
  }

  return all;
}

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
    cardLink.className = 'activity-card-link';
    cardLink.href = `actividad.html?id=${act.id}`;
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

    const statsRow = document.createElement('div');
    statsRow.style.display = 'flex';
    statsRow.style.justifyContent = 'space-between';
    statsRow.style.marginTop = '8px';
    statsRow.innerHTML = `
      <div><small class="muted">Avg speed</small><div>${act.average_speed ? (act.average_speed*3.6).toFixed(1)+' km/h' : '—'}</div></div>
      <div><small class="muted">Elev gain</small><div>${act.total_elevation_gain?act.total_elevation_gain+' m':'—'}</div></div>
    `;
    card.appendChild(statsRow);

    cardLink.appendChild(card);
    frag.appendChild(cardLink);
  });
  el.activities.appendChild(frag);
}

/* ---------------------------------------------------------
   ★ MODIFICADO PARA USAR FILTROS DEL SERVIDOR
--------------------------------------------------------- */
async function loadNextPage(){
  if (state.loading || state.reachedEnd) return;
  state.loading = true;
  showLoading(true);
  try {
    const resp = await API.listActivities(state.perPage, state.page, {
      ...state.filters,
      sort: state.sort
    });

    const activities = Array.isArray(resp) ? resp : (resp.activities || []);
    if (activities.length === 0) {
      state.reachedEnd = true;
      el.noMore.classList.remove('hidden');
    } else {
      renderActivities(activities, true);
      state.activities.push(...activities);
      state.page += 1;
    }

    el.totalActivities.textContent = `Actividades (cargadas): ${state.activities.length}`;
    el.loadedPage.textContent = `Página: ${state.page - 1}`;
  } catch (err){
    console.error('Error cargando actividades', err);
    alert('Detalle del error: ' + (err.message || err));
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

/* ---------------------------------------------------------
   ★ APLICAR FILTROS: ahora recarga TODOS los datos
--------------------------------------------------------- */
async function applyFilters(){
  const type = el.filterType.value;
  const date_from = el.filterFrom.value || null;
  const date_to = el.filterTo.value || null;
  const distance_min = el.filterDistance.value ? parseFloat(el.filterDistance.value) : null;
  const q = el.filterSearch.value.trim() || null;

  state.filters = { type, date_from, date_to, distance_min, q };

  // Ocultar botón de paginación mientras se usan filtros
  el.loadMore.classList.add("hidden");

  // ★ Cargar TODAS las actividades ya filtradas desde el backend
  el.activities.innerHTML = '';
  el.loadingIndicator.classList.remove('hidden');

  const all = await loadAllActivitiesFromStrava({
    ...state.filters,
    sort: state.sort
  });

  state.activities = all;
  state.page = 1;
  state.reachedEnd = true;

  renderActivities(all, false);

  el.totalActivities.textContent = `Actividades (cargadas): ${all.length}`;
  el.loadedPage.textContent = `Página: Todas`;

  el.loadingIndicator.classList.add('hidden');
}

/* ---------------------------------------------------------
   ★ LIMPIAR FILTROS: vuelve a paginación normal
--------------------------------------------------------- */
function clearFilters(){
  el.filterType.value = '';
  el.filterFrom.value = '';
  el.filterTo.value = '';
  el.filterDistance.value = '';
  el.filterSearch.value = '';
  state.filters = {};

  el.loadMore.classList.remove("hidden"); // volver a mostrar paginación

  reloadAll();
}

/* ---------------------------------------------------------
   ★ RECARGA NORMAL (sin filtros)
--------------------------------------------------------- */
function reloadAll(){
  state.page = 1;
  state.activities = [];
  state.reachedEnd = false;
  el.noMore.classList.add('hidden');
  el.activities.innerHTML = '';
  loadNextPage();
}

/* ---------------------------------------------------------
   RESTO DEL CÓDIGO IGUAL (modal, mapas, CSV, charts…)
--------------------------------------------------------- */

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

// (todo el resto sigue igual, no lo toco por falta de espacio)

function init(){
  reloadAll();

  window.addEventListener('keydown', e => {
    if (e.key === 'Escape' && !el.modal.classList.contains('hidden')) hideModal();
  });

  document.getElementById('logout').addEventListener('click', async () => {
    try {
      await fetch('./logout', { method: 'POST', credentials: 'same-origin' });
    } catch (e) {}
    window.location.href = './login.html';
  });
}

init();
