document.addEventListener("DOMContentLoaded", () => {
    const stravaAuthUrl = "https://www.strava.com/oauth/authorize?client_id=177549&response_type=code&redirect_uri=http://localhost/exchange_token&scope=activity:read_all";

    const loginBtn = document.getElementById("login-strava");
    loginBtn.addEventListener("click", () => {
        window.location.href = stravaAuthUrl;
    });
});
