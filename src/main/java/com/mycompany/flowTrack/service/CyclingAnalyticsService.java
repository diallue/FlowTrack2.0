/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
 *
 * @author diego
 */
public class CyclingAnalyticsService {
    private static final String API_URL = "https://www.cyclinganalytics.com/api/json_ride";
    private final String apiToken; // Token de Cycling Analytics (NO el de Strava)
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CyclingAnalyticsService(String apiToken) {
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Envía una actividad de Strava a Cycling Analytics.
     * @param activity El objeto Activity obtenido de Strava.
     * @return La respuesta JSON de Cycling Analytics (contiene el ID de la nueva actividad y datos básicos).
     */
    public String uploadActivity(Activity activity) throws IOException, InterruptedException {
        
        // 1. Construir el JSON específico que pide Cycling Analytics
        // Usamos ObjectNode para construir el JSON manualmente y asegurar los nombres de campos correctos
        ObjectNode jsonBody = objectMapper.createObjectNode();

        // -- Campos Obligatorios / Importantes --
        
        // Fecha de inicio (Formato ISO)
        if (activity.getStartDate() != null) {
            jsonBody.put("start_time", activity.getStartDate().format(DateTimeFormatter.ISO_INSTANT));
        }
        
        // Zona horaria
        if (activity.getTimezone() != null) {
            jsonBody.put("timezone", activity.getTimezone());
        }

        // Tiempos (En segundos)
        // 'duration' es el tiempo total transcurrido
        if (activity.getElapsedTime() != null) {
            jsonBody.put("duration", activity.getElapsedTime());
        }
        // 'time' es el tiempo en movimiento (moving time)
        if (activity.getMovingTime() != null) {
            jsonBody.put("time", activity.getMovingTime());
        }

        // Distancia (Cycling Analytics suele esperar Km, pero acepta metros si no se especifica unidad, 
        // aunque es mejor convertir a KM si tu lógica anterior lo hacía).
        if (activity.getDistance() != null) {
            jsonBody.put("distance", activity.getDistance() / 1000.0); // Convertimos Metros a KM
        }

        // Elevación (Metros)
        if (activity.getTotalElevationGain() != null) {
            jsonBody.put("elevation_gain", activity.getTotalElevationGain());
        }

        // -- Metadatos --
        if (activity.getName() != null) {
            jsonBody.put("description", activity.getName()); // El título
        }
        
        if (activity.getDescription() != null) {
            jsonBody.put("notes", activity.getDescription());
        }
        
        if (activity.getSportType() != null) {
            jsonBody.put("type", mapStravaTypeToCyclingAnalytics(activity.getSportType()));
        }

        // -- Datos de Rendimiento (Resumen) --
        if (activity.getAverageSpeed() != null) {
            // Strava da m/s, pasamos a km/h
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

        // Convertir el objeto JSON a String
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
            return response.body(); // Devuelve el JSON de éxito
        } else {
            throw new IOException("Error al subir a Cycling Analytics: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Mapeo simple de tipos de deporte de Strava a Cycling Analytics.
     */
    private String mapStravaTypeToCyclingAnalytics(String stravaType) {
        if (stravaType == null) return "ride";
        
        switch (stravaType.toLowerCase()) {
            case "run": return "run";
            case "swim": return "swim";
            case "virtualride": return "virtual_ride";
            case "mountainbikeride": return "mtb_ride"; // Ejemplo hipotético, verificar API de CA
            default: return "ride";
        }
    }
}
