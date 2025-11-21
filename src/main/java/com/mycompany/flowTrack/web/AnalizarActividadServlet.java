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
        // Inicializar servicios
        // RECUERDA: Mover estas credenciales a un archivo de configuración
        String stravaClientId = "177549";
        String stravaClientSecret = "17af0ae01a69783ef0981bcea389625c3300803e";
        
        // Token de tu cuenta de Cycling Analytics (Obtenlo en tu perfil de CA)
        String cyclingAnalyticsToken = "5565638"; 

        this.stravaService = new StravaService(stravaClientId, stravaClientSecret);
        this.analyticsService = new CyclingAnalyticsService(cyclingAnalyticsToken);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // 1. Obtener sesión y usuario
        HttpSession session = request.getSession(false);
        User usuario = (session != null) ? (User) session.getAttribute("USUARIO_LOGEADO") : null;

        if (usuario == null) {
            response.sendRedirect("/login.jsp");
            return;
        }

        // 2. Obtener el ID de la actividad que queremos analizar (viene del formulario)
        String activityIdStr = request.getParameter("activityId");
        if (activityIdStr == null) {
            response.sendRedirect("/mis-actividades.jsp?error=missing_id");
            return;
        }

        try {
            long activityId = Long.parseLong(activityIdStr);

            // 3. Recuperar la actividad completa desde Strava
            // (O usar la lista si ya la tienes en sesión, pero mejor asegurar datos frescos)
            // NOTA: getActivities devuelve una lista. Strava tiene un endpoint para UNA actividad: /activities/{id}
            // Si no tienes ese método en StravaService, deberías añadirlo, o buscar en la lista.
            // Por simplicidad, aquí asumimos que tienes un método getActivity(token, id)
            
            // Ejemplo rápido buscando en una lista pequeña (ineficiente pero funcional para ejemplo)
            List<Activity> activities = stravaService.getActivities(usuario.getAccessToken(), 10, 1);
            Activity actividadSeleccionada = activities.stream()
                .filter(a -> a.getId() == activityId)
                .findFirst()
                .orElseThrow(() -> new Exception("Actividad no encontrada"));

            // 4. Enviar a Cycling Analytics
            String jsonResponse = analyticsService.uploadActivity(actividadSeleccionada);

            // 5. Procesar resultado y mostrar al usuario
            // jsonResponse contiene el ID de la nueva actividad en CA y métricas calculadas
            request.setAttribute("analisisResultado", jsonResponse);
            request.getRequestDispatcher("/ver-analisis.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/mis-actividades.jsp?error=analisis_fallido");
        }
    }
}
