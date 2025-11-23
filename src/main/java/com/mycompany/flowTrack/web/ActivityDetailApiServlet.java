/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.web;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mycompany.flowTrack.model.Activity;
import com.mycompany.flowTrack.model.AnalysisResults;
import com.mycompany.flowTrack.model.User;
import com.mycompany.flowTrack.service.CyclingAnalyticsService;
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

/**
 *
 * @author Nacho
 */



@WebServlet("/api/activity-detail")
public class ActivityDetailApiServlet extends HttpServlet {

    private StravaService stravaService;
    private CyclingAnalyticsService analyticsService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        // TODO: Mover credenciales a archivo de configuración o variables de entorno
        this.stravaService = new StravaService("177549", "17af0ae01a69783ef0981bcea389625c3300803e");
        // TODO: Pon tu token real de Cycling Analytics aquí
        this.analyticsService = new CyclingAnalyticsService("5565638");
        
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

            // 1. Obtener datos básicos de Strava
            Activity activityStrava = stravaService.getActivity(stravaToken, activityId);

            // 2. Obtener análisis de Cycling Analytics
            // NOTA: Esto sube la actividad cada vez que se ve. En producción, 
            // deberías guardar este resultado en tu BBDD y solo llamar a la API si no lo tienes.
            String analyticsJsonRaw = analyticsService.uploadActivity(activityStrava);
            AnalysisResults analyticsData = objectMapper.readValue(analyticsJsonRaw, AnalysisResults.class);

            // 3. Obtener Streams para mapas y gráficas (latlng, watts, time, heartrate)
            String streamsJsonRaw = stravaService.getActivityStreams(stravaToken, activityId, "latlng,watts,time,heartrate", true);
            // Los streams los mantenemos como objeto genérico ya que su estructura varía
            Object streamsData = objectMapper.readValue(streamsJsonRaw, Object.class);

            // 4. Combinar todo en un mapa para la respuesta final
            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("strava_activity", activityStrava);
            finalResponse.put("analytics", analyticsData);
            finalResponse.put("streams", streamsData);

            // Enviar el JSON combinado al frontend
            objectMapper.writeValue(response.getWriter(), finalResponse);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error procesando la actividad: " + e.getMessage() + "\"}");
        }
    }
}