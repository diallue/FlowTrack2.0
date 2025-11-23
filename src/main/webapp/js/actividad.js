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
        // 1. Llamar a nuestro Backend API
        // Si el backend Java falla, lanzará una excepción que capturaremos abajo.
        const response = await fetch(`./api/activity-detail?id=${activityId}`);
        
        if (response.status === 401) {
            // Sesión caducada
            window.location.href = 'login.html';
            return;
        }
        
        if (!response.ok) {
             // Intentamos leer el mensaje de error del servidor si existe
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `Error del servidor (${response.status})`);
        }
        
        const data = await response.json();
        // data tiene la estructura: { strava_activity: {}, analytics: null, streams: {} }

        // Renderizamos las partes. Si falta strava_activity, es un error grave.
        if (!data.strava_activity) throw new Error("Datos de actividad incompletos");

        renderHeader(data.strava_activity);
        
        // Renderizamos métricas (manejará el caso de analytics null internamente)
        renderMetrics(data.analytics, data.strava_activity);
        
        // Renderizamos el mapa si hay datos de latlng
        renderMap(data.streams?.latlng);
        
        // NUEVO: Renderizamos la gráfica usando los streams crudos de tiempo y watts
        renderPowerChart(data.streams?.time, data.streams?.watts);

    } catch (error) {
        console.error("Error en actividad.js:", error);
        // Mostramos el error en la interfaz de usuario de forma amigable
        document.getElementById('act-title').textContent = "Error cargando actividad";
        document.getElementById('act-meta').innerHTML = `<span style="color:#ff5a5a;">${error.message}</span>`;
        document.getElementById('metrics-container').innerHTML = `<div class="muted">No se pudieron cargar los datos.</div>`;
        document.querySelector('.visuals-column').style.opacity = '0.5'; // Indicador visual de error
    }
});

// --- Funciones de Renderizado ---

function renderHeader(stravaData) {
    document.getElementById('act-title').textContent = stravaData.name || "Sin título";
    
    const date = new Date(stravaData.start_date_local).toLocaleString();
    const dist = (stravaData.distance / 1000).toFixed(2) + ' km';
    
    // Cálculo de tiempo: si hay moving_time úsalo, si no elapsed_time
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
    container.innerHTML = ''; // Limpiar placeholder

    // --- CAMBIO PRINCIPAL: Si no hay analytics, mostramos un mensaje y datos básicos de Strava ---
    if (!analytics) {
        container.innerHTML = `
            <div class="metric-card" style="grid-column: 1 / -1; text-align: left; background: transparent; border: none;">
                <p class="muted" style="margin:0;">
                    Análisis avanzado no disponible actualmente. <br>
                    Mostrando datos básicos de Strava.
                </p>
            </div>
        `;
        // Añadimos métricas básicas que sí tenemos de Strava
         const basicMetrics = [
             { label: 'Potencia Media', value: stravaData.average_watts?.toFixed(0), unit: 'W' },
             { label: 'Potencia Máx.', value: stravaData.max_watts?.toFixed(0), unit: 'W' },
             { label: 'Velocidad Media', value: (stravaData.average_speed * 3.6).toFixed(1), unit: 'km/h' },
             { label: 'Cadencia Media', value: stravaData.average_cadence?.toFixed(0), unit: 'rpm' },
             { label: 'Calorías', value: stravaData.calories?.toFixed(0), unit: 'kcal' }
         ];
         appendMetricsCards(container, basicMetrics);
         return;
    }

    // (Este código solo se ejecutaría si analytics NO fuera null en el futuro)
    const advancedMetrics = [
        { label: 'Carga (TSS)', value: analytics.load?.toFixed(0), unit: '' },
        { label: 'Intensidad (IF)', value: analytics.intensity?.toFixed(2), unit: '' },
        { label: 'Pot. Normalizada', value: analytics.epower?.toFixed(0), unit: 'W' },
        { label: 'Variabilidad (VI)', value: analytics.variability?.toFixed(2), unit: '' },
        { label: 'Trabajo', value: analytics.work?.toFixed(0), unit: 'kJ' }
    ];
    appendMetricsCards(container, advancedMetrics);
}

// Función auxiliar para añadir tarjetas
function appendMetricsCards(container, metricsArray) {
    metricsArray.forEach(m => {
        if (m.value != null && !isNaN(m.value) && m.value !== 'NaN') {
            const card = document.createElement('div');
            card.className = 'metric-card';
            card.innerHTML = `
                <span class="metric-label">${m.label}</span>
                <span class="metric-value">${m.value}</span>
                <span class="metric-unit">${m.unit}</span>
            `;
            container.appendChild(card);
        }
    });
}

function renderMap(latlngStream) {
    const mapContainer = document.getElementById('map-detail');
    // Verificamos si el stream existe y tiene datos
    if (!latlngStream || !latlngStream.data || latlngStream.data.length === 0) {
        mapContainer.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#9aa4ad;">No hay datos GPS disponibles para esta actividad.</div>';
        return;
    }

    // Limpiamos por si hubiera un mapa anterior
    mapContainer.innerHTML = '';

    const map = L.map(mapContainer).setView(latlngStream.data[0], 13);
    
    // Usamos un mapa base oscuro que combina bien con el diseño
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        attribution: '©OpenStreetMap, ©CartoDB',
        maxZoom: 19,
        subdomains: 'abcd'
    }).addTo(map);

    // Dibujamos la ruta en color acento
    const polyline = L.polyline(latlngStream.data, { color: '#00ff88', weight: 4 }).addTo(map);
    
    // Ajustamos la vista a la ruta con un poco de margen (padding)
    map.fitBounds(polyline.getBounds(), { padding: [30, 30] });

    // Invalidamos tamaño para asegurar que se renderiza bien dentro del grid
    setTimeout(() => { map.invalidateSize(); }, 250);
}

// --- NUEVA FUNCIÓN: Renderiza la gráfica usando streams crudos de Strava ---
function renderPowerChart(timeStream, wattsStream) {
    const ctx = document.getElementById('power-curve-chart').getContext('2d');
    const chartContainer = ctx.canvas.parentNode;

    // Verificamos que tenemos ambos streams
    if (!timeStream || !timeStream.data || !wattsStream || !wattsStream.data || timeStream.data.length === 0) {
        chartContainer.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#9aa4ad;">No hay datos de potencia disponibles.</div>';
        return;
    }

    const timeData = timeStream.data; // Array de segundos [0, 1, 2...]
    const wattsData = wattsStream.data; // Array de watts [100, 120, 115...]

    // Preparamos etiquetas para el eje X (Tiempo)
    // Para no saturar, Chart.js lo maneja automáticamente, pero le pasamos los segundos.
    // Opcional: Podrías formatearlos a 'mm:ss' si son muchos puntos, pero los segundos crudos funcionan.

    // Destruir gráfica anterior si existe para evitar superposiciones
    if (window.myPowerChart instanceof Chart) {
        window.myPowerChart.destroy();
    }

    window.myPowerChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: timeData, // Eje X: Segundos
            datasets: [{
                label: 'Potencia (Watts)',
                data: wattsData, // Eje Y: Watts
                borderColor: '#00ff88', // Color de acento
                backgroundColor: 'rgba(0, 255, 136, 0.1)', // Relleno transparente
                borderWidth: 1,
                fill: true,
                pointRadius: 0, // Ocultamos los puntos para que parezca una línea fluida
                pointHoverRadius: 4,
                tension: 0.2 // Un poco de suavizado
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: { duration: 800 },
            interaction: {
                mode: 'index',
                intersect: false,
            },
            scales: {
                x: {
                    type: 'linear', // Eje X lineal para tiempo
                    grid: { color: 'rgba(255,255,255,0.05)', borderColor: 'rgba(255,255,255,0.1)' },
                    ticks: { 
                        color: '#9aa4ad',
                        maxTicksLimit: 8,
                        // Callback para formatear los segundos a minutos:segundos si quieres
                        callback: function(value) {
                            const m = Math.floor(value / 60);
                            // const s = value % 60;
                            // return `${m}:${s < 10 ? '0'+s : s}`;
                            return m + ' min'; // Simplificado: muestra minutos
                        }
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
                    backgroundColor: '#0f1724',
                    titleColor: '#00ff88',
                    bodyColor: '#fff',
                    borderColor: 'rgba(255,255,255,0.1)',
                    borderWidth: 1,
                    callbacks: {
                        title: (context) => {
                           const sec = context[0].parsed.x;
                           const m = Math.floor(sec / 60);
                           const s = sec % 60;
                           return `Tiempo: ${m}m ${s}s`;
                        }
                    }
                }
            }
        }
    });
}