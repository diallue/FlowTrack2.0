package com.mycompany.flowTrack.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * /api/activities
 * Devuelve TODAS las actividades del usuario, con filtros opcionales.
 */
@WebServlet("/api/activities")
public class ActivitiesServlet extends HttpServlet {

    private StravaService strava;
    private ObjectMapper mapper;

    @Override
    public void init() throws ServletException {
        mapper = new ObjectMapper();
        strava = new StravaService("177549", "17af0ae01a69783ef0981bcea389625c3300803e");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("USUARIO_LOGEADO") == null) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"not_logged_in\"}");
            return;
        }

        User u = (User) session.getAttribute("USUARIO_LOGEADO");

        // -----------------------------
        // 1) Leer parámetros de filtros
        // -----------------------------
        String type = req.getParameter("type");
        String query = req.getParameter("q");
        String dateFrom = req.getParameter("date_from");
        String dateTo = req.getParameter("date_to");
        String minDistanceStr = req.getParameter("min_distance");

        Double minDistance = (minDistanceStr != null && !minDistanceStr.isEmpty())
                ? Double.parseDouble(minDistanceStr)
                : null;

        // Convertir fechas a epoch (segundos)
        Long after = null;
        if (dateFrom != null && !dateFrom.isEmpty()) {
            after = LocalDate.parse(dateFrom)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toEpochSecond();
        }

        Long before = null;
        if (dateTo != null && !dateTo.isEmpty()) {
            before = LocalDate.parse(dateTo)
                    .plusDays(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toEpochSecond();
        }

        // -----------------------------
        // 2) Descargar TODAS las actividades de Strava
        // -----------------------------
        List<Activity> all = new ArrayList<>();

        int page = 1;
        int perPage = 200; // máximo permitido

        while (true) {
            List<Activity> block;

            try {
                block = strava.getActivities(u.getAccessToken(), perPage, page, before, after);
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(500);
                resp.getWriter().write("{\"error\":\"strava_api_error\"}");
                return;
            }

            if (block.isEmpty()) break;

            all.addAll(block);

            if (block.size() < perPage) break;

            page++;
        }

        // -----------------------------
        // 3) Aplicar filtros manuales
        // -----------------------------
        List<Activity> filtered = all;

        if (type != null && !type.isEmpty()) {
            filtered = filtered.stream()
                    .filter(a -> type.equalsIgnoreCase(a.getType()))
                    .collect(Collectors.toList());
        }

        if (query != null && !query.isEmpty()) {
            filtered = filtered.stream()
                    .filter(a -> a.getName() != null && a.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (minDistance != null) {
            filtered = filtered.stream()
                    .filter(a -> a.getDistance() != null && a.getDistance() >= minDistance)
                    .collect(Collectors.toList());
        }

        // -----------------------------
        // 4) Responder JSON final
        // -----------------------------
        resp.setContentType("application/json");
        mapper.writeValue(resp.getWriter(), filtered);
    }
}
