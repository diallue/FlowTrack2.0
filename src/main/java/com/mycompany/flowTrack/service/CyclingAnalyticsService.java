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

/**
 * Servicio encargado de la comunicación con la API de Cycling Analytics.
 * Se utiliza para subir actividades y recuperar datos de análisis.
 */
public class CyclingAnalyticsService {
    
    private static final String API_BASE_URL = "https://www.cyclinganalytics.com/api";
    
    private final String apiToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructor del servicio.
     * Inicializa el cliente HTTP y el parser JSON.
     *
     * @param apiToken El token de acceso a la API de Cycling Analytics.
     */
    public CyclingAnalyticsService(String apiToken) {
        this.apiToken = apiToken;
        // Configuración del cliente HTTP con timeout y versión HTTP 1.1.
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sube una actividad a Cycling Analytics para su análisis.
     * Flujo:
     * 1. Convierte los streams de Strava (JSON) a CSV.
     * 2. Construye y envía una petición multipart/form-data con el archivo.
     * 3. Gestiona respuestas: éxito, error, o actividad duplicada.
     *
     * @param activity Datos básicos de la actividad.
     * @param stravaStreamsJson String JSON crudo con los streams exportados desde Strava.
     * @return JSON con el campo "summary" de la respuesta de Cycling Analytics.
     */
    public String uploadActivity(Activity activity, String stravaStreamsJson) throws IOException, InterruptedException {
        // Genera el CSV requerido por CA a partir de los streams de Strava.
        String csvContent = convertStreamsToCsv(stravaStreamsJson);
        if (csvContent.isEmpty()) throw new IOException("No hay streams para generar CSV");

        String filename = "strava_" + activity.getId() + ".csv";
        // Boundary para multipart unique por request
        String boundary = "---Bound" + UUID.randomUUID().toString();
        // Cuerpo multipart con el archivo CSV
        String body = buildMultipartBody(boundary, csvContent, filename);

        // Construcción de la request HTTP POST hacia CA
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/me/rides"))
                .header("Authorization", "Bearer " + this.apiToken)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        // Ejecución de la request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Caso éxito (status 200-299)
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return processSuccessResponse(response.body()); 
        } 
        
        // Caso duplicado o error 400
        else if (response.statusCode() == 400) {
            String errorBody = response.body();

            // Si es un duplicado, CA devuelve "duplicate_ride"
            if (errorBody.contains("duplicate_ride")) {
                String existingRideId = extractRideIdFromError(errorBody);
                if (existingRideId != null) {
                    System.out.println("Actividad duplicada. Recuperando datos existentes para ID: " + existingRideId);
                    return getExistingRideAnalysis(existingRideId);
                }
            }
            throw new IOException("Error CA (400): " + errorBody);
        } 
        
        // Otros errores (401, 500, etc)
        else {
            throw new IOException("Error CA (" + response.statusCode() + "): " + response.body());
        }
    }

    /**
     * Recupera datos de análisis para una actividad ya existente en Cycling Analytics.
     * Usado cuando la API nos informa que la actividad es un duplicado.
     *
     * @param rideId ID interno de CA para la actividad.
     */
    private String getExistingRideAnalysis(String rideId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/ride/" + rideId))
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
     * Procesa la respuesta JSON de CA y devuelve únicamente el objeto "summary".
     * Además inyecta:
     *  - ID de la actividad en CA
     *  - curva de potencia (si está disponible)
     */
    private String processSuccessResponse(String jsonResponse) throws IOException {
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        // La API devuelve gran cantidad de datos, pero el análisis está dentro de "summary".
        if (root.has("summary")) {
            JsonNode summary = root.get("summary");
            
            if (summary.isObject()) {
                // Añadir ID de CA (root.id incluido en el JSON)
                ((ObjectNode) summary).put("id", root.path("id").asLong());
                // Inyectar power_curve si existe en la respuesta completa
                if (root.has("power_curve")) {
                    ((ObjectNode) summary).set("power_curve", root.get("power_curve"));
                }
            }
            return summary.toString();
        }
        
        // Si no hay summary, se devuelve el JSON completo.
        return jsonResponse;
    }

    /**
     * Extrae el ID numérico de una actividad duplicada a partir del JSON de error.
     * Cycling Analytics no devuelve un campo explícito, así que se busca mediante regex.
     */
    private String extractRideIdFromError(String errorJson) {
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(errorJson);
        String foundId = null;

        // Se asume que los IDs válidos son números largos (más de 5 dígitos)
        while(m.find()) {
            String match = m.group(1);
            if (match.length() > 5) { 
                foundId = match;
            }
        }
        return foundId;
    }

    // --- MÉTODOS DE CONVERSIÓN DE STREAMS DE STRAVA (JSON) A CSV ---
    
    /**
     * Convierte los streams de Strava a un CSV compatible con Cycling Analytics.
     * CA requiere columnas: Time,Power,Heart Rate,Cadence,Elevation,Speed
     */
    private String convertStreamsToCsv(String jsonStreams) throws IOException {
        JsonNode root = objectMapper.readTree(jsonStreams);

        // Extrae listas desde el JSON
        List<Integer> times = getStreamDataInt(root, "time");
        List<Integer> watts = getStreamDataInt(root, "watts");
        List<Integer> hr = getStreamDataInt(root, "heartrate");
        List<Integer> cad = getStreamDataInt(root, "cadence");
        List<Double> alt = getStreamDataDouble(root, "altitude");
        List<Double> speed = getStreamDataDouble(root, "velocity_smooth");

        // Sin datos de tiempo, no se genera CSV
        if (times == null || times.isEmpty()) return "";

        StringBuilder csv = new StringBuilder();
        csv.append("Time,Power,Heart Rate,Cadence,Elevation,Speed\n");

        // Construcción línea a línea del CSV
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
    
    /**
     * Devuelve un valor de la lista o "0" si no existe.
     * Se usa para evitar nulls en el CSV final.
     */
    private String getValue(List<?> list, int index) {
        if (list != null && index < list.size() && list.get(index) != null) return String.valueOf(list.get(index));
        return "0";
    }

    /**
     * Extrae un stream de enteros del JSON.
     */
    private List<Integer> getStreamDataInt(JsonNode root, String key) {
        if (root.has(key) && root.get(key).has("data")) {
            try { 
                return objectMapper.convertValue(
                    root.get(key).get("data"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class)
                ); 
            } catch (Exception e) { 
                return null; 
            }
        }
        return null;
    }
    
    /**
     * Extrae un stream de doubles del JSON.
     */
    private List<Double> getStreamDataDouble(JsonNode root, String key) {
        if (root.has(key) && root.get(key).has("data")) {
            try { 
                return objectMapper.convertValue(
                    root.get(key).get("data"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class)
                ); 
            } catch (Exception e) { 
                return null; 
            }
        }
        return null;
    }

    /**
     * Construye una petición multipart/form-data manualmente.
     * CA requiere el archivo CSV asociado al campo "data".
     */
    private String buildMultipartBody(String boundary, String csvContent, String filename) {
        StringBuilder builder = new StringBuilder();
        String crlf = "\r\n"; 

        builder.append("--").append(boundary).append(crlf);
        builder.append("Content-Disposition: form-data; name=\"data\"; filename=\"").append(filename).append("\"").append(crlf);
        builder.append("Content-Type: text/csv").append(crlf);
        builder.append(crlf);
        builder.append(csvContent).append(crlf);

        builder.append("--").append(boundary).append(crlf);
        builder.append("Content-Disposition: form-data; name=\"format\"").append(crlf).append(crlf);
        builder.append("csv").append(crlf);

        builder.append("--").append(boundary).append("--").append(crlf);

        return builder.toString();
    }
}
