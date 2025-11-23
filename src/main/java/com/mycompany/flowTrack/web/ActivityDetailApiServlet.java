/* web/ActivityDetailApiServlet.java */
package com.mycompany.flowTrack.web;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mycompany.flowTrack.model.Activity;
import com.mycompany.flowTrack.model.AnalysisResults; // ¡Importante!
import com.mycompany.flowTrack.model.User;
import com.mycompany.flowTrack.service.CyclingAnalyticsService; // ¡Importante!
import com.mycompany.flowTrack.service.StravaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/activity-detail")
public class ActivityDetailApiServlet extends HttpServlet {

    private StravaService stravaService;
    private CyclingAnalyticsService analyticsService; // Servicio reactivado
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        // RECUERDA: Mover credenciales a configuración
        this.stravaService = new StravaService("177549", "17af0ae01a69783ef0981bcea389625c3300803e");
        // RECUERDA: Usar tu token real de Cycling Analytics
        this.analyticsService = new CyclingAnalyticsService("TU_CYCLING_ANALYTICS_TOKEN_AQUI"); 
        
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .findAndRegisterModules();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User usuario = (session != null) ? (User) session.getAttribute("USUARIO_LOGEADO") : null;

        if (usuario == null || usuario.getAccessToken() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"No autenticado\"}");
            return;
        }

        String activityIdStr = request.getParameter("id");
        if (activityIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Falta el parámetro id\"}");
            return;
        }

        try {
            long activityId = Long.parseLong(activityIdStr);
            String stravaToken = usuario.getAccessToken();

            // 1. Obtener datos básicos de Strava
            Activity activityStrava = stravaService.getActivity(stravaToken, activityId);

            // 2. Obtener Streams (Aseguramos 'cadence')
            String streamsKeys = "time,latlng,cadence,watts,altitude,heartrate,velocity_smooth";
            String streamsJsonRaw = stravaService.getActivityStreams(stravaToken, activityId, streamsKeys, true);
            Object streamsData = objectMapper.readValue(streamsJsonRaw, Object.class);

            // 3. Obtener Análisis de Cycling Analytics (Reactivado)
            AnalysisResults analyticsData = null;
            try {
                // Subimos la actividad a CA y obtenemos el análisis
                String analyticsJsonRaw = analyticsService.uploadActivity(activityStrava);
                analyticsData = objectMapper.readValue(analyticsJsonRaw, AnalysisResults.class);
            } catch (Exception e) {
                // Si falla CA, lo logueamos pero no rompemos toda la petición
                System.err.println("Error al obtener análisis de CA: " + e.getMessage());
                // analyticsData seguirá siendo null
            }

            // 4. Combinar todo en la respuesta
            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("strava_activity", activityStrava);
            finalResponse.put("streams", streamsData);
            finalResponse.put("analytics", analyticsData);

            objectMapper.writeValue(response.getWriter(), finalResponse);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error procesando la actividad: " + e.getMessage() + "\"}");
        }
    }
}