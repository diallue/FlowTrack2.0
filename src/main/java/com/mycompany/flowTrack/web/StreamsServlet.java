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
 * Servlet que expone un endpoint de API ("/api/streams") para obtener los flujos de datos
 * detallados (streams) de una actividad específica de Strava.
 * Actúa como proxy entre el frontend y la API de Strava para manejar la autenticación del lado del servidor.
 */
@WebServlet("/api/streams")
public class StreamsServlet extends HttpServlet {

    private StravaService stravaService;

    /**
     * Inicialización del servlet, donde se configura el servicio de Strava.
     */
    @Override
    public void init() throws ServletException {
        // Inicializa el servicio con las credenciales de la aplicación Strava.
        // (Nota: Estas credenciales deberían gestionarse de forma más segura en un entorno de producción).
        this.stravaService = new StravaService("177549", "17af0ae01a69783ef0981bcea389625c3300803e");
    }

    /**
     * Maneja las peticiones HTTP GET para solicitar los streams de una actividad.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Configura la respuesta para que sea JSON con codificación UTF-8.
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Verifica la sesión del usuario.
        HttpSession session = request.getSession(false);
        User usuario = (session != null) ? (User) session.getAttribute("USUARIO_LOGEADO") : null;

        // Si el usuario no está logeado o no tiene token, devuelve un error 401 Unauthorized.
        if (usuario == null || usuario.getAccessToken() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"No autenticado\"}");
            return;
        }

        try {
            // Obtener parámetros de la solicitud GET provenientes del frontend (JavaScript).
            String idStr = request.getParameter("id"); // ID de la actividad
            String keys = request.getParameter("keys"); // Lista de tipos de streams solicitados (ej: "latlng,time,watts")
            String keyByTypeStr = request.getParameter("key_by_type"); // Indica si agrupar la respuesta por tipo de stream
            boolean keyByType = "true".equals(keyByTypeStr); // Convierte el String a boolean.

            // Validación básica del ID de actividad.
            if (idStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            long activityId = Long.parseLong(idStr);

            // Llamada al servicio de Strava para obtener los datos de los streams.
            String streamsJson = stravaService.getActivityStreams(
                usuario.getAccessToken(), 
                activityId, 
                keys, 
                keyByType
            );

            // Devolver el JSON crudo (tal cual viene de Strava) directamente al frontend.
            response.getWriter().write(streamsJson);

        } catch (Exception e) {
            // Manejo de errores durante la llamada a la API o procesamiento de la respuesta.
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error obteniendo streams\"}");
        }
    }
}
