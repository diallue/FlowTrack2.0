document.addEventListener("DOMContentLoaded", async () => {
    // ⚠️ Simulación de datos: reemplázalo con fetch() a tu backend.
    // Ejemplo real: const response = await fetch('/api/strava/activities');
    // const { user, activities } = await response.json();

    const user = {
        firstname: "Diego",
        lastname: "Martínez",
        city: "Madrid",
        profile: "https://cdn-icons-png.flaticon.com/512/847/847969.png"
    };
    const activities = [
        { name: "Ciclismo de carretera", distance: 45000, moving_time: 5400, average_speed: 8.3 },
        { name: "Rodillo indoor", distance: 25000, moving_time: 3600, average_speed: 7.1 }
    ];

    // Mostrar datos del usuario
    document.getElementById("profile-pic").src = user.profile;
    document.getElementById("user-name").innerText = `${user.firstname} ${user.lastname}`;
    document.getElementById("user-location").innerText = user.city;

    // Mostrar actividades
    const container = document.getElementById("activities-list");
    activities.forEach(a => {
        const card = document.createElement("div");
        card.classList.add("card");
        card.innerHTML = `
            <h4>${a.name}</h4>
            <p><strong>Distancia:</strong> ${(a.distance/1000).toFixed(1)} km</p>
            <p><strong>Duración:</strong> ${(a.moving_time/60).toFixed(0)} min</p>
            <p><strong>Velocidad media:</strong> ${a.average_speed.toFixed(1)} km/h</p>
        `;
        container.appendChild(card);
    });
});
