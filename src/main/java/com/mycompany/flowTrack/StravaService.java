/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public StravaService(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
    public List<Activity> getActivities(String accessToken, int perPage, int page) throws IOException, InterruptedException {
        String url = String.format("https://www.strava.com/api/v3/athlete/activities?per_page=%d&page=%d", perPage, page);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new IOException("Error getting activities. HTTP " + res.statusCode() + " - " + res.body());
        }

        Activity[] arr = objectMapper.readValue(res.body(), Activity[].class);
        return Arrays.asList(arr);
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

    public class TokenResponse {
        private String token_type;
        private String access_token;
        private String refresh_token;
        private Integer expires_in;
        private Long expires_at;
        private Athletes athlete;

        // ---- Getters y Setters ----
        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getRefresh_token() {
            return refresh_token;
        }

        public void setRefresh_token(String refresh_token) {
            this.refresh_token = refresh_token;
        }

        public Integer getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Integer expires_in) {
            this.expires_in = expires_in;
        }

        public Long getExpires_at() {
            return expires_at;
        }

        public void setExpires_at(Long expires_at) {
            this.expires_at = expires_at;
        }

        public Athletes getAthlete() {
            return athlete;
        }

        public void setAthlete(Athletes athlete) {
            this.athlete = athlete;
        }

        @Override
        public String toString() {
            return "TokenResponse{" +
                    "access_token='" + access_token + '\'' +
                    ", refresh_token='" + refresh_token + '\'' +
                    ", expires_in=" + expires_in +
                    ", athlete=" + (athlete != null ? athlete.toString() : "null") +
                    '}';
        }
    }


    // ---------------------------------------------------------------------
    // Ejemplo de uso (puedes quitarlo en producción)
    // ---------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        // Ejemplo rápido: intercambiar code recibido por token (no recomendado en main en producción)
        String redirectUri = "http://localhost:8080/exchange_token";
        StravaService s = new StravaService("TU_CLIENT_ID", "TU_CLIENT_SECRET");
        TokenResponse tr = s.exchangeCodeForToken("EL_CODE", redirectUri);
        System.out.println("Access token: " + tr.access_token);
    }
}
