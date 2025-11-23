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
        this.stravaService = new StravaService("CLIENT_ID", "CLIENT_SECRET");
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
            int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
            int perPage = request.getParameter("per_page") != null ? Integer.parseInt(request.getParameter("per_page")) : 30;

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

            // Obtener actividades filtradas hasta llenar la página solicitada
            List<Activity> filteredActivities = new java.util.ArrayList<>();
            int stravaPage = 1;
            int itemsToSkip = (page - 1) * perPage;

            while (filteredActivities.size() < itemsToSkip + perPage) {
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

            // Ordenamiento
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
                        filteredActivities.sort(Comparator.comparing(Activity::getStartDateLocal, Comparator.nullsLast(Comparator.reverseOrder())));
                        break;
                }
            }

            // Sublista según la página
            int fromIndex = Math.min(itemsToSkip, filteredActivities.size());
            int toIndex = Math.min(itemsToSkip + perPage, filteredActivities.size());
            List<Activity> pageActivities = filteredActivities.subList(fromIndex, toIndex);

            objectMapper.writeValue(response.getWriter(), pageActivities);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error al filtrar actividades: " + e.getMessage() + "\"}");
        }
    }
}
