
package com.mycompany.flowTrack.model;

/**
 * Representa una bicicleta registrada en el perfil del atleta dentro de Strava.
 * 
 * Este modelo se utiliza para mapear la información de cada bicicleta asociada
 * a la cuenta del usuario que se ha autenticado. Incluye información básica
 * como su identificador, nombre, si es la bicicleta principal y la distancia
 * total recorrida.
 */
public class Bikes {

    /** ID único de la bicicleta asignado por Strava. */
    private String id;

    /** Indica si esta bicicleta es la bicicleta principal del atleta. */
    private boolean primary;

    /** Nombre asignado por el usuario a la bicicleta. */
    private String name;

    /** Nivel de detalle del recurso devuelto por Strava. */
    private int resourceState;

    /** Distancia total recorrida con esta bicicleta (en metros). */
    private double distance;

    /** @return ID único de la bicicleta */
    public String getId() { return id; }

    /** @param id ID único de la bicicleta */
    public void setId(String id) { this.id = id; }

    /** @return true si la bicicleta es la principal del usuario */
    public boolean isPrimary() { return primary; }

    /** @param primary Define si la bicicleta es la principal */
    public void setPrimary(boolean primary) { this.primary = primary; }

    /** @return Nombre de la bicicleta */
    public String getName() { return name; }

    /** @param name Nombre asignado a la bicicleta */
    public void setName(String name) { this.name = name; }

    /** @return Estado del recurso devuelto por Strava */
    public int getResource_state() { return resourceState; }

    /** @param resource_state Estado del recurso */
    public void setResource_state(int resource_state) { this.resourceState = resource_state; }

    /** @return Distancia total recorrida con esta bicicleta */
    public double getDistance() { return distance; }

    /** @param distance Distancia total recorrida */
    public void setDistance(double distance) { this.distance = distance; }
}
