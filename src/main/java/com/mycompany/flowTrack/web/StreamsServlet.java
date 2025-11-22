/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.web;

import com.mycompany.flowTrack.model.User;
import com.mycompany.flowTrack.service.StravaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 *
 * @author diego
 */
@WebServlet("/api/streams")
public class StreamsServlet extends HttpServlet {

    private StravaService stravaService;

    @Override
    public void init() throws ServletException {
        // Recuerda usar tus credenciales o cargar desde config
        this.stravaService = new StravaService("177549", "17af0ae01a69783ef0981bcea389625c3300803e");
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

        try {
            // Obtener parámetros del JS
            String idStr = request.getParameter("id");
            String keys = request.getParameter("keys"); // "latlng,time,watts..."
            String keyByTypeStr = request.getParameter("key_by_type");
            boolean keyByType = "true".equals(keyByTypeStr);

            if (idStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            long activityId = Long.parseLong(idStr);

            // Llamar a Strava
            String streamsJson = stravaService.getActivityStreams(
                usuario.getAccessToken(), 
                activityId, 
                keys, 
                keyByType
            );

            // Devolver el JSON crudo directamente al frontend
            response.getWriter().write(streamsJson);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error obteniendo streams\"}");
        }
    }
}