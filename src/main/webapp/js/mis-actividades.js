// Define un objeto global API que encapsula las funciones para interactuar con los endpoints del backend (servlets).
const API = {
  /**
   * Llama al endpoint de la API para listar actividades, aplicando paginación y filtros.
   * @param {number} perPage - Cantidad de elementos por página.
   * @param {number} page - Número de página.
   * @param {object} filters - Objeto con los criterios de filtro (type, date_from, etc.).
   * @returns {Promise<Array>} Una promesa que resuelve con la lista de actividades.
   */
  listActivities: (perPage = 30, page = 1, filters = {}) => {
    // Construye los parámetros de la URL.
    const params = new URLSearchParams();
    params.set('per_page', perPage);
    params.set('page', page);

    // Añade filtros si están presentes.
    if (filters.type) params.set('type', filters.type);
    if (filters.date_from) params.set('date_from', filters.date_from);
    if (filters.date_to) params.set('date_to', filters.date_to);
    if (filters.distance_min) params.set('distance_min', filters.distance_min);
    if (filters.q) params.set('q', filters.q);
    if (filters.sort) params.set('sort', filters.sort);

    // Realiza la llamada fetch al servlet /api/activities. 'credentials: same-origin' asegura que se envíen las cookies de sesión.
    return fetch(`./api/activities?${params.toString()}`, { credentials: 'same-origin' })
      .then(checkJson); // Procesa la respuesta JSON y maneja errores HTTP.
  },
  
  /**
   * Llama al endpoint de la API para obtener los streams (datos detallados: latlng, watts, etc.) de una actividad.
   * @param {number|string} activityId - El ID de la actividad de Strava.
   * @param {string} keys - Lista de streams solicitados separados por comas.
   * @param {boolean} keyByType - Si se deben agrupar los resultados por tipo de stream.
   * @returns {Promise<object>} Una promesa que resuelve con los datos de los streams.
   */
  getStreams: (activityId, keys = 'time,latlng,altitude,velocity_smooth,heartrate,watts,cadence', keyByType = true) => {
    // Construye los parámetros de la URL.
    const params = new URLSearchParams({ keys, key_by_type: keyByType ? 'true' : 'false' });
    params.set('d', activityId); // Parámetro para el ID de la actividad (usado en StreamsServlet como 'id').
    
    // Realiza la llamada fetch al servlet /api/streams.
    return fetch(`./api/streams?${params.toString()}`, { credentials: 'same-origin' })
      .then(checkJson);
  }
};

/**
 * Función helper para verificar la respuesta HTTP. Lanza un error si la respuesta no es OK (status 200-299).
 * @param {Response} res - La respuesta fetch.
 * @returns {Promise<object>} El cuerpo JSON de la respuesta.
 */
function checkJson(res){
  if (!res.ok) {
    // Intenta leer el cuerpo del error como JSON para dar más detalles.
    return res.json().catch(()=>{ throw new Error(`HTTP ${res.status}`) }).then(err => { throw err; });
  }
  return res.json();
}

// Objeto que gestiona el estado de la aplicación en el frontend.
let state = {
  perPage: 30,
  page: 1,
  loading: false,       // Indica si se está cargando datos actualmente.
  reachedEnd: false,    // Indica si se han cargado todas las actividades disponibles.
  activities: [],       // Array donde se almacenan las actividades cargadas.
  filters: {},          // Objeto que contiene los filtros aplicados actualmente.
  sort: 'start_date_local_desc' // Criterio de ordenación por defecto.
};

// Mapeo de elementos del DOM para un acceso más sencillo.
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
  refreshBtn: document.getElementById('refresh-all')
};

// --- Listeners de Eventos ---
el.loadMore.addEventListener('click', () => loadNextPage());
el.applyFilters.addEventListener('click', () => applyFilters());
el.clearFilters.addEventListener('click', () => clearFilters());
el.exportCsv.addEventListener('click', () => exportActivitiesCSV(state.activities));
el.refreshBtn.addEventListener('click', ()=> reloadAll());

// Listener para el campo de búsqueda con un retardo (debounce) para no disparar peticiones con cada tecla.
let searchTimeout = null;
el.filterSearch.addEventListener('input', (e)=>{
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(()=> { applyFilters(); }, 600);
});

// --- Funciones de Formateo de Datos ---
function formatDistance(m){ return (m/1000).toFixed(2)+' km'; }
function formatTime(s){ const h=Math.floor(s/3600), m=Math.floor((s%3600)/60), sec=s%60; return `${h>0?h+'h ':''}${m}m ${sec}s`; }
function dateShort(d){ return new Date(d).toLocaleString(); }

/**
 * Renderiza la lista de actividades en el DOM.
 * @param {Array<object>} list - Lista de actividades a renderizar.
 * @param {boolean} append - Si es true, añade al final; si es false, reemplaza la lista existente.
 */
function renderActivities(list, append=true){
  if (!append) el.activities.innerHTML='';
  const frag = document.createDocumentFragment();
  list.forEach(act=>{
    // Crea un enlace que envuelve la tarjeta de actividad, apuntando a la página de detalle.
    const cardLink=document.createElement('a');
    cardLink.className='activity-card-link';
    cardLink.href=`actividad.html?id=${act.id}`; // URL para ver detalles/análisis.
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

/**
 * Carga la siguiente página de actividades de la API, las renderiza y actualiza el estado.
 */
async function loadNextPage(){
  if (state.loading || state.reachedEnd) return;
  state.loading=true;
  showLoading(true);
  try {
    // Llama al servicio de API con los parámetros actuales de paginación y filtro.
    const resp = await API.listActivities(state.perPage, state.page, {...state.filters, sort: state.sort});
    const activities = Array.isArray(resp) ? resp : (resp.activities||[]);
    
    if (activities.length===0){ 
      state.reachedEnd=true; 
      el.noMore.classList.remove('hidden'); // Muestra mensaje de "No hay más actividades".
    }
    else { 
      renderActivities(activities,true); 
      state.activities.push(...activities); 
      state.page+=1; 
    }

    // Actualiza la UI con estadísticas de carga.
    el.totalActivities.textContent=`Actividades (cargadas): ${state.activities.length}`;
    el.loadedPage.textContent=`Página: ${state.page-1}`;
  } catch(err){
    console.error('Error cargando actividades',err);
    const mensaje=err.error||err.message||JSON.stringify(err);
    alert('Detalle del error: '+mensaje);
    // Si el error es de autenticación, redirige al login.
    if (mensaje==="No autenticado"||mensaje.includes("Usuario no autenticado")) window.location.href='./login.html';
  } finally { 
    state.loading=false; 
    showLoading(false); // Oculta el indicador de carga.
  }
}

/**
 * Muestra u oculta los indicadores de carga y deshabilita el botón "Cargar más".
 * @param {boolean} v - True para mostrar carga, false para ocultar.
 */
function showLoading(v){ 
    if(v){ el.loadingIndicator.classList.remove('hidden'); el.loadMore.disabled=true; } 
    else { el.loadingIndicator.classList.add('hidden'); el.loadMore.disabled=false; } 
}

/**
 * Recoge los valores de los inputs de filtro, actualiza el estado y recarga todas las actividades.
 */
function applyFilters(){
  const type=el.filterType.value;
  const date_from=el.filterFrom.value||null;
  const date_to=el.filterTo.value||null;
  const distance_min=el.filterDistance.value ? parseFloat(el.filterDistance.value) : null;
  const q=el.filterSearch.value.trim()||null;

  state.filters={type,date_from,date_to,distance_min,q};
  reloadAll();
}

/**
 * Limpia los inputs de filtro, reinicia el estado de filtros y recarga todas las actividades.
 */
function clearFilters(){
  el.filterType.value=''; el.filterFrom.value=''; el.filterTo.value=''; el.filterDistance.value=''; el.filterSearch.value='';
  state.filters={};
  reloadAll();
}

/**
 * Resetea el estado de paginación y carga de actividades para iniciar desde cero con los filtros actuales.
 */
function reloadAll(){ 
  state.page=1; 
  state.activities=[]; 
  state.reachedEnd=false; 
  el.noMore.classList.add('hidden'); 
  el.activities.innerHTML=''; 
  loadNextPage(); 
}

/**
 * Toma los datos de actividades cargadas y genera un archivo CSV que se descarga automáticamente.
 * @param {Array<object>} activities - La lista de actividades a exportar.
 */
function exportActivitiesCSV(activities){
  if(!activities||activities.length===0)return alert('No hay actividades cargadas');
  // Define las cabeceras del CSV y mapea los datos de las actividades a filas CSV.
  const headers=['id','name','type','start_date_local','distance_m','elapsed_time_s','elev_gain_m','avg_speed_m_s'];
  const rows=activities.map(a=>[a.id,a.name,a.type,a.start_date_local,a.distance||'',a.elapsed_time||'',a.total_elevation_gain||'',a.average_speed||'']);
  downloadCSV([headers,...rows],'activities_export.csv');
}

/**
 * Helper para crear y disparar la descarga de un archivo CSV a partir de un array 2D.
 * @param {Array<Array<any>>} table - Los datos tabulares.
 * @param {string} filename - Nombre del archivo a descargar.
 */
function downloadCSV(table,filename){
  // Formatea la tabla como una cadena CSV válida (manejo básico de comillas).
  const csv=table.map(r=>r.map(c=>`"${String(c??'').replace(/"/g,'""')}"`).join(',')).join('\n');
  const blob=new Blob([csv],{type:'text/csv'});
  const url=URL.createObjectURL(blob);
  const a=document.createElement('a');
  a.href=url;
  a.download=filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url); // Limpia la URL del objeto Blob.
}

/**
 * Función de inicialización principal que se ejecuta al cargar la página.
 */
function init(){ 
  reloadAll(); // Carga la primera página de actividades.
  // Listener básico para teclas (actualmente vacío).
  window.addEventListener('keydown',e=>{ if(e.key==='Escape'){} }); 
  
  // Listener para el botón de logout.
  document.getElementById('logout').addEventListener('click',async()=>{ 
    try{ 
      // Llama al servlet de logout mediante POST.
      await fetch('./logout',{method:'POST',credentials:'same-origin'}); 
    } catch(e){
      // El error de fetch podría ocurrir si la sesión ya expiró, pero la redirección debe ocurrir igual.
    } 
    // Redirige al login después del logout (exitoso o fallido en el fetch).
    window.location.href='./login.html'; 
  }); 
}

// Ejecuta la función de inicialización.
init();
