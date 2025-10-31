/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.logicaNegocio;

/**
 *
 * @author diego
 */
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpServer;

public class ConexionApiStrava {

    private static final String USER_ID = "177549"; 
    private static final String USER_TOKEN = "17af0ae01a69783ef0981bcea389625c3300803e";
    private static final String REDIRECT_URI = "http://localhost:8080/exchange_token";

    /**
     * Método que realiza el login con Strava y devuelve el access_token.
     * Puede ser llamado directamente desde tu clase Login.
     */
    public static String autenticarConStrava() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        final StringBuilder authCode = new StringBuilder();

        server.createContext("/exchange_token", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("code=")) {
                String code = query.split("code=")[1].split("&")[0];
                authCode.append(code);

                String response = "<html><body><h2>Autorización completada</h2>" + "<p>Ya puedes cerrar esta pestaña.</p></body></html>";
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.close();

                new Thread(() -> {
                    try { server.stop(0); } catch (Exception ignored) {}
                }).start();
            }
        });
        server.start();

        String authUrl = "https://www.strava.com/oauth/authorize?client_id=" + USER_ID
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&scope=activity:read_all";

        System.out.println("Abriendo navegador para autorización...");
        java.awt.Desktop.getDesktop().browse(new URI(authUrl));

        System.out.println("Esperando autorización del usuario...");
        while (authCode.length() == 0) {
            Thread.sleep(500);
        }

        System.out.println("Código recibido: " + authCode);
        String accessToken = getAccessToken(authCode.toString());
        System.out.println("Access Token obtenido: " + accessToken);

        return accessToken;
    }

    /**
     * Intercambia el código recibido por un token de acceso.
     */
    private static String getAccessToken(String code) throws IOException {
        String url = "https://www.strava.com/oauth/token";
        String params = "client_id=" + USER_ID
                + "&client_secret=" + USER_TOKEN
                + "&code=" + code
                + "&grant_type=authorization_code";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.getOutputStream().write(params.getBytes(StandardCharsets.UTF_8));

        BufferedReader reader;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            response.append(line);
        reader.close();

        String json = response.toString();
        if (!json.contains("\"access_token\"")) {
            throw new IOException("Error al obtener access_token: " + json);
        }

        int start = json.indexOf("\"access_token\":\"") + 16;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    /**
     * Obtiene las actividades del usuario autenticado.
     */
    public static String getActivities(String accessToken) throws IOException {
        String url = "https://www.strava.com/api/v3/athlete/activities";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            response.append(line);
        reader.close();

        return response.toString();
    }
}
