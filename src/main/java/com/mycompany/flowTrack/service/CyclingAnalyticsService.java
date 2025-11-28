package com.mycompany.flowTrack.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.flowTrack.model.Activity;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class CyclingAnalyticsService {
    
    // Endpoint oficial para subir archivos
    private static final String API_URL = "https://www.cyclinganalytics.com/api/post_ride";
    private final String apiToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CyclingAnalyticsService(String apiToken) {
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Convierte los streams de Strava a CSV y los sube a Cycling Analytics.
     */
    public String uploadActivity(Activity activity, String stravaStreamsJson) throws IOException, InterruptedException {
        
        // 1. Convertir JSON de Strava Streams a formato CSV
        String csvContent = convertStreamsToCsv(stravaStreamsJson);
        String filename = "strava_activity_" + activity.getId() + ".csv";

        // 2. Preparar el cuerpo Multipart (Simulación de subida de archivo)
        String boundary = "---" + UUID.randomUUID().toString();
        String body = buildMultipartBody(boundary, csvContent, filename);

        // 3. Crear petición HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + this.apiToken)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        // 4. Enviar
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new IOException("Error CA (" + response.statusCode() + "): " + response.body());
        }
    }

    /**
     * Convierte el JSON de streams de Strava (arrays separados) en un CSV (columnas).
     */
    private String convertStreamsToCsv(String jsonStreams) throws IOException {
        JsonNode root = objectMapper.readTree(jsonStreams);
        
        // Strava devuelve un array de objetos: [{"type":"time", "data":[...]}, {"type":"watts", ...}]
        // O un mapa si usaste key_by_type=true. Asumimos key_by_type=true por tu código anterior.
        
        List<Integer> times = getStreamDataInt(root, "time");
        List<Integer> watts = getStreamDataInt(root, "watts");
        List<Integer> hr = getStreamDataInt(root, "heartrate");
        List<Integer> cad = getStreamDataInt(root, "cadence");
        List<Double> latlng = null; // CSV de CA prefiere datos simples, lat/lng es complejo en CSV simple, lo omitimos para análisis de potencia/hr

        if (times == null || times.isEmpty()) return "";

        StringBuilder csv = new StringBuilder();
        // Cabecera que entiende Cycling Analytics
        csv.append("time,power,heart_rate,cadence\n");

        for (int i = 0; i < times.size(); i++) {
            // Tiempo (segundos)
            csv.append(times.get(i)); 
            csv.append(",");
            
            // Potencia
            if (watts != null && i < watts.size()) csv.append(watts.get(i));
            csv.append(",");
            
            // Pulso
            if (hr != null && i < hr.size()) csv.append(hr.get(i));
            csv.append(",");
            
            // Cadencia
            if (cad != null && i < cad.size()) csv.append(cad.get(i));
            csv.append("\n");
        }
        
        return csv.toString();
    }

    // Helper para extraer listas de enteros del JSON
    private List<Integer> getStreamDataInt(JsonNode root, String key) {
        if (root.has(key) && root.get(key).has("data")) {
            try {
                return objectMapper.convertValue(root.get(key).get("data"), objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));
            } catch (Exception e) { return null; }
        }
        return null;
    }

    /**
     * Construye el cuerpo "multipart/form-data" manualmente para no depender de librerías extra.
     */
    private String buildMultipartBody(String boundary, String csvContent, String filename) {
        StringBuilder builder = new StringBuilder();

        // Parte del archivo
        builder.append("--").append(boundary).append("\r\n");
        builder.append("Content-Disposition: form-data; name=\"data_file\"; filename=\"").append(filename).append("\"\r\n");
        builder.append("Content-Type: text/csv\r\n\r\n");
        builder.append(csvContent).append("\r\n");

        // Parte del formato (opcional, ayuda a CA a detectar que es CSV)
        builder.append("--").append(boundary).append("\r\n");
        builder.append("Content-Disposition: form-data; name=\"format\"\r\n\r\n");
        builder.append("csv\r\n");

        // Cierre
        builder.append("--").append(boundary).append("--\r\n");
        return builder.toString();
    }
}