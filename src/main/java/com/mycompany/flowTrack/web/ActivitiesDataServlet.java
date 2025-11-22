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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        this.objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
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
            // 1. LEER PARÁMETROS DEL FRONTEND
            int page = 1;
            int perPage = 30;
            if (request.getParameter("page") != null) page = Integer.parseInt(request.getParameter("page"));
            if (request.getParameter("per_page") != null) perPage = Integer.parseInt(request.getParameter("per_page"));

            String type = request.getParameter("type");           
            String dateFrom = request.getParameter("date_from");  
            String dateTo = request.getParameter("date_to");      
            String distMinStr = request.getParameter("distance_min"); 
            String query = request.getParameter("q");             
            String sort = request.getParameter("sort");           

            // 2. CONVERTIR FECHAS A TIMESTAMP (Strava usa Epoch seconds)
            Long after = null;
            Long before = null;
            
            // Usamos la zona horaria del sistema por defecto
            ZoneId zone = ZoneId.systemDefault(); 

            if (dateFrom != null && !dateFrom.isEmpty()) {
                // "after" en Strava es exclusivo, así que tomamos el inicio del día
                after = LocalDate.parse(dateFrom).atStartOfDay(zone).toEpochSecond();
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                // "before" en Strava es exclusivo, sumamos 1 día para incluir el día seleccionado completo
                before = LocalDate.parse(dateTo).plusDays(1).atStartOfDay(zone).toEpochSecond();
            }

            // 3. LLAMADA A STRAVA (Filtrando solo por fechas)
            // Nota: Pedimos 'perPage' items. Si luego filtramos por tipo, podrían quedar menos.
            // Para una app perfecta habría que pedir más páginas, pero esto es suficiente por ahora.
            List<Activity> rawActivities = stravaService.getActivities(usuario.getAccessToken(), perPage, page, before, after);

            // 4. FILTRADO EN MEMORIA (Java Stream API)
            // Filtramos lo que Strava no puede filtrar: Tipo, Distancia mínima, Texto de búsqueda
            List<Activity> filteredActivities = rawActivities.stream()
                .filter(a -> {
                    // Filtro por TIPO
                    if (type != null && !type.isEmpty()) {
                        return a.getType().equalsIgnoreCase(type); // Ride, Run...
                    }
                    return true;
                })
                .filter(a -> {
                    // Filtro por DISTANCIA MÍNIMA (convertimos km a metros)
                    if (distMinStr != null && !distMinStr.isEmpty()) {
                        try {
                            double minMeters = Double.parseDouble(distMinStr) * 1000;
                            return a.getDistance() != null && a.getDistance() >= minMeters;
                        } catch(NumberFormatException e) { return true; }
                    }
                    return true;
                })
                .filter(a -> {
                    // Filtro por TEXTO (Nombre)
                    if (query != null && !query.isEmpty()) {
                        return a.getName() != null && a.getName().toLowerCase().contains(query.toLowerCase());
                    }
                    return true;
                })
                .collect(Collectors.toList());

            // 5. ORDENACIÓN (SORTING)
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
                    // 'start_date_local_desc' es el defecto de Strava, no hace falta ordenar
                    default: 
                         // Strava ya devuelve orden descendente por fecha, no tocamos nada
                        break;
                }
            }

            // 6. ENVIAR RESULTADO
            objectMapper.writeValue(response.getWriter(), filteredActivities);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error al filtrar actividades: " + e.getMessage() + "\"}");
        }
    }
}
