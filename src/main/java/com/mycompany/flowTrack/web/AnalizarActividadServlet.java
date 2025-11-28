/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.web;

import com.mycompany.flowTrack.model.Activity;
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
import java.util.List;

/**
 *
 * @author diego
 */
@WebServlet("/analizar-actividad")
public class AnalizarActividadServlet extends HttpServlet {

    private StravaService stravaService;
    private CyclingAnalyticsService analyticsService;

    @Override
    public void init() throws ServletException {
        String stravaClientId = "177549";
        String stravaClientSecret = "17af0ae01a69783ef0981bcea389625c3300803e";
        String cyclingAnalyticsToken = "5565638"; 

        this.stravaService = new StravaService(stravaClientId, stravaClientSecret);
        this.analyticsService = new CyclingAnalyticsService(cyclingAnalyticsToken);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User usuario = (session != null) ? (User) session.getAttribute("USUARIO_LOGEADO") : null;

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String activityIdStr = request.getParameter("activityId");
        if (activityIdStr == null) {
            // Ajustamos la redirección a .html que es lo que usas ahora
            response.sendRedirect(request.getContextPath() + "/mis-actividades.html?error=missing_id");
            return;
        }

        try {
            long activityId = Long.parseLong(activityIdStr);

            // 1. Obtener la Actividad (Usamos el método directo mejor que filtrar la lista)
            Activity actividadSeleccionada = stravaService.getActivity(usuario.getAccessToken(), activityId);

            // 2. NUEVO: Obtener los Streams (necesarios para el CSV de Cycling Analytics)
            String streamsKeys = "time,latlng,cadence,watts,heartrate,velocity_smooth";
            String streamsJsonRaw = stravaService.getActivityStreams(usuario.getAccessToken(), activityId, streamsKeys, true);

            // 3. Enviar a Cycling Analytics (Ahora con los DOS parámetros)
            String jsonResponse = analyticsService.uploadActivity(actividadSeleccionada, streamsJsonRaw);

            // 4. Procesar resultado
            request.setAttribute("analisisResultado", jsonResponse);
            // Asegúrate de que este JSP existe, si no, redirige a actividad.html?id=...
            request.getRequestDispatcher("/ver-analisis.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/mis-actividades.html?error=analisis_fallido");
        }
    }
}