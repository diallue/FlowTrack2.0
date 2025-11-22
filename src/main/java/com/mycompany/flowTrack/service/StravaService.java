/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mycompany.flowTrack.model.Activity;
import com.mycompany.flowTrack.model.Athletes;

import java.net.http.HttpClient;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author diego
 */
public class StravaService {
    private final String clientId;
    private final String clientSecret;
    private final ObjectMapper objectMapper;
    private final HttpClient http;

    // AHORA LAS CREDENCIALES SE PASAN EN EL CONSTRUCTOR
    public StravaService(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .findAndRegisterModules();

        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    // ---------------------------------------------------------------------
    // OAuth token endpoints
    // ---------------------------------------------------------------------

    /**
     * Intercambia el authorization code por tokens (access_token, refresh_token).
     * Devuelve un TokenResponse o lanza IOException si hay fallo.
     * @param redirectUri
     */
    public TokenResponse exchangeCodeForToken(String code, String redirectUri) throws IOException, InterruptedException {
        String body = "client_id=" + urlEncode(clientId)
                + "&client_secret=" + urlEncode(clientSecret)
                + "&code=" + urlEncode(code)
                + "&grant_type=authorization_code"
                + "&redirect_uri=" + urlEncode(redirectUri);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://www.strava.com/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new IOException("Error exchanging code for token. HTTP " + res.statusCode() + " - " + res.body());
        }
        // Jackson parseará la respuesta en tu clase TokenResponse
        return objectMapper.readValue(res.body(), TokenResponse.class);
    }

    /**
     * Refresca el access token usando refresh_token.
     */
    public TokenResponse refreshAccessToken(String refreshToken) throws IOException, InterruptedException {
        String body = "client_id=" + urlEncode(clientId)
                + "&client_secret=" + urlEncode(clientSecret)
                + "&grant_type=refresh_token"
                + "&refresh_token=" + urlEncode(refreshToken);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://www.strava.com/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new IOException("Error refreshing token. HTTP " + res.statusCode() + " - " + res.body());
        }
        return objectMapper.readValue(res.body(), TokenResponse.class);
    }

    // ---------------------------------------------------------------------
    // Activity endpoints
    // ---------------------------------------------------------------------

    /**
     * Obtiene actividades del atleta autenticado.
     * perPage: número de resultados por página (max 200 según Strava docs).
     * page: página (1..n)
     */
    public List<Activity> getActivities(String accessToken, int perPage, int page, Long before, Long after) throws IOException, InterruptedException {
        // Construimos la URL base
        StringBuilder urlBuilder = new StringBuilder("https://www.strava.com/api/v3/athlete/activities");
        urlBuilder.append("?per_page=").append(perPage);
        urlBuilder.append("&page=").append(page);
        
        // Añadimos filtros de fecha si existen
        if (before != null) {
            urlBuilder.append("&before=").append(before);
        }
        if (after != null) {
            urlBuilder.append("&after=").append(after);
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new IOException("Error getting activities. HTTP " + res.statusCode() + " - " + res.body());
        }

        // Jackson parseará el array JSON en una Lista de Actividades
        Activity[] arr = objectMapper.readValue(res.body(), Activity[].class);
        return Arrays.asList(arr);
    }
    
    public List<Activity> getActivities(String accessToken, int perPage, int page) throws IOException, InterruptedException {
        return getActivities(accessToken, perPage, page, null, null);
    }
    

    /**
     * Obtiene streams (time series) de una actividad.
     * keys: por ejemplo "time,latlng,altitude,velocity_smooth,heartrate,watts,cadence"
     * keyByType: si true, la respuesta vendrá agrupada por tipo
     */
    public String getActivityStreams(String accessToken, long activityId, String keys, boolean keyByType) throws IOException, InterruptedException {
        String url = String.format("https://www.strava.com/api/v3/activities/%d/streams?keys=%s&key_by_type=%s",
                activityId, urlEncode(keys), keyByType ? "true" : "false");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new IOException("Error getting streams. HTTP " + res.statusCode() + " - " + res.body());
        }

        // devolvemos el JSON crudo; puedes mapearlo a clases más específicas si quieres
        return res.body();
    }

    // ---------------------------------------------------------------------
    // Utilitarios
    // ---------------------------------------------------------------------

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public Activity getActivity(String accessToken, long activityId) throws IOException, InterruptedException {
        String url = "https://www.strava.com/api/v3/activities/" + activityId;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new IOException("Error fetching activity");
        return objectMapper.readValue(res.body(), Activity.class);
    }

    // ---------------------------------------------------------------------
    // Clase interna para la respuesta de Token
    // ---------------------------------------------------------------------
    // NOTA: Los nombres de las variables están en camelCase
    // Jackson los mapeará automáticamente desde snake_case gracias a la
    // configuración en el constructor.
    // ---------------------------------------------------------------------
    public static class TokenResponse {
        private String tokenType;
        private String accessToken;
        private String refreshToken;
        private Integer expiresIn;
        private Long expiresAt;
        private Athletes athlete;

        // ---- Getters y Setters ----
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

        public Integer getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Integer expiresIn) { this.expiresIn = expiresIn; }

        public Long getExpiresAt() { return expiresAt; }
        public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }

        public Athletes getAthlete() { return athlete; }
        public void setAthlete(Athletes athlete) { this.athlete = athlete; }

        @Override
        public String toString() {
            return "TokenResponse{" +
                    "accessToken='" + accessToken + '\'' +
                    ", refreshToken='" + refreshToken + '\'' +
                    ", expiresIn=" + expiresIn +
                    ", athlete=" + (athlete != null ? athlete.toString() : "null") +
                    '}';
        }
    }
}
