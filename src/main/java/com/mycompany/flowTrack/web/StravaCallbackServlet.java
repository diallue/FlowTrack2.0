/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.web;

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
 *
 * @author diego
 */
@WebServlet("/exchange_token") // Esto mapea la URL http://localhost:8080/exchange_token
public class StravaCallbackServlet extends HttpServlet {

    // TODO: ¡MUY IMPORTANTE! Mueve esto a un archivo de configuración
    // NUNCA dejes credenciales en el código fuente.
    private final String STRAVA_CLIENT_ID = "177549"; // Tu Client ID
    private final String STRAVA_CLIENT_SECRET = "17af0ae01a69783ef0981bcea389625c3300803e"; // Tu Client Secret
    
    private StravaService stravaService;

    @Override
    public void init() throws ServletException {
        // Inicializamos el servicio cuando el servlet se carga
        this.stravaService = new StravaService(STRAVA_CLIENT_ID, STRAVA_CLIENT_SECRET);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // 1. Coger el 'code' que Strava nos envía en la URL
        String code = request.getParameter("code");
        String error = request.getParameter("error");

        // Manejo de error (si el usuario deniega el acceso)
        if (error != null) {
            response.sendRedirect("/login.jsp?error=strava_denied");
            return;
        }

        // Si no hay código, algo fue mal
        if (code == null || code.isEmpty()) {
            response.sendRedirect("/login.jsp?error=strava_fail");
            return;
        }

        try {
            // 2. Intercambiar el código por tokens
            String redirectUri = "http://localhost:8080/FlowTrack/exchange_token";
            TokenResponse tokenResponse = stravaService.exchangeCodeForToken(code, redirectUri);

            // 3. ¡Éxito! Ahora tenemos los tokens y los datos del atleta
            Athletes stravaAthlete = tokenResponse.getAthlete();
            String accessToken = tokenResponse.getAccessToken();
            String refreshToken = tokenResponse.getRefreshToken();
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn());

            // -----------------------------------------------------------------
            // TODO: LÓGICA DE BASE DE DATOS
            // -----------------------------------------------------------------
            // Aquí deberías:
            // 1. Buscar en tu BBDD si ya existe un User con 'stravaAthlete.getId()'
            // 2. Si no existe, crea un nuevo 'User'
            // 3. Si existe, actualízalo.
            // 4. Guarda el accessToken, refreshToken y expiresAt en ese User.

            // Ejemplo de creación/actualización (sin BBDD real):
            User miUsuario = new User(); 
            miUsuario.setId(stravaAthlete.getId());
            miUsuario.setFirstname(stravaAthlete.getFirstname());
            miUsuario.setLastname(stravaAthlete.getLastname());
            miUsuario.setProfileMedium(stravaAthlete.getProfileMedium());
            miUsuario.setAccessToken(accessToken);
            miUsuario.setRefreshToken(refreshToken);
            miUsuario.setTokenExpiresAt(expiresAt);
            
            // ... (miUsuarioRepository.save(miUsuario)) ...
            // -----------------------------------------------------------------

            // 4. Guardamos el usuario en la sesión para "logearlo" en nuestra app
            HttpSession session = request.getSession(true);
            session.setAttribute("USUARIO_LOGEADO", miUsuario);

            // 5. Redirigir al usuario a la página de actividades
            response.sendRedirect("/mis-actividades.jsp"); // O como se llame tu página

        } catch (Exception e) {
            e.printStackTrace();
            // Si el intercambio de token falla
            response.sendRedirect("/login.jsp?error=token_exchange_failed");
        }
    }
}
