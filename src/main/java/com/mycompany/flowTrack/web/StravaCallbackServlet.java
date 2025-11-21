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
@WebServlet("/exchange_token")
public class StravaCallbackServlet extends HttpServlet {

    private final String STRAVA_CLIENT_ID = "177549";
    private final String STRAVA_CLIENT_SECRET = "17af0ae01a69783ef0981bcea389625c3300803e";
    
    private StravaService stravaService;

    @Override
    public void init() throws ServletException {
        this.stravaService = new StravaService(STRAVA_CLIENT_ID, STRAVA_CLIENT_SECRET);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        String code = request.getParameter("code");
        String error = request.getParameter("error");
        
        // Esto detectará "/FlowTrack" automáticamente
        String contextPath = request.getContextPath(); 

        if (error != null) {
            response.sendRedirect(contextPath + "/login.html?error=strava_denied");
            return;
        }

        if (code == null || code.isEmpty()) {
            response.sendRedirect(contextPath + "/login.html?error=strava_fail");
            return;
        }

        try {
            // 1. URL DE CALLBACK
            // Debe coincidir EXACTAMENTE con la que pusiste en login.js
            // Al usar getRequestURL(), si tu navegador está en /FlowTrack/exchange_token, esto generará la URL correcta.
            String redirectUri = "http://localhost:8080/FlowTrack/exchange_token";
            System.out.println("Intentando intercambiar token con URI: " + redirectUri);
            
            // 2. Intercambio de tokens
            TokenResponse tokenResponse = stravaService.exchangeCodeForToken(code, redirectUri);

            // 3. Procesar datos del atleta
            Athletes stravaAthlete = tokenResponse.getAthlete();
            User miUsuario = new User(); 
            miUsuario.setId(stravaAthlete.getId());
            miUsuario.setFirstname(stravaAthlete.getFirstname());
            miUsuario.setLastname(stravaAthlete.getLastname());
            miUsuario.setProfileMedium(stravaAthlete.getProfileMedium());
            miUsuario.setAccessToken(tokenResponse.getAccessToken());
            miUsuario.setRefreshToken(tokenResponse.getRefreshToken());
            miUsuario.setTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
            
            // 4. Guardar en sesión
            HttpSession session = request.getSession(true);
            session.setAttribute("USUARIO_LOGEADO", miUsuario);

            // 5. REDIRECCIÓN EXITOSA
            // Importante: Usamos contextPath para que vaya a /FlowTrack/mis-actividades.html
            response.sendRedirect(contextPath + "/mis-actividades.html");

        } catch (Exception e) {
            e.printStackTrace(); // Mira la consola de "Output" en NetBeans si esto falla
            // Si falla, volvemos al login
            response.sendRedirect(contextPath + "/login.html?error=token_exchange_failed");
        }
    }
}
