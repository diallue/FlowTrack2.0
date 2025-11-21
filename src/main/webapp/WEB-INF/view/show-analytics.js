document.addEventListener("DOMContentLoaded", async () => {
    // ⚠️ Simulación de datos: reemplázalo con fetch('/api/analytics')
    const analyticsData = {
        labels: ["Enero", "Febrero", "Marzo", "Abril", "Mayo"],
        fitness: [60, 65, 70, 75, 78],
        fatigue: [50, 55, 58, 62, 63],
        form: [10, 12, 15, 13, 15]
    };

    new Chart(document.getElementById("chart"), {
        type: "line",
        data: {
            labels: analyticsData.labels,
            datasets: [
                { label: "Fitness", data: analyticsData.fitness, borderWidth: 2 },
                { label: "Fatiga", data: analyticsData.fatigue, borderWidth: 2 },
                { label: "Forma", data: analyticsData.form, borderWidth: 2 }
            ]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: "top" },
                title: { display: true, text: "Evolución del rendimiento" }
            },
            scales: { y: { beginAtZero: false } }
        }
    });
});
