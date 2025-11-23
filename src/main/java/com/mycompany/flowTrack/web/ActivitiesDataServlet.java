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
 * Servlet que devuelve actividades de Strava aplicando filtros en backend
 */
@WebServlet("/api/activities") 
public class ActivitiesDataServlet extends HttpServlet {

    private StravaService stravaService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        this.stravaService = new StravaService("177549", "17af0ae01a69783ef0981bcea389625c3300803e");
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

            Long after = null;
            Long before = null;
            ZoneId zone = ZoneId.systemDefault(); 

            if (dateFrom != null && !dateFrom.isEmpty()) {
                after = LocalDate.parse(dateFrom).atStartOfDay(zone).toEpochSecond();
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                before = LocalDate.parse(dateTo).plusDays(1).atStartOfDay(zone).toEpochSecond();
            }

            // Paginación avanzada: pedimos páginas hasta llenar perPage tras filtrar
            int stravaPage = 1;
            List<Activity> filteredActivities = new java.util.ArrayList<>();
            while (filteredActivities.size() < perPage) {
                List<Activity> rawActivities = stravaService.getActivities(usuario.getAccessToken(), perPage, stravaPage, before, after);
                if (rawActivities.isEmpty()) break;

                List<Activity> currentFiltered = rawActivities.stream()
                        .filter(a -> type == null || type.isEmpty() || a.getType().equalsIgnoreCase(type))
                        .filter(a -> {
                            if (distMinStr != null && !distMinStr.isEmpty()) {
                                try { return a.getDistance() != null && a.getDistance() >= Double.parseDouble(distMinStr)*1000; } 
                                catch(NumberFormatException e){ return true; }
                            }
                            return true;
                        })
                        .filter(a -> query == null || query.isEmpty() || (a.getName() != null && a.getName().toLowerCase().contains(query.toLowerCase())))
                        .collect(Collectors.toList());

                filteredActivities.addAll(currentFiltered);
                stravaPage++;
            }

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
                        break;
                }
            }

            // Limitar a perPage finales
            if (filteredActivities.size() > perPage) {
                filteredActivities = filteredActivities.subList(0, perPage);
            }

            objectMapper.writeValue(response.getWriter(), filteredActivities);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error al filtrar actividades: " + e.getMessage() + "\"}");
        }
    }
}
