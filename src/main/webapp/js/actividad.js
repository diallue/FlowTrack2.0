// Espera a que el DOM esté completamente cargado antes de ejecutar la lógica principal.
document.addEventListener('DOMContentLoaded', async () => {
    // Obtiene los parámetros de la URL actual (ej. ?id=12345).
    const params = new URLSearchParams(window.location.search);
    const activityId = params.get('id');

    // Verifica si el ID de la actividad está presente.
    if (!activityId) {
        alert("No se ha especificado una actividad.");
        // Redirige al usuario a la lista de actividades si falta el ID.
        window.location.href = 'mis-actividades.html'; return;
    }

    try {
        // Realiza una llamada fetch al endpoint del servlet que combina datos de Strava y Cycling Analytics.
        const response = await fetch(`./api/activity-detail?id=${activityId}`);
        
        // Manejo de errores de autenticación HTTP 401 Unauthorized.
        if (response.status === 401) { window.location.href = 'login.html'; return; }
        // Manejo de otros errores HTTP.
        if (!response.ok) throw new Error(`Error del servidor (${response.status})`);
        
        // Parsea la respuesta JSON.
        const data = await response.json(); 
        
        // Verificación básica de que los datos principales de Strava existen.
        if (!data.strava_activity) throw new Error("Datos incompletos");

        // --- 1. EXTRACCIÓN Y LIMPIEZA DE DATOS (IMPORTANTE: HACER ESTO PRIMERO) ---
        const streams = data.streams || {};
        
        // Utiliza una función helper para extraer los arrays de datos limpios de los objetos de streams.
        const timeData = extractStreamData(streams.time);
        const latlngData = extractStreamData(streams.latlng);
        const altData = extractStreamData(streams.altitude);
        const speedData = extractStreamData(streams.velocity_smooth);
        const hrData = extractStreamData(streams.heartrate);
        const cadData = extractStreamData(streams.cadence);
        const gradeData = extractStreamData(streams.grade_smooth);

        // --- 2. RENDERIZADO DE PANELES DE UI ---
        
        // Cabecera: Muestra nombre, tipo, fecha, distancia y tiempo.
        renderHeader(data.strava_activity);
        
        // Métricas: Muestra estadísticas clave, intentando usar datos de Cycling Analytics o calculando un respaldo local.
        renderMetrics(data.analytics, data.strava_activity, hrData);
        
        // Mapa: Renderiza la ruta si hay datos de latitud/longitud.
        renderMap(latlngData);

        // --- 3. RENDERIZADO DE GRÁFICAS CON Chart.js ---
        // 'timeData' se usa comúnmente como el eje X (tiempo relativo en segundos).
        const xAxis = timeData;

        // Renderiza las diferentes gráficas de rendimiento.
        renderChart('chart-elevation', xAxis, altData, 'Elevación (m)', '#8884d8', true);
        renderChart('chart-speed', xAxis, speedData ? speedData.map(v => v * 3.6) : null, 'Velocidad (km/h)', '#00C49F');
        renderChart('chart-hr', xAxis, hrData, 'Frecuencia Cardíaca (bpm)', '#FF5A5A');
        renderChart('chart-cadence', xAxis, cadData, 'Cadencia (rpm)', '#00d2ff');

        // Zonas de Pulso: Renderiza gráfico de zonas si hay datos de FC disponibles.
        if (hrData && hrData.length > 0) {
            renderZonesChart(hrData);
        } else {
            const zp = document.querySelector('.zones-panel');
            if(zp) zp.style.display = 'none'; // Oculta el panel si no hay datos de pulso.
        }
        
        // Zonas de Gradiente: Renderiza gráfico de zonas de pendiente.
        if (gradeData && gradeData.length > 0) {
            renderGradientZones(gradeData);
        }

    } catch (error) {
        // Captura y maneja cualquier error que ocurra durante la carga/renderizado.
        console.error("Error JS:", error);
        showErrorState(error.message);
    }
});

// --- Funciones de Lógica y Renderizado ---

/**
 * Renderiza las tarjetas de métricas en el contenedor especificado.
 * Intenta obtener la carga (TRIMP) de Cycling Analytics, si falla, la calcula localmente.
 * Esta función organiza toda la información clave del entrenamiento.
 */
function renderMetrics(analytics, stravaData, hrArray) {
    const container = document.getElementById('metrics-container');
    container.innerHTML = '';
    
    // --- 1. CÁLCULO DE TRIMP (Carga de entrenamiento) ---
    let trimpValue = analytics?.trimp || analytics?.load;
    let trimpLabel = "(API)";
    if (!trimpValue || trimpValue === 0) {
        // CORRECCIÓN: Comprobamos si hrArray existe y es un array antes de usarlo
        if (hrArray && Array.isArray(hrArray) && hrArray.length > 0) {
            // Usa el cálculo local de Edwards TRIMP como respaldo.
            trimpValue = calculateEdwardsTRIMP(hrArray, stravaData.max_heartrate);
            trimpLabel = ""; // Etiqueta vacía si es calculado localmente.
        }
    }
    
    // --- 2. DISPOSITIVO ---
    const deviceName = stravaData.device_name || "Strava App / Desconocido";

    /**
     * Lista de métricas a mostrar.
     * Cada elemento define:
     *  - l: etiqueta visible
     *  - v: valor
     *  - u: unidad
     *  - highlight: resaltar condiciones especiales
     *  - small: formato reducido
     */
    const metrics = [
        { l: `Carga (TRIMP) ${trimpLabel}`, v: trimpValue ? Math.round(trimpValue) : '—', u: '', highlight: true },
        { l: 'Potencia Norm.', v: analytics?.epower || analytics?.weighted_power, u: 'W' },
        { l: 'Pulso Medio', v: stravaData.average_heartrate, u: 'bpm' },
        { l: 'Pulso Máx', v: stravaData.max_heartrate, u: 'bpm' },
        { l: 'Vel. Media', v: stravaData.average_speed ? (stravaData.average_speed*3.6).toFixed(1) : null, u: 'km/h' },
        { l: 'Vel. Máxima', v: stravaData.max_speed ? (stravaData.max_speed*3.6).toFixed(1) : null, u: 'km/h' },
        { l: 'Cadencia', v: stravaData.average_cadence, u: 'rpm' },
        { l: 'Calorías', v: stravaData.calories, u: 'kcal' },
        { l: 'Desnivel +', v: stravaData.total_elevation_gain, u: 'm' },
        { l: 'Temp. Media', v: stravaData.average_temp, u: '°C' },
        { l: 'Dispositivo', v: deviceName, u: '', small: true }
    ];

    // Itera sobre las métricas y las renderiza en tarjetas HTML.
    metrics.forEach(m => {
        if (m.v !== null && m.v !== undefined && m.v !== '—') {
            const styleClass = m.highlight ? 'metric-value highlight' : 'metric-value';
            const smallClass = m.small ? 'metric-card small-text' : 'metric-card';
            const valFormatted = m.isDecimal ? formatNumber(m.v, 2) : m.v;

            container.innerHTML += `
                <div class="${smallClass}">
                    <span class="metric-label">${m.l}</span>
                    <span class="${styleClass}">${valFormatted}</span>
                    <span class="metric-unit">${m.u}</span>
                </div>`;
        }
    });
}

/**
 * Calcula la carga de entrenamiento (TRIMP) usando el método de Edwards,
 * basado en el tiempo acumulado en zonas de pulsaciones.
 * Este cálculo es un plan B cuando la API de Cycling Analytics no devuelve TRIMP.
 */
function calculateEdwardsTRIMP(hrData, maxHr) {
    if (!Array.isArray(hrData) || hrData.length === 0) return 0;
    
    // Si no hay MaxHR en Strava, usamos 190 como estándar seguro.
    const max = maxHr || 190; 
    let trimp = 0;

    hrData.forEach(bpm => {
        if (bpm > 0) { // Ignorar lecturas de cero (posible ausencia de sensor).
            const percent = bpm / max;
            let factor = 0;
            // Asigna un factor de intensidad basado en el porcentaje de FC máxima.
            if (percent >= 0.5 && percent < 0.6) factor = 1;
            else if (percent >= 0.6 && percent < 0.7) factor = 2;
            else if (percent >= 0.7 && percent < 0.8) factor = 3;
            else if (percent >= 0.8 && percent < 0.9) factor = 4;
            else if (percent >= 0.9) factor = 5;
            
            // Sumamos (factor * minutos). Como el dato es por segundo, dividimos por 60.
            trimp += (factor / 60);
        }
    });
    return trimp;
}

// --- Helpers Genéricos ---

/**
 * Normaliza los datos de stream. La API de Strava puede devolver:
 *   - array simple
 *   - objeto con { data: [...] }
 * Esta función unifica ambos formatos para simplificar su uso.
 */
function extractStreamData(streamObj) {
    if (!streamObj) return null;
    if (Array.isArray(streamObj)) return streamObj;
    if (streamObj.data && Array.isArray(streamObj.data)) return streamObj.data;
    return null;
}

/**
 * Formatea un número para visualización, manejando valores nulos o no numéricos.
 */
function formatNumber(value, decimals = 0) {
    return (value !== null && value !== undefined && !isNaN(value)) ? Number(value).toFixed(decimals) : '—';
}

/**
 * Formatea segundos en un formato de tiempo legible (Hh Mm).
 * Útil para mostrar duración de actividad en la cabecera.
 */
function formatTime(seconds) {
    if (!seconds) return '—';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return `${h > 0 ? h + 'h ' : ''}${m}m`;
}

/**
 * Renderiza la sección de cabecera con nombre, tipo, icono, fecha,
 * distancia total y tiempo en movimiento.
 */
function renderHeader(stravaData) {
    document.getElementById('act-title').textContent = stravaData.name || "Actividad";
    const date = new Date(stravaData.start_date_local).toLocaleDateString();
    let icon = '🚴'; // Icono por defecto
    if (stravaData.type === 'Run') icon = '🏃';
    else if (stravaData.type === 'Swim') icon = '🏊';
    
    document.getElementById('act-meta').innerHTML = `${icon} ${stravaData.type} • 📅 ${date} • 📏 ${(stravaData.distance/1000).toFixed(2)} km • ⏱️ ${formatTime(stravaData.moving_time)}`;
}

/**
 * Renderiza el mapa de la ruta usando la librería Leaflet.
 * Se dibuja la polilínea GPS y se auto-ajusta la vista para encajar toda la ruta.
 */
function renderMap(latlngs) {
    const el = document.getElementById('map-detail');
    if (!latlngs || latlngs.length === 0) { el.innerHTML = "<p style='text-align:center;color:#666;padding:20px'>Sin GPS</p>"; return; }
    // Limpieza básica si el mapa ya estaba inicializado (útil para desarrollo en caliente).
    if (el._leaflet_id) { el.innerHTML = ''; } 

    // Inicializa el mapa y añade una capa de tiles (mapa base).
    const map = L.map(el).setView(latlngs[0], 13);
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', { attribution: '©OpenStreetMap' }).addTo(map);
    // Dibuja la polilínea de la ruta y ajusta la vista del mapa para que quepa toda la ruta.
    const poly = L.polyline(latlngs, { color: '#00ff88', weight: 4 }).addTo(map);
    map.fitBounds(poly.getBounds(), { padding: [20, 20] });
}

/**
 * Renderiza una gráfica de línea usando la librería Chart.js.
 * Esta función se usa para velocidad, altitud, pulso, cadencia, etc.
 */
function renderChart(canvasId, labels, data, label, color, fill = false) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    
    // Oculta el panel de la gráfica si no hay datos para mostrar.
    if (!data || data.length === 0) {
        const panel = canvas.closest('.chart-panel');
        if(panel) panel.style.display = 'none';
        return;
    }

    // Crea una nueva instancia de Chart.js
    new Chart(canvas.getContext('2d'), {
        type: 'line',
        data: {
            labels: labels, 
            datasets: [{
                label: label,
                data: data,
                borderColor: color,
                backgroundColor: color + '20',
                borderWidth: 2,
                pointRadius: 0,
                pointHoverRadius: 4,
                fill: fill,
                tension: 0.2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { x: { display: false }, y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#888' } } },
            interaction: { mode: 'nearest', axis: 'x', intersect: false }
        }
    });
}

/**
 * Renderiza un gráfico de barras con la distribución del tiempo
 * en zonas de frecuencia cardíaca (Z1 - Z5).
 */
function renderZonesChart(hrData) {
    if (!hrData || !Array.isArray(hrData)) return;
    const zones = [0, 0, 0, 0, 0];
    let totalPoints = 0;
    
    hrData.forEach(bpm => {
        if(bpm > 0) { 
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
            labels: ['Z1', 'Z2', 'Z3', 'Z4', 'Z5'],
            datasets: [{ data: percentages, backgroundColor: ['#A0A0A0', '#3498db', '#2ecc71', '#f1c40f', '#e74c3c'], borderRadius: 4 }]
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { y: { display: false }, x: { ticks: { color: '#ccc' }, grid: { display: false } } }
        }
    });
}

/**
 * Muestra un estado de error en la interfaz cuando ocurre un fallo crítico.
 */
function showErrorState(message) {
    document.getElementById('act-title').textContent = "Error";
    document.getElementById('act-meta').innerHTML = `<span style="color:#ff5a5a;">${message}</span>`;
}

/**
 * Renderiza las zonas de gradiente (pendiente) del recorrido.
 * Esto permite identificar cuánto tiempo se ha pasado en llano, subida, bajada, etc.
 */
function renderGradientZones(gradeData) {
    if (!Array.isArray(gradeData) || gradeData.length === 0) return;

    // Categorías de terreno según intensidad de la pendiente.
    const zones = [0, 0, 0, 0, 0];
    let totalPoints = 0;

    gradeData.forEach(g => {
        totalPoints++;
        if (g < -1) zones[0]++;
        else if (g < 2) zones[1]++;
        else if (g < 5) zones[2]++;
        else if (g < 8) zones[3]++;
        else zones[4]++;
    });

    const percentages = zones.map(count => ((count / totalPoints) * 100).toFixed(1));

    new Chart(document.getElementById('gradient-chart'), {
        type: 'bar',
        data: {
            labels: ['Bajada', 'Llano', '2-5%', '5-8%', '>8%'],
            datasets: [{
                data: percentages,
                backgroundColor: [
                    '#00C49F', // Bajada (Verde agua)
                    '#A0A0A0', // Llano (Gris)
                    '#f1c40f', // Amarillo
                    '#e67e22', // Naranja
                    '#e74c3c'  // Rojo (Duro)
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
                tooltip: { callbacks: { label: (c) => `${c.raw}% del recorrido` } } 
            },
            scales: {
                y: { display: false },
                x: { ticks: { color: '#ccc', font: { size: 11 } }, grid: { display: false } }
            }
        }
    });
}
