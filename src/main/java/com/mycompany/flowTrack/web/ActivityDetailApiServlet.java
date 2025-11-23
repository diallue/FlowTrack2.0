package com.mycompany.flowTrack.web;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mycompany.flowTrack.model.Activity;
// import com.mycompany.flowTrack.model.AnalysisResults; // NO SE USA POR AHORA
import com.mycompany.flowTrack.model.User;
// import com.mycompany.flowTrack.service.CyclingAnalyticsService; // NO SE USA POR AHORA
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
    // private CyclingAnalyticsService analyticsService; // COMENTADO: Servicio eliminado temporalmente
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        // RECUERDA: Idealmente, mueve estas credenciales a un archivo de configuración
        this.stravaService = new StravaService("177549", "17af0ae01a69783ef0981bcea389625c3300803e");
        
        // COMENTADO: Ya no inicializamos este servicio porque la API no funciona como esperábamos
        // this.analyticsService = new CyclingAnalyticsService("TU_TOKEN_AQUI"); 
        
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
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

            // 1. Obtener datos básicos de Strava (Funciona correctamente)
            Activity activityStrava = stravaService.getActivity(stravaToken, activityId);

            // 2. Obtener Streams para mapas y gráficas (Funciona correctamente)
            // Pedimos watts, latlng (mapa), time (eje X gráfica), altitude (perfil)
            String streamsKeys = "watts,latlng,time,altitude,heartrate,cadence";
            // Usamos key_by_type=true para que sea más fácil de usar en el JS
            String streamsJsonRaw = stravaService.getActivityStreams(stravaToken, activityId, streamsKeys, true);
            Object streamsData = objectMapper.readValue(streamsJsonRaw, Object.class);


            // --- SECCIÓN DE CYCLING ANALYTICS ANULADA ---
            // Como la API no funciona para JSON, enviamos null en esta sección.
            // El frontend deberá manejar este caso mostrando un mensaje.
            Object analyticsData = null; 
            // ----------------------------------------------


            // 3. Combinar todo en un mapa para la respuesta final
            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("strava_activity", activityStrava);
            finalResponse.put("streams", streamsData);
            finalResponse.put("analytics", analyticsData); // Será null

            // Enviar el JSON combinado al frontend
            objectMapper.writeValue(response.getWriter(), finalResponse);

        } catch (Exception e) {
            e.printStackTrace();
            // Si es un error de Strava (ej. actividad no encontrada), el código de estado debería reflejarlo,
            // pero por simplicidad devolvemos 500 con el mensaje.
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error procesando la actividad: " + e.getMessage() + "\"}");
        }
    }
}