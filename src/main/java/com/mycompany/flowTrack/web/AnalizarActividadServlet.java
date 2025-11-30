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
 * Servlet encargado de procesar la solicitud para analizar una actividad específica de Strava.
 * Actúa como un controlador que orquesta la obtención de datos detallados de Strava y su envío a Cycling Analytics.
 */
@WebServlet("/analizar-actividad")
public class AnalizarActividadServlet extends HttpServlet {

    // Servicio para interactuar con la API de Strava.
    private StravaService stravaService;
    // Servicio para interactuar con la API de Cycling Analytics.
    private CyclingAnalyticsService analyticsService;

    /**
     * Método de inicialización del servlet. Configura las instancias de los servicios con las credenciales necesarias.
     */
    @Override
    public void init() throws ServletException {
        // Credenciales harcodeadas para Strava y Cycling Analytics.
        String stravaClientId = "177549";
        String stravaClientSecret = "17af0ae01a69783ef0981bcea389625c3300803e";
        String cyclingAnalyticsToken = "5565638"; 

        this.stravaService = new StravaService(stravaClientId, stravaClientSecret);
        this.analyticsService = new CyclingAnalyticsService(cyclingAnalyticsToken);
    }

    /**
     * Maneja las solicitudes HTTP POST enviadas cuando el usuario decide analizar una actividad específica.
     * 
     * @param request El objeto HttpServletRequest que contiene la solicitud (espera el parámetro 'activityId').
     * @param response El objeto HttpServletResponse que manejará la redirección o el reenvío a una vista JSP.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // Intenta recuperar la sesión del usuario logeado.
        HttpSession session = request.getSession(false);
        User usuario = (session != null) ? (User) session.getAttribute("USUARIO_LOGEADO") : null;

        // Verifica si el usuario está autenticado. Si no, redirige a la página de login.
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        // Obtiene el ID de la actividad a analizar del parámetro POST.
        String activityIdStr = request.getParameter("activityId");
        if (activityIdStr == null) {
            // Si falta el ID, redirige a la lista de actividades con un mensaje de error.
            response.sendRedirect(request.getContextPath() + "/mis-actividades.html?error=missing_id");
            return;
        }

        try {
            // Convierte el ID de String a long.
            long activityId = Long.parseLong(activityIdStr);

            // 1. Obtener la Actividad: Llama al servicio de Strava para obtener los detalles principales de la actividad.
            Activity actividadSeleccionada = stravaService.getActivity(usuario.getAccessToken(), activityId);

            // 2. NUEVO: Obtener los Streams de datos (ritmo cardíaco, cadencia, vatios, etc.), necesarios para el análisis detallado.
            String streamsKeys = "time,latlng,cadence,watts,heartrate,velocity_smooth";
            String streamsJsonRaw = stravaService.getActivityStreams(usuario.getAccessToken(), activityId, streamsKeys, true);

            // 3. Enviar a Cycling Analytics: Utiliza el servicio de análisis para subir los datos de la actividad y los streams.
            String jsonResponse = analyticsService.uploadActivity(actividadSeleccionada, streamsJsonRaw);

            // 4. Procesar resultado: Guarda la respuesta (que debería contener el enlace al análisis) en un atributo de la solicitud.
            request.setAttribute("analisisResultado", jsonResponse);
            
            // Reenvía la solicitud y la respuesta al JSP encargado de mostrar el resultado del análisis.
            request.getRequestDispatcher("/ver-analisis.jsp").forward(request, response);

        } catch (Exception e) {
            // Manejo de errores: imprime la traza y redirige con un mensaje de error si algo falla durante el proceso.
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/mis-actividades.html?error=analisis_fallido");
        }
    }
}
