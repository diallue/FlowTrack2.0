document.addEventListener("DOMContentLoaded", () => {
    const stravaAuthUrl =
        "https://www.strava.com/oauth/authorize" +
        "?client_id=177549" +
        "&response_type=code" +
        "&redirect_uri=http://localhost:8080/exchange_token" +
        "&scope=activity:read_all";

    const form = document.getElementById("login-form");

    form.addEventListener("submit", (e) => {
        e.preventDefault();

        const username = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value.trim();

        // ⚠️ Solo demostrativo: Strava no permite autenticación directa por usuario/contraseña.
        console.log("Usuario:", username);
        console.log("Contraseña:", password);

        // Simular login exitoso y redirigir al flujo OAuth
        window.location.href = stravaAuthUrl;
    });
});
