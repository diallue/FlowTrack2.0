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
     * @param apiToken El token de acceso a la API de Cycling Analytics.
     */
    public CyclingAnalyticsService(String apiToken) {
        this.apiToken = apiToken;
        // Configura el cliente HTTP para realizar las peticiones.
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sube una actividad a Cycling Analytics para su análisis.
     * Primero convierte los streams de Strava (JSON) a formato CSV y luego realiza la petición multipart/form-data.
     * Maneja casos de éxito, errores y actividades duplicadas.
     * 
     * @param activity Datos básicos de la actividad.
     * @param stravaStreamsJson String JSON crudo de los streams de Strava.
     * @return String JSON con los resultados del análisis (resumen) de Cycling Analytics.
     */
    public String uploadActivity(Activity activity, String stravaStreamsJson) throws IOException, InterruptedException {
        // 1. Convertir datos de Strava a CSV para la API de CA
        String csvContent = convertStreamsToCsv(stravaStreamsJson);
        if (csvContent.isEmpty()) throw new IOException("No hay streams para generar CSV");

        String filename = "strava_" + activity.getId() + ".csv";
        // Genera un boundary único para la petición multipart/form-data.
        String boundary = "---Bound" + UUID.randomUUID().toString();
        // Construye el cuerpo de la petición HTTP con el archivo CSV incrustado.
        String body = buildMultipartBody(boundary, csvContent, filename);

        // Construye la petición POST a la API de CA.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/me/rides"))
                .header("Authorization", "Bearer " + this.apiToken) // Autenticación con Bearer token
                .header("Content-Type", "multipart/form-data; boundary=" + boundary) // Indica tipo multipart
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        // Envía la petición y obtiene la respuesta.
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // A. Éxito normal (200-299)
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            // Procesa la respuesta para extraer solo el resumen del análisis.
            return processSuccessResponse(response.body()); 
        } 
        
        // B. Error 400: Posible duplicado
        else if (response.statusCode() == 400) {
            String errorBody = response.body();
            // Verificamos si el error específico es "duplicate_ride"
            if (errorBody.contains("duplicate_ride")) {
                // Si es un duplicado, extraemos el ID de la actividad existente.
                String existingRideId = extractRideIdFromError(errorBody);
                if (existingRideId != null) {
                    System.out.println("Actividad duplicada. Recuperando datos existentes para ID: " + existingRideId);
                    // Recuperamos los datos de análisis de la actividad ya subida.
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
     * Recupera los datos de análisis de una actividad que ya existe en Cycling Analytics usando su ID interno de CA.
     */
    private String getExistingRideAnalysis(String rideId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/ride/" + rideId)) // Endpoint para obtener detalles de un ride específico.
                .header("Authorization", "Bearer " + this.apiToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Procesa la respuesta para extraer solo el resumen del análisis.
            return processSuccessResponse(response.body());
        } else {
            throw new IOException("Error recuperando duplicado (" + response.statusCode() + ")");
        }
    }

    /**
     * Procesa la respuesta JSON de CA para devolver solo la parte 'summary' 
     * (que es la que mapea con nuestra clase AnalysisResults) e inyecta campos útiles adicionales.
     */
    private String processSuccessResponse(String jsonResponse) throws IOException {
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        // La API de CA puede devolver un objeto grande; las métricas clave están en 'summary'.
        if (root.has("summary")) {
            JsonNode summary = root.get("summary");
            
            // Si el 'summary' es un objeto (lo normal), inyectamos el ID de la actividad y la curva de potencia si están disponibles en la raíz.
            if (summary.isObject()) {
                // Inyectamos el ID de CA en el summary para que el frontend pueda usarlo.
                ((ObjectNode) summary).put("id", root.path("id").asLong());
                // Inyectamos también la curva de potencia si está disponible en la respuesta raíz.
                if (root.has("power_curve")) {
                    ((ObjectNode) summary).set("power_curve", root.get("power_curve"));
                }
            }
            return summary.toString();
        }
        
        return jsonResponse; // Si no hay summary, devolvemos el JSON crudo tal cual.
    }

    /**
     * Extrae el ID numérico de la actividad duplicada a partir del mensaje de error JSON de CA.
     */
    private String extractRideIdFromError(String errorJson) {
        // Busca un patrón numérico (secuencia de dígitos) dentro del mensaje.
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(errorJson);
        String foundId = null;
        while(m.find()) {
            String match = m.group(1);
            // Asume que IDs largos (ej. > 5 dígitos) son el ID de la actividad.
            if (match.length() > 5) { 
                foundId = match;
            }
        }
        return foundId;
    }

    // --- MÉTODOS DE CONVERSIÓN DE STREAMS DE STRAVA (JSON) A CSV ---
    
    /**
     * Convierte el JSON de streams de Strava a un formato CSV compatible con Cycling Analytics.
     */
    private String convertStreamsToCsv(String jsonStreams) throws IOException {
        JsonNode root = objectMapper.readTree(jsonStreams);
        // Extrae los arrays de datos para cada métrica.
        List<Integer> times = getStreamDataInt(root, "time");
        List<Integer> watts = getStreamDataInt(root, "watts");
        List<Integer> hr = getStreamDataInt(root, "heartrate");
        List<Integer> cad = getStreamDataInt(root, "cadence");
        List<Double> alt = getStreamDataDouble(root, "altitude");
        List<Double> speed = getStreamDataDouble(root, "velocity_smooth");

        if (times == null || times.isEmpty()) return "";

        StringBuilder csv = new StringBuilder();
        // Define la cabecera CSV requerida por CA.
        csv.append("Time,Power,Heart Rate,Cadence,Elevation,Speed\n");

        // Itera sobre los datos y construye las filas del CSV.
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
     * Helper para obtener un valor de una lista de streams, usando "0" como valor por defecto si no existe o es nulo.
     */
    private String getValue(List<?> list, int index) {
        if (list != null && index < list.size() && list.get(index) != null) return String.valueOf(list.get(index));
        return "0"; // Valor por defecto para datos faltantes.
    }

    /**
     * Helper para extraer un array de enteros de un nodo JSON de streams.
     */
    private List<Integer> getStreamDataInt(JsonNode root, String key) {
        if (root.has(key) && root.get(key).has("data")) {
            try { return objectMapper.convertValue(root.get(key).get("data"), objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class)); } catch (Exception e) { return null; }
        }
        return null;
    }
    
    /**
     * Helper para extraer un array de doubles de un nodo JSON de streams.
     */
    private List<Double> getStreamDataDouble(JsonNode root, String key) {
        if (root.has(key) && root.get(key).has("data")) {
            try { return objectMapper.convertValue(root.get(key).get("data"), objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class)); } catch (Exception e) { return null; }
        }
        return null;
    }

    /**
     * Construye el cuerpo de la petición HTTP multipart/form-data.
     */
    private String buildMultipartBody(String boundary, String csvContent, String filename) {
        StringBuilder builder = new StringBuilder();
        String crlf = "\r\n"; // El estándar HTTP requiere CRLF (retorno de carro + salto de línea)
        builder.append("--").append(boundary).append(crlf);
        // Define la disposición del contenido como un archivo con un nombre específico.
        builder.append("Content-Disposition: form-data; name=\"data\"; filename=\"").append(filename).append("\"").append(crlf);
        builder.append("Content-Type: text/csv").append(crlf); // Indica el tipo de contenido que se envía.
        builder.append(crlf); // Línea vacía requerida antes del contenido real del archivo.
        builder.append(csvContent).append(crlf);
        builder.append("--").append(boundary).append(crlf);
        builder.append("Content-Disposition: form-data; name=\"format\"").append(crlf).append(crlf);
        builder.append("csv").append(crlf);
        builder.append("--").append(boundary).append("--").append(crlf); // Marcador de cierre de la petición.
        return builder.toString();
    }
}
























