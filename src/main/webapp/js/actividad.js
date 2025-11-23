// webapp/js/actividad.js - VERSIÓN FINAL COMPLETA Y CORREGIDA

document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const activityId = params.get('id');

    if (!activityId) {
        alert("No se ha especificado una actividad.");
        window.location.href = 'mis-actividades.html'; return;
    }

    try {
        const response = await fetch(`./api/activity-detail?id=${activityId}`);
        if (response.status === 401) { window.location.href = 'login.html'; return; }
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `Error del servidor (${response.status})`);
        }
        
        const data = await response.json(); // { strava_activity, analytics, streams }
        if (!data.strava_activity) throw new Error("Datos de actividad incompletos");

        // Renderizamos la cabecera (título, fecha, etc.)
        renderHeader(data.strava_activity);
        
        // Renderizamos las métricas (avanzadas o básicas)
        renderMetrics(data.analytics, data.strava_activity);
        
        // Extraemos datos de streams de forma segura
        const latlngData = extractStreamData(data.streams?.latlng);
        const timeData = extractStreamData(data.streams?.time);
        const cadenceData = extractStreamData(data.streams?.cadence);

        // Renderizamos el mapa
        renderMap(latlngData);
        // Renderizamos la gráfica de cadencia
        renderCadenceChart(timeData, cadenceData);

    } catch (error) {
        console.error("Error crítico:", error);
        showErrorState(error.message);
    }
});

// --- Funciones Auxiliares ---

// Extrae el array de datos de un stream, venga como venga (directo o dentro de un objeto .data)
function extractStreamData(streamObj) {
    if (!streamObj) return null;
    if (Array.isArray(streamObj)) return streamObj;
    if (streamObj.data && Array.isArray(streamObj.data)) return streamObj.data;
    return null;
}

// Formatea números con decimales opcionales
function formatNumber(value, decimals = 0) {
    if (value === null || value === undefined || isNaN(value)) return null;
    return Number(value).toFixed(decimals);
}

// Formatea segundos a horas:minutos
function formatTime(seconds) {
    if (!seconds) return null;
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return `${h > 0 ? h + 'h ' : ''}${m}m`;
}

// Muestra estado de error en la interfaz
function showErrorState(message) {
    document.getElementById('act-title').textContent = "Error cargando actividad";
    document.getElementById('act-meta').innerHTML = `<span style="color:#ff5a5a;">${message}</span>`;
    document.getElementById('metrics-container').innerHTML = `<div class="muted">No se pudieron cargar los datos.</div>`;
    document.querySelector('.visuals-column').style.opacity = '0.5';
}


// --- Funciones de Renderizado (AHORA SÍ ESTÁN COMPLETAS) ---

function renderHeader(stravaData) {
    document.getElementById('act-title').textContent = stravaData.name || "Sin título";
    
    const date = new Date(stravaData.start_date_local).toLocaleString();
    const dist = (stravaData.distance / 1000).toFixed(2) + ' km';
    const timeStr = formatTime(stravaData.moving_time || stravaData.elapsed_time);
    const elev = stravaData.total_elevation_gain ? `${stravaData.total_elevation_gain} m` : '—';

    document.getElementById('act-meta').innerHTML = `
        <span>📅 ${date}</span> • <span>📏 ${dist}</span> • <span>⏱️ ${timeStr}</span> • <span>⛰️ ${elev}</span>
    `;
}

function renderMetrics(analytics, stravaData) {
    const container = document.getElementById('metrics-container');
    container.innerHTML = '';

    let metrics = [];
    let title = 'Métricas';

    // Si hay análisis avanzado con datos válidos (ej. tiene carga calculada), lo usamos
    if (analytics && typeof analytics.load === 'number') {
        title = 'Análisis Avanzado (Cycling Analytics)';
        metrics = [
            { label: 'Carga (TSS)', value: formatNumber(analytics.load), unit: '' },
            { label: 'Intensidad (IF)', value: formatNumber(analytics.intensity, 2), unit: '' },
            { label: 'Variabilidad (VI)', value: formatNumber(analytics.variability, 2), unit: '' },
            { label: 'Pot. Normalizada', value: formatNumber(analytics.epower), unit: 'W' },
            { label: 'Trabajo', value: formatNumber(analytics.work), unit: 'kJ' }
        ];
    } else {
        // Si no, mostramos más métricas básicas de Strava
        title = 'Métricas Básicas (Strava)';
        metrics = [
             { label: 'Velocidad Media', value: formatNumber(stravaData.average_speed ? stravaData.average_speed * 3.6 : null, 1), unit: 'km/h' },
             { label: 'Velocidad Máx.', value: formatNumber(stravaData.max_speed ? stravaData.max_speed * 3.6 : null, 1), unit: 'km/h' },
             { label: 'Cadencia Media', value: formatNumber(stravaData.average_cadence), unit: 'rpm' },
             { label: 'Potencia Media', value: formatNumber(stravaData.average_watts), unit: 'W' },
             { label: 'Desnivel +', value: formatNumber(stravaData.total_elevation_gain), unit: 'm' },
             { label: 'Tiempo Mov.', value: formatTime(stravaData.moving_time), unit: '' },
             { label: 'Calorías', value: formatNumber(stravaData.calories), unit: 'kcal' }
         ];
    }

    // Actualizamos el título de la sección
    const metricsTitle = document.querySelector('.metrics-panel h2');
    if (metricsTitle) metricsTitle.textContent = title;

    appendMetricsCards(container, metrics);
}

function appendMetricsCards(container, metricsArray) {
    let cardsAdded = false;
    metricsArray.forEach(m => {
        if (m.value !== null) {
            const card = document.createElement('div');
            card.className = 'metric-card';
            card.innerHTML = `<span class="metric-label">${m.label}</span><span class="metric-value">${m.value}</span><span class="metric-unit">${m.unit}</span>`;
            container.appendChild(card);
            cardsAdded = true;
        }
    });
    if (!cardsAdded) container.innerHTML += '<p class="muted">No hay métricas disponibles.</p>';
}

function renderMap(latlngData) {
    const mapContainer = document.getElementById('map-detail');
    if (!latlngData || latlngData.length === 0) {
        mapContainer.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#9aa4ad;">No hay datos GPS disponibles.</div>';
        return;
    }

    mapContainer.innerHTML = '';

    const map = L.map(mapContainer).setView(latlngData[0], 13);
    
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        attribution: '©OpenStreetMap, ©CartoDB', maxZoom: 19, subdomains: 'abcd'
    }).addTo(map);

    const polyline = L.polyline(latlngData, { color: '#00ff88', weight: 4 }).addTo(map);
    map.fitBounds(polyline.getBounds(), { padding: [30, 30] });

    setTimeout(() => { map.invalidateSize(); }, 250);
}

function renderCadenceChart(timeData, cadenceData) {
    const ctx = document.getElementById('power-curve-chart').getContext('2d');
    const chartContainer = ctx.canvas.parentNode;

    if (!timeData || timeData.length === 0 || !cadenceData || cadenceData.length === 0) {
        chartContainer.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#9aa4ad;">No hay datos de cadencia disponibles.</div>';
        return;
    }

    if (window.myChart instanceof Chart) window.myChart.destroy();

    window.myChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: timeData,
            datasets: [{
                label: 'Cadencia (rpm)',
                data: cadenceData,
                borderColor: '#00d2ff', // Color cian para cadencia
                backgroundColor: 'rgba(0, 210, 255, 0.1)',
                borderWidth: 1,
                fill: true,
                pointRadius: 0, pointHoverRadius: 4, tension: 0.2
            }]
        },
        options: {
            responsive: true, maintainAspectRatio: false, animation: { duration: 800 },
            interaction: { mode: 'index', intersect: false },
            scales: {
                x: {
                    type: 'linear',
                    grid: { color: 'rgba(255,255,255,0.05)', borderColor: 'rgba(255,255,255,0.1)' },
                    ticks: { 
                        color: '#9aa4ad', maxTicksLimit: 8,
                        callback: function(value) { const m = Math.floor(value / 60); return m + ' min'; }
                    },
                    title: { display: true, text: 'Tiempo (min)', color: '#9aa4ad' }
                },
                y: {
                    grid: { color: 'rgba(255,255,255,0.05)', borderColor: 'rgba(255,255,255,0.1)' },
                    ticks: { color: '#9aa4ad', maxTicksLimit: 6 },
                    title: { display: true, text: 'rpm', color: '#9aa4ad' },
                    beginAtZero: true
                }
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#0f1724', titleColor: '#00d2ff', bodyColor: '#fff', borderColor: 'rgba(255,255,255,0.1)', borderWidth: 1,
                    callbacks: {
                        title: (context) => { const sec = context[0].parsed.x; const m = Math.floor(sec / 60); const s = sec % 60; return `Tiempo: ${m}m ${s}s`; }
                    }
                }
            }
        }
    });
}