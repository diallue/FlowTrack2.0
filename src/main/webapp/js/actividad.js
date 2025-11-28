// webapp/js/actividad.js

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
        if (!response.ok) throw new Error(`Error del servidor (${response.status})`);
        
        const data = await response.json(); // { strava_activity, analytics, streams }
        
        if (!data.strava_activity) throw new Error("Datos incompletos");

        // 1. Cabecera y Métricas Ampliadas
        renderHeader(data.strava_activity);
        renderMetrics(data.analytics, data.strava_activity);
        
        // 2. Extraer Streams
        const streams = data.streams || {};
        const timeData = extractStreamData(streams.time);
        const latlngData = extractStreamData(streams.latlng);
        
        // Datos para gráficas
        const altData = extractStreamData(streams.altitude);
        const speedData = extractStreamData(streams.velocity_smooth);
        const hrData = extractStreamData(streams.heartrate);
        const cadData = extractStreamData(streams.cadence);

        // 3. Renderizar Mapa y Gráficos (Sin Potencia)
        renderMap(latlngData);

        // Eje X (Tiempo)
        const xAxis = timeData;

        renderChart('chart-elevation', xAxis, altData, 'Elevación (m)', '#8884d8', true);
        
        // Velocidad (convertir m/s a km/h)
        renderChart('chart-speed', xAxis, speedData ? speedData.map(v => v * 3.6) : null, 'Velocidad (km/h)', '#00C49F');
        
        // Frecuencia Cardíaca
        renderChart('chart-hr', xAxis, hrData, 'Frecuencia Cardíaca (bpm)', '#FF5A5A');
        
        // Cadencia
        renderChart('chart-cadence', xAxis, cadData, 'Cadencia (rpm)', '#00d2ff');

        // 4. Análisis de Zonas (Solo si hay pulso)
        if (hrData && hrData.length > 0) {
            renderZonesChart(hrData);
        } else {
            // Ocultar panel de zonas si no hay datos de pulso
            const zonePanel = document.querySelector('.zones-panel');
            if(zonePanel) zonePanel.style.display = 'none';
        }

    } catch (error) {
        console.error("Error:", error);
        showErrorState(error.message);
    }
});

// --- Funciones Auxiliares ---

function extractStreamData(streamObj) {
    if (!streamObj) return null;
    if (Array.isArray(streamObj)) return streamObj;
    if (streamObj.data && Array.isArray(streamObj.data)) return streamObj.data;
    return null;
}

function formatNumber(value, decimals = 0) {
    return (value !== null && value !== undefined && !isNaN(value)) ? Number(value).toFixed(decimals) : '—';
}

function formatTime(seconds) {
    if (!seconds) return '—';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return `${h > 0 ? h + 'h ' : ''}${m}m`;
}

function showErrorState(message) {
    document.getElementById('act-title').textContent = "Error";
    document.getElementById('act-meta').innerHTML = `<span style="color:#ff5a5a;">${message}</span>`;
}

// --- Renderizado ---

function renderHeader(stravaData) {
    document.getElementById('act-title').textContent = stravaData.name || "Actividad sin título";
    const date = new Date(stravaData.start_date_local).toLocaleDateString();
    // Icono según tipo de deporte
    let icon = '🚴';
    if (stravaData.type === 'Run') icon = '🏃';
    if (stravaData.type === 'Swim') icon = '🏊';
    
    document.getElementById('act-meta').innerHTML = `${icon} ${stravaData.type} • 📅 ${date} • 📏 ${(stravaData.distance/1000).toFixed(2)} km • ⏱️ ${formatTime(stravaData.moving_time)}`;
}

function renderMetrics(analytics, stravaData) {
    const container = document.getElementById('metrics-container');
    container.innerHTML = '';
    
    // Lista completa de métricas para mostrar
    // Priorizamos TRIMP y HR Load de Cycling Analytics si no hay potencia
    const metrics = [
        // 1. Esfuerzo y Carga (Cycling Analytics)
        { l: 'Carga (TRIMP)', v: analytics?.trimp || analytics?.load, u: '', highlight: true }, 
        { l: 'Intensidad', v: analytics?.intensity ? formatNumber(analytics.intensity, 2) : null, u: '' },
        
        // 2. Datos Cardíacos (Strava)
        { l: 'Pulso Medio', v: stravaData.average_heartrate, u: 'bpm' },
        { l: 'Pulso Máx', v: stravaData.max_heartrate, u: 'bpm' },
        
        // 3. Velocidad y Ritmo
        { l: 'Vel. Media', v: stravaData.average_speed ? (stravaData.average_speed*3.6).toFixed(1) : null, u: 'km/h' },
        { l: 'Vel. Máxima', v: stravaData.max_speed ? (stravaData.max_speed*3.6).toFixed(1) : null, u: 'km/h' },
        
        // 4. Cadencia
        { l: 'Cadencia Med.', v: stravaData.average_cadence, u: 'rpm' },
        
        // 5. Energía y Entorno
        { l: 'Calorías', v: stravaData.calories, u: 'kcal' },
        { l: 'Desnivel +', v: stravaData.total_elevation_gain, u: 'm' },
        { l: 'Temp. Media', v: stravaData.average_temp, u: '°C' },
        
        // 6. Otros
        { l: 'Dispositivo', v: stravaData.device_name, u: '', small: true }
    ];

    metrics.forEach(m => {
        // Solo mostramos si el valor existe (no es null ni undefined)
        if (m.v !== null && m.v !== undefined && m.v !== '—') {
            const styleClass = m.highlight ? 'metric-value highlight' : 'metric-value';
            const smallClass = m.small ? 'metric-card small-text' : 'metric-card';
            
            container.innerHTML += `
                <div class="${smallClass}">
                    <span class="metric-label">${m.l}</span>
                    <span class="${styleClass}">${m.v}</span>
                    <span class="metric-unit">${m.u}</span>
                </div>`;
        }
    });
}

function renderMap(latlngs) {
    const el = document.getElementById('map-detail');
    if (!latlngs || latlngs.length === 0) { el.innerHTML = "<p style='text-align:center;padding:20px;color:#666'>Sin datos GPS</p>"; return; }
    
    // Limpiamos mapa previo si existe (importante en SPAs, aunque aquí recargamos página)
    if (el._leaflet_id) { el.innerHTML = ''; }

    const map = L.map(el).setView(latlngs[0], 13);
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', { 
        attribution: '©OpenStreetMap',
        maxZoom: 19
    }).addTo(map);
    
    const poly = L.polyline(latlngs, { color: '#00ff88', weight: 4, opacity: 0.8 }).addTo(map);
    
    // Marcadores inicio/fin
    L.circleMarker(latlngs[0], {color: '#00ff88', radius: 5, fillOpacity:1}).addTo(map).bindPopup("Inicio");
    L.circleMarker(latlngs[latlngs.length-1], {color: '#ff5a5a', radius: 5, fillOpacity:1}).addTo(map).bindPopup("Fin");

    map.fitBounds(poly.getBounds(), { padding: [30, 30] });
}

function renderChart(canvasId, labels, data, label, color, fill = false) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return; // Si quitaste el canvas del HTML
    
    if (!data || data.length === 0) {
        // Ocultamos el panel entero si no hay datos para esa gráfica
        const panel = canvas.closest('.chart-panel');
        if(panel) panel.style.display = 'none';
        return;
    }

    new Chart(canvas.getContext('2d'), {
        type: 'line',
        data: {
            labels: labels, 
            datasets: [{
                label: label,
                data: data,
                borderColor: color,
                backgroundColor: color + '20', // 20 = Transparencia hex
                borderWidth: 2,
                pointRadius: 0, // Sin puntos para mejor rendimiento
                pointHoverRadius: 4,
                fill: fill,
                tension: 0.2 // Curva suave
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { 
                legend: { display: false },
                tooltip: { 
                    mode: 'index', 
                    intersect: false,
                    backgroundColor: 'rgba(15, 23, 36, 0.9)',
                    titleColor: color,
                    bodyColor: '#fff',
                    borderColor: 'rgba(255,255,255,0.1)',
                    borderWidth: 1
                } 
            },
            scales: {
                x: { display: false }, 
                y: { 
                    grid: { color: 'rgba(255,255,255,0.05)' }, 
                    ticks: { color: '#888', font: {size: 10} } 
                }
            },
            interaction: { mode: 'nearest', axis: 'x', intersect: false }
        }
    });
}

function renderZonesChart(hrData) {
    // Calculamos zonas básicas (estimación estándar)
    // Z1: <125, Z2: 125-145, Z3: 145-165, Z4: 165-180, Z5: >180
    // Idealmente estas zonas deberían venir del perfil del usuario
    const zones = [0, 0, 0, 0, 0];
    let totalPoints = 0;
    
    hrData.forEach(bpm => {
        if(bpm > 0) { // Ignorar ceros
            totalPoints++;
            if (bpm < 125) zones[0]++;
            else if (bpm < 145) zones[1]++;
            else if (bpm < 165) zones[2]++;
            else if (bpm < 180) zones[3]++;
            else zones[4]++;
        }
    });

    if (totalPoints === 0) return;

    const percentages = zones.map(count => ((count / totalPoints) * 100).toFixed(1));

    new Chart(document.getElementById('zones-chart'), {
        type: 'bar',
        data: {
            labels: ['Z1 Recup', 'Z2 Fondo', 'Z3 Tempo', 'Z4 Umbral', 'Z5 Anaer'],
            datasets: [{
                data: percentages,
                backgroundColor: [
                    '#A0A0A0', // Gris
                    '#3498db', // Azul
                    '#2ecc71', // Verde
                    '#f1c40f', // Amarillo
                    '#e74c3c'  // Rojo
                ],
                borderRadius: 4,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { 
                legend: { display: false }, 
                tooltip: { 
                    callbacks: { label: (c) => `${c.raw}% del tiempo` } 
                } 
            },
            scales: {
                y: { display: false, grid: {display: false} },
                x: { 
                    ticks: { color: '#ccc', font: { size: 11 } }, 
                    grid: { display: false } 
                }
            }
        }
    });
}