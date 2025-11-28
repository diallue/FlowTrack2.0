package com.mycompany.flowTrack.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * Recurso REST de ejemplo para probar Jakarta EE 10.
 *
 * Proporciona un endpoint simple para verificar que el servicio está activo.
 */
@Path("jakartaee10")
public class JakartaEE10Resource {
    
    /**
     * Endpoint GET para verificar conectividad.
     *
     * @return Respuesta HTTP 200 con el mensaje "ping Jakarta EE"
     */
    @GET
    public Response ping(){
        return Response
                .ok("ping Jakarta EE")
                .build();
    }
}
