package com.mycompany.flowTrack.model;

/**
 * Representa unas zapatillas registradas en el perfil del atleta dentro de Strava.
 *
 * Este modelo se utiliza para mapear la información de cada par de zapatillas asociadas
 * a la cuenta del usuario. Contiene información básica como su identificador, nombre,
 * si son las zapatillas principales y la distancia total recorrida con ellas.
 */
public class Shoes {

    /** ID único de las zapatillas asignado por Strava. */
    private String id;

    /** Indica si este par de zapatillas es el principal del atleta. */
    private boolean primary;

    /** Nombre asignado por el usuario a las zapatillas. */
    private String name;

    /** Nivel de detalle del recurso devuelto por Strava. */
    private int resourceState;

    /** Distancia total recorrida con estas zapatillas (en metros). */
    private double distance;

    /** @return ID único de las zapatillas */
    public String getId() { return id; }

    /** @param id ID único de las zapatillas */
    public void setId(String id) { this.id = id; }

    /** @return true si las zapatillas son las principales del usuario */
    public boolean isPrimary() { return primary; }

    /** @param primary Define si son las zapatillas principales */
    public void setPrimary(boolean primary) { this.primary = primary; }

    /** @return Nombre de las zapatillas */
    public String getName() { return name; }

    /** @param name Nombre asignado a las zapatillas */
    public void setName(String name) { this.name = name; }

    /** @return Estado del recurso devuelto por Strava */
    public int getResourceState() { return resourceState; }

    /** @param resource_state Estado del recurso */
    public void setResourceState(int resource_state) { this.resourceState = resource_state; }

    /** @return Distancia total recorrida con estas zapatillas */
    public double getDistance() { return distance; }

    /** @param distance Distancia total recorrida */
    public void setDistance(double distance) { this.distance = distance; }
}
