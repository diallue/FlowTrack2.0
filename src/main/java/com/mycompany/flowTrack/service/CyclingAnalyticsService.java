package com.mycompany.flowTrack.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.flowTrack.model.Activity;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para interactuar con la API de Cycling Analytics.
 *
 * Permite subir actividades deportivas obtenidas de Strava y obtener un
 * registro analítico en Cycling Analytics.
 */
public class CyclingAnalyticsService {
    /** URL base del endpoint de Cycling Analytics para subir rides. */
    private static final String API_URL = "https://www.cyclinganalytics.com/api/json_ride";

    /** Token de autorización de Cycling Analytics (NO el de Strava). */
    private final String apiToken;

    /** Cliente HTTP reutilizable para las peticiones. */
    private final HttpClient httpClient;

    /** Mapeador JSON para construir y parsear objetos JSON. */
    private final ObjectMapper objectMapper;

    /**
     * Constructor.
     *
     * @param apiToken Token de acceso a Cycling Analytics.
     */
    public CyclingAnalyticsService(String apiToken) {
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sube una actividad de Strava a Cycling Analytics.
     *
     * Construye el JSON requerido por la API y realiza una petición HTTP POST.
     *
     * @param activity Actividad obtenida de Strava.
     * @return JSON devuelto por Cycling Analytics con el ID de la nueva actividad y métricas.
     * @throws IOException Si ocurre un error en la petición HTTP.
     * @throws InterruptedException Si la petición HTTP es interrumpida.
     */
    public String uploadActivity(Activity activity) throws IOException, InterruptedException {
        
        // 1. Construir el JSON específico que pide Cycling Analytics
        ObjectNode jsonBody = objectMapper.createObjectNode();

        // -- Campos Obligatorios / Importantes --
        if (activity.getStartDate() != null) {
            jsonBody.put("start_time", activity.getStartDate().format(DateTimeFormatter.ISO_INSTANT));
        }
        
        if (activity.getTimezone() != null) {
            jsonBody.put("timezone", activity.getTimezone());
        }

        if (activity.getElapsedTime() != null) {
            jsonBody.put("duration", activity.getElapsedTime());
        }

        if (activity.getMovingTime() != null) {
            jsonBody.put("time", activity.getMovingTime());
        }

        if (activity.getDistance() != null) {
            jsonBody.put("distance", activity.getDistance() / 1000.0);
        }

        if (activity.getTotalElevationGain() != null) {
            jsonBody.put("elevation_gain", activity.getTotalElevationGain());
        }

        // -- Metadatos --
        if (activity.getName() != null) {
            jsonBody.put("description", activity.getName());
        }
        
        if (activity.getDescription() != null) {
            jsonBody.put("notes", activity.getDescription());
        }
        
        if (activity.getSportType() != null) {
            jsonBody.put("type", mapStravaTypeToCyclingAnalytics(activity.getSportType()));
        }

        // -- Datos de Rendimiento (Resumen) --
        if (activity.getAverageSpeed() != null) {
            jsonBody.put("average_speed", activity.getAverageSpeed() * 3.6);
        }
        
        if (activity.getAverageWatts() != null) {
            jsonBody.put("average_power", activity.getAverageWatts());
        }
        
        if (activity.getMaxWatts() != null) {
            jsonBody.put("max_power", activity.getMaxWatts());
        }
        
        if (activity.getAverageCadence() != null) {
            jsonBody.put("average_cadence", activity.getAverageCadence());
        }
        
        if (activity.getAverageTemp() != null) {
            jsonBody.put("temperature", activity.getAverageTemp());
        }

        String requestBody = objectMapper.writeValueAsString(jsonBody);

        // 2. Crear la petición HTTP POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + this.apiToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // 3. Enviar y recibir respuesta
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new IOException("Error al subir a Cycling Analytics: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Mapea tipos de deporte de Strava a los tipos usados por Cycling Analytics.
     *
     * @param stravaType Tipo de actividad de Strava
     * @return Tipo equivalente en Cycling Analytics
     */
    private String mapStravaTypeToCyclingAnalytics(String stravaType) {
        if (stravaType == null) return "ride";
        
        switch (stravaType.toLowerCase()) {
            case "run": return "run";
            case "swim": return "swim";
            case "virtualride": return "virtual_ride";
            case "mountainbikeride": return "mtb_ride"; 
            default: return "ride";
        }
    }
}
