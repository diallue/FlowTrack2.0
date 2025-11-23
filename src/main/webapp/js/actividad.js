// webapp/js/actividad.js

document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const activityId = params.get('id');

    if (!activityId) {
        alert("No se ha especificado una actividad.");
        window.location.href = 'mis-actividades.html';
        return;
    }

    try {
        // Llamada al backend
        const response = await fetch(`./api/activity-detail?id=${activityId}`);
        
        if (response.status === 401) {
            window.location.href = 'login.html'; return;
        }
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `Error del servidor (${response.status})`);
        }
        
        const data = await response.json(); // Estructura: { strava_activity, analytics, streams }

        if (!data.strava_activity) throw new Error("Datos de actividad incompletos");

        // Renderizado
        renderHeader(data.strava_activity);
        renderMetrics(data.analytics, data.strava_activity);
        
        // Importante: Pasamos los objetos completos de stream (ej. {type:..., data:[...]})
        renderMap(data.streams?.latlng);
        renderPowerChart(data.streams?.time, data.streams?.watts);

    } catch (error) {
        console.error("Error en actividad.js:", error);
        showErrorState(error.message);
    }
});

// --- Funciones Auxiliares ---
function formatNumber(value, decimals = 0) {
    if (value === null || value === undefined || isNaN(value)) return null;
    return Number(value).toFixed(decimals);
}

function showErrorState(message) {
    document.getElementById('act-title').textContent = "Error cargando actividad";
    document.getElementById('act-meta').innerHTML = `<span style="color:#ff5a5a;">${message}</span>`;
    document.getElementById('metrics-container').innerHTML = `<div class="muted">No se pudieron cargar los datos.</div>`;
    document.querySelector('.visuals-column').style.opacity = '0.5';
}

// --- Funciones de Renderizado ---

function renderHeader(stravaData) {
    document.getElementById('act-title').textContent = stravaData.name || "Sin título";
    
    const date = new Date(stravaData.start_date_local).toLocaleString();
    const dist = (stravaData.distance / 1000).toFixed(2) + ' km';
    
    const durationSeconds = stravaData.moving_time || stravaData.elapsed_time || 0;
    const h = Math.floor(durationSeconds / 3600);
    const m = Math.floor((durationSeconds % 3600) / 60);
    const timeStr = `${h > 0 ? h + 'h ' : ''}${m}m`;
    
    const elev = stravaData.total_elevation_gain ? `${stravaData.total_elevation_gain} m` : '—';

    document.getElementById('act-meta').innerHTML = `
        <span>📅 ${date}</span> • <span>📏 ${dist}</span> • <span>⏱️ ${timeStr}</span> • <span>⛰️ ${elev}</span>
    `;
}

function renderMetrics(analytics, stravaData) {
    const container = document.getElementById('metrics-container');
    container.innerHTML = '';

    if (!analytics) {
        container.innerHTML = `
            <div class="metric-card" style="grid-column: 1 / -1; text-align: left; background: transparent; border: none;">
                <p class="muted" style="margin:0; margin-bottom: 15px;">
                    Análisis avanzado no disponible. Mostrando datos básicos:
                </p>
            </div>
        `;
        const basicMetrics = [
             { label: 'Potencia Media', value: formatNumber(stravaData.average_watts), unit: 'W' },
             { label: 'Potencia Máx.', value: formatNumber(stravaData.max_watts), unit: 'W' },
             { label: 'Velocidad Media', value: formatNumber(stravaData.average_speed ? stravaData.average_speed * 3.6 : null, 1), unit: 'km/h' },
             { label: 'Cadencia Media', value: formatNumber(stravaData.average_cadence), unit: 'rpm' },
             { label: 'Calorías', value: formatNumber(stravaData.calories), unit: 'kcal' }
         ];
         appendMetricsCards(container, basicMetrics);
         return;
    }

    // (Lógica futura para analytics)
}

function appendMetricsCards(container, metricsArray) {
    let cardsAdded = false;
    metricsArray.forEach(m => {
        if (m.value !== null) {
            const card = document.createElement('div');
            card.className = 'metric-card';
            card.innerHTML = `
                <span class="metric-label">${m.label}</span>
                <span class="metric-value">${m.value}</span>
                <span class="metric-unit">${m.unit}</span>
            `;
            container.appendChild(card);
            cardsAdded = true;
        }
    });
    if (!cardsAdded) container.innerHTML += '<p class="muted">No hay métricas básicas disponibles.</p>';
}

function renderMap(latlngStream) {
    const mapContainer = document.getElementById('map-detail');
    // Verificación robusta
    if (!latlngStream || !latlngStream.data || !Array.isArray(latlngStream.data) || latlngStream.data.length === 0) {
        mapContainer.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#9aa4ad;">No hay datos GPS disponibles.</div>';
        return;
    }

    mapContainer.innerHTML = ''; // Limpiar

    // Crear mapa
    const map = L.map(mapContainer).setView(latlngStream.data[0], 13);
    
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        attribution: '©OpenStreetMap, ©CartoDB', maxZoom: 19, subdomains: 'abcd'
    }).addTo(map);

    const polyline = L.polyline(latlngStream.data, { color: '#00ff88', weight: 4 }).addTo(map);
    map.fitBounds(polyline.getBounds(), { padding: [30, 30] });

    // CRÍTICO: Forzar redibujado del mapa por problemas de CSS grid
    setTimeout(() => { 
        map.invalidateSize(); 
    }, 200);
}

function renderPowerChart(timeStream, wattsStream) {
    const ctx = document.getElementById('power-curve-chart').getContext('2d');
    const chartContainer = ctx.canvas.parentNode;

    // Verificación de datos de potencia y tiempo
    if (!timeStream || !timeStream.data || !wattsStream || !wattsStream.data || timeStream.data.length === 0) {
        chartContainer.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#9aa4ad;">No hay datos de potencia disponibles.</div>';
        return;
    }

    const timeData = timeStream.data;
    const wattsData = wattsStream.data;

    if (window.myPowerChart instanceof Chart) window.myPowerChart.destroy();

    window.myPowerChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: timeData, // Eje X: segundos
            datasets: [{
                label: 'Potencia (Watts)',
                data: wattsData, // Eje Y: watts
                borderColor: '#00ff88',
                backgroundColor: 'rgba(0, 255, 136, 0.1)',
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
                    title: { display: true, text: 'Watts', color: '#9aa4ad' },
                    beginAtZero: true
                }
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#0f1724', titleColor: '#00ff88', bodyColor: '#fff', borderColor: 'rgba(255,255,255,0.1)', borderWidth: 1,
                    callbacks: {
                        title: (context) => { const sec = context[0].parsed.x; const m = Math.floor(sec / 60); const s = sec % 60; return `Tiempo: ${m}m ${s}s`; }
                    }
                }
            }
        }
    });
}