// webapp/js/actividad.js - VERSIÓN FINAL (CADENCIA + ANALYTICS)

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

        renderHeader(data.strava_activity);
        // Ahora pasamos ambos objetos para que decida qué mostrar
        renderMetrics(data.analytics, data.strava_activity);
        
        // Extraemos datos de streams
        const latlngData = extractStreamData(data.streams?.latlng);
        const timeData = extractStreamData(data.streams?.time);
        // Usamos cadencia en lugar de watts
        const cadenceData = extractStreamData(data.streams?.cadence);

        renderMap(latlngData);
        // Renderizamos la gráfica de cadencia
        renderCadenceChart(timeData, cadenceData);

    } catch (error) {
        console.error("Error crítico:", error);
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
    if (value === null || value === undefined || isNaN(value)) return null;
    return Number(value).toFixed(decimals);
}
function showErrorState(message) { /* ... (igual que antes) ... */ }
function renderHeader(stravaData) { /* ... (igual que antes) ... */ }
function renderMap(latlngData) { /* ... (igual que antes) ... */ }


// --- NUEVA LÓGICA DE MÉTRICAS ---
function renderMetrics(analytics, stravaData) {
    const container = document.getElementById('metrics-container');
    container.innerHTML = '';

    let metrics = [];
    let title = '';

    // Si tenemos análisis avanzado, lo priorizamos
    if (analytics && analytics.load) {
        title = 'Análisis Avanzado (Cycling Analytics)';
        metrics = [
            { label: 'Carga (TSS)', value: formatNumber(analytics.load), unit: '' },
            { label: 'Intensidad (IF)', value: formatNumber(analytics.intensity, 2), unit: '' },
            { label: 'Variabilidad (VI)', value: formatNumber(analytics.variability, 2), unit: '' },
            { label: 'Pot. Normalizada', value: formatNumber(analytics.epower), unit: 'W' },
            { label: 'Trabajo', value: formatNumber(analytics.work), unit: 'kJ' }
        ];
    } else {
        // Si no, mostramos las básicas de Strava
        title = 'Métricas Básicas (Strava)';
        metrics = [
             { label: 'Velocidad Media', value: formatNumber(stravaData.average_speed ? stravaData.average_speed * 3.6 : null, 1), unit: 'km/h' },
             { label: 'Velocidad Máx.', value: formatNumber(stravaData.max_speed ? stravaData.max_speed * 3.6 : null, 1), unit: 'km/h' },
             { label: 'Cadencia Media', value: formatNumber(stravaData.average_cadence), unit: 'rpm' },
             { label: 'Potencia Media', value: formatNumber(stravaData.average_watts), unit: 'W' },
             { label: 'Calorías', value: formatNumber(stravaData.calories), unit: 'kcal' }
         ];
    }

    // Cambiamos el título de la sección
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

// --- NUEVA FUNCIÓN PARA GRÁFICA DE CADENCIA ---
function renderCadenceChart(timeData, cadenceData) {
    const ctx = document.getElementById('power-curve-chart').getContext('2d'); // Reutilizamos el canvas
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
                // Color diferente para cadencia (ej. azul claro/cian)
                borderColor: '#00d2ff',
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