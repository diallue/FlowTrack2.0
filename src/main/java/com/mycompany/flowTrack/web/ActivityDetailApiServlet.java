/* web/ActivityDetailApiServlet.java */
package com.mycompany.flowTrack.web;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mycompany.flowTrack.model.Activity;
import com.mycompany.flowTrack.model.AnalysisResults; // Importa el modelo para los resultados del análisis
import com.mycompany.flowTrack.model.User;
import com.mycompany.flowTrack.service.CyclingAnalyticsService; // Importa el servicio de Cycling Analytics
import com.mycompany.flowTrack.service.StravaService;
import com.mycompany.flowTrack.util.Config;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet API que proporciona todos los detalles de una actividad específica,
 * incluyendo datos básicos de Strava, streams detallados y (si es posible)
 * resultados del análisis de Cycling Analytics.
 */
@WebServlet("/api/activity-detail")
public class ActivityDetailApiServlet extends HttpServlet {

    private StravaService stravaService;
    private CyclingAnalyticsService analyticsService; // Servicio para interactuar con CA
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        // 1. LEER CREDENCIALES DEL ARCHIVO DE CONFIGURACIÓN
        String stravaId = Config.get("strava.client.id");
        String stravaSecret = Config.get("strava.client.secret");
        String caToken = Config.get("cycling.analytics.token");
        
        // Verificación de seguridad: Si no están, lanzamos error para que te des cuenta rápido
        if (stravaId == null || stravaSecret == null || caToken == null) {
            throw new ServletException("¡ERROR! Faltan credenciales en config.properties. Revisa que el archivo exista en src/main/resources.");
        }

        // 2. INICIALIZAR SERVICIOS CON LAS VARIABLES
        this.stravaService = new StravaService(stravaId, stravaSecret);
        this.analyticsService = new CyclingAnalyticsService(caToken);
        
        // Configuración del ObjectMapper de Jackson para manejo de JSON.
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE) // snake_case para compatibilidad con APIs
                .findAndRegisterModules(); // Soporte para módulos extra (ej. Java Time API)
    }

    /**
     * Maneja las peticiones GET para obtener los datos combinados de una actividad.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Verificación de autenticación del usuario.
        HttpSession session = request.getSession(false);
        User usuario = (session != null) ? (User) session.getAttribute("USUARIO_LOGEADO") : null;

        if (usuario == null || usuario.getAccessToken() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"No autenticado\"}");
            return;
        }

        // Obtiene el ID de la actividad de los parámetros de la URL.
        String activityIdStr = request.getParameter("id");
        if (activityIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Falta el parámetro id\"}");
            return;
        }

        try {
            long activityId = Long.parseLong(activityIdStr);
            String stravaToken = usuario.getAccessToken();

            // 1. Obtener datos básicos de Strava (model Activity)
            Activity activityStrava = stravaService.getActivity(stravaToken, activityId);

            // 2. Obtener Streams (datos crudos: lat/lon, vatios, ritmo cardíaco, etc.)
            String streamsKeys = "time,latlng,cadence,watts,heartrate,velocity_smooth,altitude,grade_smooth";
            // Llama a Strava y obtiene la respuesta JSON cruda como String
            String streamsJsonRaw = stravaService.getActivityStreams(stravaToken, activityId, streamsKeys, true);
            
            // Convierte el JSON crudo de streams a un tipo Object genérico para incluirlo fácilmente en la respuesta final.
            Object streamsData = objectMapper.readValue(streamsJsonRaw, Object.class);

            // 3. Obtener Análisis de Cycling Analytics
            AnalysisResults analyticsData = null;
            try {
                // Se sube la actividad a Cycling Analytics usando los datos básicos Y los streams crudos para generar el CSV internamente.
                String analyticsJsonRaw = analyticsService.uploadActivity(activityStrava, streamsJsonRaw);
                
                // Mapea la respuesta JSON de CA al modelo AnalysisResults.
                analyticsData = objectMapper.readValue(analyticsJsonRaw, AnalysisResults.class);
            } catch (Exception e) {
                // Si la subida a CA falla (ej. error de servicio externo, datos faltantes), se registra el error pero se permite continuar con los datos de Strava.
                System.err.println("CA Upload Failed: " + e.getMessage());
            }

            // 4. Combinar todo en la respuesta final que se envía al frontend.
            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("strava_activity", activityStrava);
            finalResponse.put("streams", streamsData);
            finalResponse.put("analytics", analyticsData);

            // Escribe el mapa combinado como respuesta JSON al cliente.
            objectMapper.writeValue(response.getWriter(), finalResponse);

        } catch (Exception e) {
            // Manejo de errores genérico durante el proceso.
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error procesando la actividad: " + e.getMessage() + "\"}");
        }
    }
}
