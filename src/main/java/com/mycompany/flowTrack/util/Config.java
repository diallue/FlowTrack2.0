package com.mycompany.flowTrack.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Config {
    private static final Properties properties = new Properties();

    // Bloque estático: Se ejecuta una sola vez cuando la aplicación arranca
    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Lo siento, no se pudo encontrar el archivo config.properties");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Obtiene el valor de una clave del archivo de configuración.
     * @param key La clave (ej: "strava.client.id")
     * @return El valor o null si no existe
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }
}
