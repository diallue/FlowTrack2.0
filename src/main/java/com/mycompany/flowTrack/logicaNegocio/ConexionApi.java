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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ConexionApi {
    private static final String CLIENT_ID = "177549";
    private static final String CLIENT_SECRET = "17af0ae01a69783ef0981bcea389625c3300803e";
    private static final String REDIRECT_URI = "http://localhost/exchange_token";

    public static void main(String[] args) throws IOException {
        System.out.println("Abre este enlace para autorizar tu cuenta de Strava:");
        System.out.println("https://www.strava.com/oauth/authorize?client_id=" + CLIENT_ID
                + "&response_type=code&redirect_uri=" + REDIRECT_URI
                + "&scope=activity:read_all");

        System.out.print("\nIntroduce el 'code' que aparece en la URL tras autorizar: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String code = br.readLine();

        String accessToken = getAccessToken(code);
        System.out.println("\nAccess Token obtenido: " + accessToken);

        getActivities(accessToken);
    }

    // Intercambiar el code por el access_token
    private static String getAccessToken(String code) throws IOException {
        String url = "https://www.strava.com/oauth/token";
        String params = "client_id=" + CLIENT_ID
                + "&client_secret=" + CLIENT_SECRET
                + "&code=" + code
                + "&grant_type=authorization_code";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.getOutputStream().write(params.getBytes(StandardCharsets.UTF_8));

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            response.append(line);
        reader.close();

        // Extraer el access_token del JSON (búsqueda simple)
        String json = response.toString();
        int start = json.indexOf("\"access_token\":\"") + 16;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    // Obtener lista de actividades
    private static void getActivities(String accessToken) throws IOException {
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

        System.out.println("\nActividades obtenidas:\n" + response);
    }

}
