package com.mycompany.flowTrack.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.flowTrack.model.Athletes;
import com.mycompany.flowTrack.model.User;
import com.mycompany.flowTrack.service.StravaService;
import com.mycompany.flowTrack.service.StravaService.TokenResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Servlet que gestiona la URL de callback de Strava (OAuth 2.0).
 * Maneja el intercambio del código de autorización por los tokens de acceso y refresco.
 */
@WebServlet("/exchange_token")
public class StravaCallbackServlet extends HttpServlet {

    // Credenciales de la aplicación Strava.
    private final String STRAVA_CLIENT_ID = "177549";
    private final String STRAVA_CLIENT_SECRET = "17af0ae01a69783ef0981bcea389625c3300803e";
    
    private ObjectMapper objectMapper;
    private StravaService stravaService;

    /**
     * Inicialización del servlet.
     */
    @Override
    public void init() throws ServletException {
        this.objectMapper = new ObjectMapper();
        this.stravaService = new StravaService(STRAVA_CLIENT_ID, STRAVA_CLIENT_SECRET);
        // Configura Jackson para no usar timestamps numéricos en fechas.
        this.objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Maneja la solicitud GET que Strava realiza a nuestra aplicación después de la autorización del usuario.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // Parámetros recibidos de Strava en la URL: 'code' (éxito) o 'error' (fallo).
        String code = request.getParameter("code");
        String error = request.getParameter("error");
        
        // Obtiene la ruta del contexto de la aplicación (ej. "/FlowTrack") para construir URLs relativas correctamente.
        String contextPath = request.getContextPath(); 

        // Si Strava devuelve un parámetro 'error', el usuario denegó el acceso.
        if (error != null) {
            response.sendRedirect(contextPath + "/login.html?error=strava_denied");
            return;
        }

        // Si no hay 'error' pero tampoco hay 'code', algo falló inesperadamente.
        if (code == null || code.isEmpty()) {
            response.sendRedirect(contextPath + "/login.html?error=strava_fail");
            return;
        }

        try {
            // 1. URL DE CALLBACK
            // Se utiliza la URL de la solicitud actual como la URI de redirección necesaria para el intercambio de tokens.
            String redirectUri = request.getRequestURL().toString();
            System.out.println("Intentando intercambiar token con URI: " + redirectUri);
            
            // 2. Intercambio de tokens
            // Llama al servicio de Strava para intercambiar el código temporal por tokens permanentes.
            TokenResponse tokenResponse = stravaService.exchangeCodeForToken(code, redirectUri);

            // 3. Procesar datos del atleta
            // Extrae la información relevante de la respuesta de la API.
            Athletes stravaAthlete = tokenResponse.getAthlete();
            String accessToken = tokenResponse.getAccessToken();
            String refreshToken = tokenResponse.getRefreshToken();
            // Calcula la hora de expiración del token sumando los segundos de validez a la hora actual.
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn());
            
            // Crea un objeto de modelo interno (User) con los datos obtenidos.
            User usuarioFinal = new User();
            usuarioFinal.setId(stravaAthlete.getId());
            usuarioFinal.setFirstname(stravaAthlete.getFirstname());
            usuarioFinal.setLastname(stravaAthlete.getLastname());
            usuarioFinal.setProfileMedium(stravaAthlete.getProfileMedium());
            usuarioFinal.setAccessToken(accessToken);
            usuarioFinal.setRefreshToken(refreshToken);
            usuarioFinal.setTokenExpiresAt(expiresAt);
            
            // 4. Guardar en sesión
            // Obtiene/crea la sesión HTTP y guarda el objeto User logeado para mantener el estado de la sesión.
            HttpSession session = request.getSession(true);
            session.setAttribute("USUARIO_LOGEADO", usuarioFinal);

            // 5. REDIRECCIÓN EXITOSA
            // Redirige al usuario a la página principal de actividades ahora que está autenticado.
            response.sendRedirect(contextPath + "/mis-actividades.html");

        } catch (Exception e) {
            // Manejo de errores durante el intercambio de tokens.
            e.printStackTrace(); // Imprime el error para depuración.
            // Redirige al login con un mensaje de error.
            response.sendRedirect(contextPath + "/login.html?error=token_exchange_failed");
        }
    }
}
