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
        // 1. Llamar a nuestro nuevo Backend API
        const response = await fetch(`./api/activity-detail?id=${activityId}`);
        
        if (response.status === 401) {
            window.location.href = 'login.html';
            return;
        }
        
        if (!response.ok) throw new Error('Error cargando detalles de la actividad');
        
        const data = await response.json();
        // data tiene la estructura: { strava_activity: {}, analytics: {}, streams: {} }

        renderHeader(data.strava_activity);
        renderMetrics(data.analytics, data.strava_activity);
        renderMap(data.streams?.latlng);
        renderPowerCurveChart(data.analytics?.power_curve);

    } catch (error) {
        console.error(error);
        document.getElementById('act-title').textContent = "Error cargando actividad";
        document.getElementById('metrics-container').innerHTML = `<div style="color:red">${error.message}</div>`;
    }
});

// --- Funciones de Renderizado ---

function renderHeader(stravaData) {
    document.getElementById('act-title').textContent = stravaData.name;
    const date = new Date(stravaData.start_date_local).toLocaleString();
    const dist = (stravaData.distance / 1000).toFixed(2) + ' km';
    // Cálculo simple de tiempo, puedes usar tu función formatTime de mis-actividades.js
    const time = (stravaData.moving_time / 60).toFixed(0) + ' min'; 
    
    document.getElementById('act-meta').innerHTML = `
        <span>📅 ${date}</span> • <span>📏 ${dist}</span> • <span>⏱️ ${time}</span>
    `;
}

function renderMetrics(analytics, stravaData) {
    const container = document.getElementById('metrics-container');
    container.innerHTML = ''; // Limpiar placeholder

    if (!analytics || !analytics.load) {
        container.innerHTML = '<div class="muted">No se pudo generar el análisis de rendimiento.</div>';
        return;
    }

    // Definimos qué métricas queremos mostrar
    const metricsToShow = [
        { label: 'Carga (TSS)', value: analytics.load?.toFixed(0), unit: '' },
        { label: 'Intensidad (IF)', value: analytics.intensity?.toFixed(2), unit: '' },
        { label: 'Pot. Normalizada', value: analytics.epower?.toFixed(0), unit: 'W' },
        { label: 'Variabilidad (VI)', value: analytics.variability?.toFixed(2), unit: '' },
        { label: 'Trabajo', value: analytics.work?.toFixed(0), unit: 'kJ' },
        { label: 'Potencia Media', value: stravaData.average_watts?.toFixed(0) || analytics.avg_power?.toFixed(0), unit: 'W' },
        { label: 'FC Media', value: analytics.avg_heartrate?.toFixed(0), unit: 'bpm' }
    ];

    metricsToShow.forEach(m => {
        if (m.value != null && !isNaN(m.value)) {
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
    if (!latlngStream || !latlngStream.data || latlngStream.data.length === 0) {
        document.getElementById('map-detail').innerHTML = '<div style="padding:20px;color:#bbb;text-align:center;">No hay datos GPS.</div>';
        return;
    }

    const map = L.map('map-detail').setView(latlngStream.data[0], 13);
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        attribution: '©OpenStreetMap, ©CartoDB',
        maxZoom: 19
    }).addTo(map);

    const polyline = L.polyline(latlngStream.data, { color: '#00ff88', weight: 4 }).addTo(map);
    map.fitBounds(polyline.getBounds(), { padding: [20, 20] });
}

function renderPowerCurveChart(powerCurveData) {
    const ctx = document.getElementById('power-curve-chart').getContext('2d');
    
    if (!powerCurveData || powerCurveData.length === 0) {
        ctx.canvas.parentNode.innerHTML = '<div style="padding:20px;color:#bbb;text-align:center;">No hay datos de potencia.</div>';
        return;
    }

    // powerCurveData viene como [[segundos, watts], [segundos, watts]...]
    // Preparamos los datos para Chart.js
    const labels = [];
    const dataPoints = [];

    powerCurveData.forEach(point => {
        const seconds = point[0];
        const watts = point[1];
        
        // Crear etiquetas legibles para el eje X
        let label = seconds + 's';
        if (seconds >= 60) label = (seconds/60).toFixed(0) + 'm';
        if (seconds >= 3600) label = (seconds/3600).toFixed(1) + 'h';
        
        labels.push(label);
        dataPoints.push(watts);
    });

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Potencia Crítica (Watts)',
                data: dataPoints,
                borderColor: '#00ff88',
                backgroundColor: 'rgba(0, 255, 136, 0.1)',
                borderWidth: 2,
                fill: true,
                pointRadius: 2,
                tension: 0.3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    grid: { color: 'rgba(255,255,255,0.05)' },
                    ticks: { color: '#9aa4ad', maxTicksLimit: 10 }
                },
                y: {
                    grid: { color: 'rgba(255,255,255,0.05)' },
                    ticks: { color: '#9aa4ad' },
                    title: { display: true, text: 'Watts', color: '#9aa4ad' }
                }
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    backgroundColor: '#0f1724',
                    titleColor: '#00ff88',
                    bodyColor: '#fff',
                    borderColor: 'rgba(255,255,255,0.1)',
                    borderWidth: 1
                }
            }
        }
    });
}