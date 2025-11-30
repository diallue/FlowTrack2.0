/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.flowTrack.model.Activity;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CyclingAnalyticsService {
    
    private static final String API_BASE_URL = "https://www.cyclinganalytics.com/api";
    
    private final String apiToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CyclingAnalyticsService(String apiToken) {
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String uploadActivity(Activity activity, String stravaStreamsJson) throws IOException, InterruptedException {
        // 1. Intentar subir la actividad (POST)
        String csvContent = convertStreamsToCsv(stravaStreamsJson);
        if (csvContent.isEmpty()) throw new IOException("No hay streams para generar CSV");

        String filename = "strava_" + activity.getId() + ".csv";
        String boundary = "---Bound" + UUID.randomUUID().toString();
        String body = buildMultipartBody(boundary, csvContent, filename);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/me/rides"))
                .header("Authorization", "Bearer " + this.apiToken)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // A. Éxito normal (200-299)
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return processSuccessResponse(response.body()); 
        } 
        
        // B. Error 400: Posible duplicado
        else if (response.statusCode() == 400) {
            String errorBody = response.body();
            // Verificamos si es el error "duplicate_ride"
            if (errorBody.contains("duplicate_ride")) {
                // Extraemos el ID del mensaje: "The ride already exists: 12345"
                String existingRideId = extractRideIdFromError(errorBody);
                if (existingRideId != null) {
                    System.out.println("Actividad duplicada. Recuperando datos existentes para ID: " + existingRideId);
                    return getExistingRideAnalysis(existingRideId);
                }
            }
            throw new IOException("Error CA (400): " + errorBody);
        } 
        
        // C. Otros errores
        else {
            throw new IOException("Error CA (" + response.statusCode() + "): " + response.body());
        }
    }

    /**
     * Recupera los datos de una actividad que ya existe en CA.
     */
    private String getExistingRideAnalysis(String rideId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/ride/" + rideId)) //
                .header("Authorization", "Bearer " + this.apiToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return processSuccessResponse(response.body());
        } else {
            throw new IOException("Error recuperando duplicado (" + response.statusCode() + ")");
        }
    }

    /**
     * Procesa la respuesta JSON de CA para devolver solo la parte 'summary' 
     * que es la que mapea con nuestra clase AnalysisResults.
     */
    private String processSuccessResponse(String jsonResponse) throws IOException {
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        // La API de CA devuelve un objeto grande, las métricas están en 'summary'
        // O a veces en la raíz si es un create. Verificamos.
        if (root.has("summary")) {
            JsonNode summary = root.get("summary");
            // Si queremos mantener el ID de la actividad, lo inyectamos en el summary
            if (summary.isObject()) {
                ((ObjectNode) summary).put("id", root.path("id").asLong());
                // Inyectamos también la curva de potencia si está disponible en la raíz
                if (root.has("power_curve")) {
                    ((ObjectNode) summary).set("power_curve", root.get("power_curve"));
                }
            }
            return summary.toString();
        }
        
        return jsonResponse; // Si no hay summary, devolvemos tal cual
    }

    private String extractRideIdFromError(String errorJson) {
        // Buscamos un patrón numérico después de "exists:" o al final
        // Ejemplo: "The ride already exists: 102107402665"
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(errorJson);
        // Buscamos la secuencia de dígitos más larga (el ID)
        String foundId = null;
        while(m.find()) {
            String match = m.group(1);
            if (match.length() > 5) { // Los IDs suelen ser largos
                foundId = match;
            }
        }
        return foundId;
    }

    // --- MÉTODOS DE CSV (Iguales que antes, no cambiar) ---
    
    private String convertStreamsToCsv(String jsonStreams) throws IOException {
        JsonNode root = objectMapper.readTree(jsonStreams);
        List<Integer> times = getStreamDataInt(root, "time");
        List<Integer> watts = getStreamDataInt(root, "watts");
        List<Integer> hr = getStreamDataInt(root, "heartrate");
        List<Integer> cad = getStreamDataInt(root, "cadence");
        List<Double> alt = getStreamDataDouble(root, "altitude");
        List<Double> speed = getStreamDataDouble(root, "velocity_smooth");

        if (times == null || times.isEmpty()) return "";

        StringBuilder csv = new StringBuilder();
        csv.append("Time,Power,Heart Rate,Cadence,Elevation,Speed\n");

        for (int i = 0; i < times.size(); i++) {
            csv.append(times.get(i)).append(",");
            csv.append(getValue(watts, i)).append(",");
            csv.append(getValue(hr, i)).append(",");
            csv.append(getValue(cad, i)).append(",");
            csv.append(getValue(alt, i)).append(",");
            csv.append(getValue(speed, i));
            csv.append("\n");
        }
        return csv.toString();
    }
    
    private String getValue(List<?> list, int index) {
        if (list != null && index < list.size() && list.get(index) != null) return String.valueOf(list.get(index));
        return "0";
    }

    private List<Integer> getStreamDataInt(JsonNode root, String key) {
        if (root.has(key) && root.get(key).has("data")) {
            try { return objectMapper.convertValue(root.get(key).get("data"), objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class)); } catch (Exception e) { return null; }
        }
        return null;
    }
    
    private List<Double> getStreamDataDouble(JsonNode root, String key) {
        if (root.has(key) && root.get(key).has("data")) {
            try { return objectMapper.convertValue(root.get(key).get("data"), objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class)); } catch (Exception e) { return null; }
        }
        return null;
    }

    private String buildMultipartBody(String boundary, String csvContent, String filename) {
        StringBuilder builder = new StringBuilder();
        String crlf = "\r\n";
        builder.append("--").append(boundary).append(crlf);
        builder.append("Content-Disposition: form-data; name=\"data\"; filename=\"").append(filename).append("\"").append(crlf);
        builder.append("Content-Type: text/csv").append(crlf).append(crlf);
        builder.append(csvContent).append(crlf);
        builder.append("--").append(boundary).append(crlf);
        builder.append("Content-Disposition: form-data; name=\"format\"").append(crlf).append(crlf);
        builder.append("csv").append(crlf);
        builder.append("--").append(boundary).append("--").append(crlf);
        return builder.toString();
    }
}