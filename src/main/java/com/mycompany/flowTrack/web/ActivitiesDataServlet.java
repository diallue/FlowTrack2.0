/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.web;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mycompany.flowTrack.model.Activity;
import com.mycompany.flowTrack.model.User;
import com.mycompany.flowTrack.service.StravaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author diego
 */
@WebServlet("/api/activities") 
public class ActivitiesDataServlet extends HttpServlet {

    private StravaService stravaService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        // Inicializa el servicio con tus credenciales
        this.stravaService = new StravaService("177549", "17af0ae01a69783ef0981bcea389625c3300803e");
        
        // Configuramos Jackson EXACTAMENTE igual que en StravaService
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.findAndRegisterModules(); 
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Preparamos respuesta JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Verificamos sesión
        HttpSession session = request.getSession(false);
        User usuario = (session != null) ? (User) session.getAttribute("USUARIO_LOGEADO") : null;

        if (usuario == null || usuario.getAccessToken() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"No autenticado\"}");
            return;
        }

        try {
            // Parámetros de paginación
            int page = 1;
            int perPage = 30;
            
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page"));
            }
            if (request.getParameter("per_page") != null) {
                perPage = Integer.parseInt(request.getParameter("per_page"));
            }

            // Llamada a Strava
            List<Activity> actividades = stravaService.getActivities(usuario.getAccessToken(), perPage, page);

            // Enviar JSON al frontend
            objectMapper.writeValue(response.getWriter(), actividades);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error al cargar actividades\"}");
        }
    }
}
