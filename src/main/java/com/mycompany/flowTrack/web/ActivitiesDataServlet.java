package com.mycompany.flowTrack.web;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mycompany.flowTrack.model.Activity;
import com.mycompany.flowTrack.model.User;
import com.mycompany.flowTrack.service.StravaService;
import com.mycompany.flowTrack.util.Config;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet encargado de manejar las solicitudes de datos de actividades de Strava.
 * Actúa como un endpoint de API REST que devuelve una lista de actividades filtradas y paginadas en formato JSON.
 */
@WebServlet("/api/activities") 
public class ActivitiesDataServlet extends HttpServlet {

    // Servicio para interactuar con la API de Strava.
    private StravaService stravaService;
    // Mapper de Jackson para serializar/deserializar objetos Java a/desde JSON.
    private ObjectMapper objectMapper;

    /**
     * Método de inicialización del servlet. Se ejecuta una sola vez cuando se carga el servlet.
     */
    @Override
    public void init() throws ServletException {
        String stravaId = Config.get("strava.client.id");
        String stravaSecret = Config.get("strava.client.secret");
        
        if (stravaId == null || stravaSecret == null) {
            throw new ServletException("Faltan credenciales de Strava en config.properties");
        }
        
        // Inicializa el servicio de Strava con credenciales (estas deberían cargarse de forma segura, no hardcodeadas).
        this.stravaService = new StravaService(stravaId, stravaSecret);
        // Inicializa y configura el ObjectMapper de Jackson.
        this.objectMapper = new ObjectMapper();
        // Ignora propiedades desconocidas en el JSON de respuesta de Strava durante la deserialización.
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Configura Jackson para usar snake_case (común en APIs REST) en lugar de camelCase para las propiedades.
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        // Registra módulos adicionales (como el módulo JavaTime para manejar fechas modernas).
        this.objectMapper.findAndRegisterModules();
        // Configura para no escribir fechas como timestamps numéricos, sino como cadenas ISO 8601.
        this.objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Maneja las solicitudes HTTP GET.
     * Recupera las actividades del usuario logeado en Strava, aplica filtros y devuelve el resultado paginado en JSON.
     * 
     * @param request El objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response El objeto HttpServletResponse que contendrá la respuesta del servlet.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Configura la respuesta para que sea JSON con codificación UTF-8.
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Intenta recuperar la sesión existente sin crear una nueva si no existe.
        HttpSession session = request.getSession(false);
        // Recupera el objeto User de la sesión.
        User usuario = (session != null) ? (User) session.getAttribute("USUARIO_LOGEADO") : null;

        // Verifica si el usuario está logeado y tiene un token de acceso válido.
        if (usuario == null || usuario.getAccessToken() == null) {
            // Si no está autenticado, devuelve un error 401 Unauthorized.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"No autenticado\"}");
            return;
        }

        try {
            // Parámetros de paginación por defecto, obtenidos de la solicitud si existen.
            int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
            int perPage = request.getParameter("per_page") != null ? Integer.parseInt(request.getParameter("per_page")) : 30;

            // Parámetros de filtrado obtenidos de la solicitud HTTP.
            String type = request.getParameter("type");           // Tipo de actividad (ej: "Ride", "Run")
            String dateFrom = request.getParameter("date_from");  // Fecha de inicio para el filtro (YYYY-MM-DD)
            String dateTo = request.getParameter("date_to");      // Fecha de fin para el filtro (YYYY-MM-DD)
            String distMinStr = request.getParameter("distance_min"); // Distancia mínima en Km
            String query = request.getParameter("q");             // Cadena de búsqueda por nombre de actividad
            String sort = request.getParameter("sort");           // Criterio de ordenación (comentado en el código)

            // Tiempos UNIX 'before' y 'after' para la API de Strava.
            Long after = null;
            Long before = null;
            ZoneId zone = ZoneId.systemDefault(); // Zona horaria del sistema para conversiones.

            // Convierte las fechas de cadena (YYYY-MM-DD) a timestamps UNIX.
            if (dateFrom != null && !dateFrom.isEmpty()) {
                after = LocalDate.parse(dateFrom).atStartOfDay(zone).toEpochSecond();
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                // Sumamos un día a 'dateTo' para incluir todas las actividades de ese día.
                before = LocalDate.parse(dateTo).plusDays(1).atStartOfDay(zone).toEpochSecond();
            }

            // --- Lógica de Paginación y Filtrado ---
            
            // Lista para almacenar las actividades que cumplen con los criterios.
            List<Activity> filteredActivities = new java.util.ArrayList<>();
            int stravaPage = 1; // Contador para las páginas solicitadas a la API de Strava.
            // Calcula cuántos elementos deben omitirse para llegar a la página solicitada por el cliente.
            int itemsToSkip = (page - 1) * perPage;

            // Bucle para solicitar datos a Strava en bloques hasta tener suficientes actividades filtradas para la página del cliente.
            while (filteredActivities.size() < itemsToSkip + perPage) {
                // Obtiene una página de actividades sin procesar de la API de Strava, usando los filtros de fecha.
                List<Activity> rawActivities = stravaService.getActivities(usuario.getAccessToken(), perPage, stravaPage, before, after);
                if (rawActivities.isEmpty()) break; // Sale del bucle si ya no hay más actividades en Strava.

                // Aplica filtros adicionales del lado del servidor (tipo, distancia mínima, búsqueda por nombre) usando Streams.
                List<Activity> currentFiltered = rawActivities.stream()
                        // Filtra por tipo de actividad si se especifica.
                        .filter(a -> type == null || type.isEmpty() || a.getType().equalsIgnoreCase(type))
                        // Filtra por distancia mínima (convierte Km a metros, que es la unidad de Strava).
                        .filter(a -> {
                            if (distMinStr != null && !distMinStr.isEmpty()) {
                                try { return a.getDistance() != null && a.getDistance() >= Double.parseDouble(distMinStr)*1000; } 
                                catch(NumberFormatException e){ return true; } // Si el formato es inválido, no se aplica el filtro.
                            }
                            return true;
                        })
                        // Filtra por nombre de actividad (búsqueda insensible a mayúsculas/minúsculas).
                        .filter(a -> query == null || query.isEmpty() || (a.getName() != null && a.getName().toLowerCase().contains(query.toLowerCase())))
                        .collect(Collectors.toList());

                // Añade las actividades filtradas de esta página a la lista general.
                filteredActivities.addAll(currentFiltered);
                stravaPage++; // Prepara la solicitud para la siguiente página de Strava.
            }

            // --- Ordenamiento (Lógica comentada, actualmente usa el orden por defecto de Strava: más reciente primero) ---
            /*
            if (sort != null && !sort.isEmpty()) {
                switch (sort) {
                    case "start_date_local_asc":
                        filteredActivities.sort(Comparator.comparing(Activity::getStartDateLocal));
                        break;
                    case "distance_desc":
                        filteredActivities.sort(Comparator.comparing(Activity::getDistance, Comparator.nullsLast(Comparator.reverseOrder())));
                        break;
                    case "elapsed_time_desc":
                        filteredActivities.sort(Comparator.comparing(Activity::getElapsedTime, Comparator.nullsLast(Comparator.reverseOrder())));
                        break;
                    default:
                        // Orden por defecto: fecha de inicio descendente
                        filteredActivities.sort(Comparator.comparing(Activity::getStartDateLocal, Comparator.nullsLast(Comparator.reverseOrder())));
                        break;
                }
            }
            */

            // --- Paginación Final ---
            // Determina los índices de inicio y fin para la sublista que representa la página solicitada por el cliente.
            int fromIndex = Math.min(itemsToSkip, filteredActivities.size());
            int toIndex = Math.min(itemsToSkip + perPage, filteredActivities.size());
            // Extrae solo las actividades necesarias para la página actual.
            List<Activity> pageActivities = filteredActivities.subList(fromIndex, toIndex);

            // Serializa la lista final de actividades en formato JSON y la escribe en la respuesta HTTP.
            objectMapper.writeValue(response.getWriter(), pageActivities);

        } catch (Exception e) {
            // Manejo de errores genérico.
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error al filtrar actividades: " + e.getMessage() + "\"}");
        }
    }
}
