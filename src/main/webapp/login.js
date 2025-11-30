// Espera a que todo el DOM (estructura HTML) esté completamente cargado antes de ejecutar el código JavaScript.
document.addEventListener("DOMContentLoaded", () => {
    
    // Define la URL de autorización de Strava utilizando el flujo de OAuth 2.0.
    const stravaAuthUrl =
        "https://www.strava.com/oauth/authorize" + // Endpoint base de autorización de Strava
        "?client_id=177549" +                     // ID de cliente de la aplicación registrada en Strava
        "&response_type=code" +                   // Solicita un "código" de autorización que luego se intercambiará por un token
        // URI a la que Strava redirigirá al usuario tras la autorización.
        // Debe coincidir exactamente con la configurada en la aplicación de Strava y en el StravaCallbackServlet.
        "&redirect_uri=http://localhost:8080/FlowTrack/exchange_token" + 
        // Define los permisos (scopes) que la aplicación necesita. En este caso, acceso de lectura a todas las actividades.
        "&scope=activity:read_all";

    // Obtiene una referencia al botón de login en el DOM usando su ID.
    const btn = document.getElementById("login-strava");

    // Añade un listener de evento 'click' al botón.
    btn.addEventListener("click", () => {
        // Cuando se hace clic en el botón, el navegador es redirigido a la URL de autorización de Strava.
        window.location.href = stravaAuthUrl;
    });
});
