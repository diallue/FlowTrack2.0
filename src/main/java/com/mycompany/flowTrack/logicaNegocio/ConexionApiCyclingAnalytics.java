/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.logicaNegocio;

import com.google.gson.JsonObject;
import com.mycompany.flowTrack.model.Activity;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.format.DateTimeFormatter;

/**
 * @file ConexionApiCyclingAnalytics.java
 * @brief Clase para interactuar con la API de Cycling Analytics.
 * @details
 * Esta clase gestiona la conexión y el envío de datos a la API de
 * Cycling Analytics (cyclinganalytics.com).
 * Utiliza un objeto {@link com.mycompany.flowTrack.model.Activity} (basado en el
 * modelo de Strava) como fuente de datos, lo transforma al formato JSON
 * esperado por Cycling Analytics y lo envía para obtener analíticas.
 *
 * @author diego
 * @author ignacio
 * @author alvaro
 *
 * @date 2025
 * @version 1.0
 */
public class ConexionApiCyclingAnalytics {

    /**
     * @brief URL base de la API de Cycling Analytics
     */
    private static final String API_BASE_URL = "https://www.cyclinganalytics.com/api";

    /**
     * @brief Token de autorización (API Key) para Cycling Analytics
     * @details Se debe obtener de la configuración de la cuenta del usuario
     */
    private final String apiToken;

    /**
     * @brief Cliente HTTP para realizar las peticiones a la API
     * @details Se reutiliza el cliente para mejorar la eficiencia
     */
    private final HttpClient httpClient;

    /**
     * @brief Constructor de la clase
     * @details
     * Inicializa la conexión con la API de Cycling Analytics.
     * Requiere el token de API del usuario para poder autenticar las peticiones.
     *
     * @param apiToken El token de API (Bearer Token) del usuario de Cycling
     * Analytics.
     */
    public ConexionApiCyclingAnalytics(String apiToken) {
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    /**
     * @brief Envía una actividad a Cycling Analytics y obtiene la analítica
     * resultante.
     * @details
     * Este método toma un objeto `Activity` (del modelo Strava), lo
     * transforma a un JSON compatible con el endpoint `POST /api/json_ride`
     * de Cycling Analytics, y lo envía. La API procesa la actividad y
     * devuelve un JSON con la actividad creada y sus analíticas.
     *
     * @param actividad El objeto {@link Activity} que se desea enviar.
     * @return Un String que contiene la respuesta JSON de la API de Cycling
     * Analytics.
     * @throws Exception Si ocurre un error durante la petición HTTP (ej.
     * autenticación fallida, error de red).
     */
    public String enviarActividadYObtenerAnalitica(Activity actividad) throws Exception {

        // 1. Transformar el objeto Activity al JSON de Cycling Analytics
        JsonObject payload = transformarActivityAJson(actividad);

        // 2. Construir la petición HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/json_ride"))
                .header("Authorization", "Bearer " + this.apiToken)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(payload.toString()))
                .build();

        // 3. Enviar la petición y obtener la respuesta
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        // 4. Manejar la respuesta
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            // Éxito, devuelve el JSON de la actividad creada
            return response.body();
        } else {
            // Error, lanza una excepción con el mensaje de error
            throw new Exception("Error al enviar la actividad a Cycling Analytics: "
                    + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * @brief Método privado para convertir un objeto Activity a un JsonObject.
     * @details
     * Mapea los campos del objeto `Activity` (modelo Strava) a los
     * campos esperados por el endpoint `POST /api/json_ride` de Cycling
     * Analytics.
     *
     * @note La clase `Activity` es un resumen y carece de "streams"
     * (datos segundo a segundo de lat, lng, hr, cad, etc.). Se envían
     * solo los datos de resumen disponibles.
     *
     * @param a El objeto Activity a transformar.
     * @return Un JsonObject listo para ser enviado como payload.
     */
    private JsonObject transformarActivityAJson(Activity a) {
        JsonObject json = new JsonObject();

        // --- Mapeo de campos obligatorios/principales ---
        
        // Fecha y hora: La API de CA prefiere ISO 8601 Instant (UTC)
        if (a.getStartDate() != null) {
            json.addProperty("start_time", a.getStartDate().format(DateTimeFormatter.ISO_INSTANT));
        }
        
        // Zona horaria
        if (a.getTimezone() != null) {
            json.addProperty("timezone", a.getTimezone());
        }

        // Tiempos (la API de CA espera 'duration' en segundos, usamos elapsedTime)
        if (a.getElapsedTime() != null) {
            json.addProperty("duration", a.getElapsedTime()); // Tiempo total
        }
        if (a.getMovingTime() != null) {
            // 'time' en CA se refiere al tiempo en movimiento
            json.addProperty("time", a.getMovingTime()); 
        }

        // Distancia (la API de CA espera 'distance' en km)
        if (a.getDistanceInKm() != null) {
            json.addProperty("distance", a.getDistanceInKm());
        }

        // Elevación (la API de CA espera 'elevation_gain' en metros)
        if (a.getTotalElevationGain() != null) {
            json.addProperty("elevation_gain", a.getTotalElevationGain());
        }

        // --- Mapeo de campos de resumen (opcionales) ---
        
        // Nombre y descripción
        if (a.getName() != null) {
            json.addProperty("description", a.getName()); // Usamos 'description' para el título
        }
        if (a.getDescription() != null) {
            json.addProperty("notes", a.getDescription()); // 'notes' para la descripción larga
        }

        // Velocidades (la API de CA espera km/h)
        if (a.getAverageSpeedKmh() != null) {
            json.addProperty("average_speed", a.getAverageSpeedKmh());
        }
        if (a.getMaxSpeedKmh() != null) {
            json.addProperty("max_speed", a.getMaxSpeedKmh());
        }

        // Potencia (Watts)
        if (a.getAverageWatts() != null) {
            json.addProperty("average_watts", a.getAverageWatts());
        }
        if (a.getMaxWatts() != null) {
            json.addProperty("max_watts", a.getMaxWatts());
        }
        if (a.getWeightedAverageWatts() != null) {
            // CA lo llama 'normalised_power' (NP)
            json.addProperty("normalised_power", a.getWeightedAverageWatts()); 
        }

        // Cadencia
        if (a.getAverageCadence() != null) {
            // CA espera 'average_cadence' en rpm (asumimos que ya lo es)
            json.addProperty("average_cadence", a.getAverageCadence());
        }

        // Calorías
        if (a.getCalories() != null) {
            json.addProperty("calories", a.getCalories());
        }
        
        // Tipo de actividad
        if (a.getSportType() != null) {
            // La API de CA usa sus propios tipos (ej. "ride", "run", "virtual_ride")
            // Esto requeriría un mapeo (ej. "VirtualRide" de Strava -> "virtual_ride" de CA)
            // Por simplicidad, enviamos el tipo de Strava
            json.addProperty("type", a.getSportType()); 
        }

        // NOTA: Los 'streams' (flujos de datos) como 'time_stream', 'lat_stream',
        // 'lng_stream', 'heartrate_stream', 'power_stream', etc., no se envían
        // porque no están presentes en el objeto Activity de resumen.
        
        return json;
    }

}