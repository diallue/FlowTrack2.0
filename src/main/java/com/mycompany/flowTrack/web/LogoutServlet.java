package com.mycompany.flowTrack.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet encargado de manejar el cierre de sesión del usuario.
 * Mapeado a la URL "/logout".
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    
    /**
     * Maneja las peticiones HTTP POST para cerrar la sesión.
     * Normalmente, un logout se maneja con POST por seguridad (para prevenir ataques CSRF si se incluyeran en GETs accidentales).
     * 
     * @param req El objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param resp El objeto HttpServletResponse que contendrá la respuesta del servlet.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Invalida la sesión actual del usuario, eliminando todos los atributos almacenados en ella,
        // incluyendo los datos del usuario logeado y su token de acceso.
        req.getSession().invalidate(); 
        
        // Establece el código de estado HTTP 200 (OK) en la respuesta, indicando que la operación fue exitosa.
        // El cliente (frontend) es responsable de redirigir al usuario a la página de inicio de sesión o a la página principal.
        resp.setStatus(200);
    }
}
