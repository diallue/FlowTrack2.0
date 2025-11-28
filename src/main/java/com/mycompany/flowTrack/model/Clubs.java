package com.mycompany.flowTrack.model;

/**
 * Representa un club asociado a un atleta en Strava.
 *
 * Este modelo mapea la información recibida desde la API de Strava respecto
 * a los clubes en los que participa el usuario. Contiene datos básicos como
 * identificador, nombre, tipo de deporte del club, ubicación, privacidad y
 * elementos visuales como las imágenes de perfil.
 */
public class Clubs {

    /** Identificador único del club en Strava. */
    private Long id;

    /** Nombre oficial del club. */
    private String name;

    /** Nivel de detalle del recurso devuelto por Strava. */
    private Integer resourceState;

    /** Tipo de deporte principal del club (por ejemplo: cycling, running). */
    private String sportType;

    /** Ciudad donde se ubica el club. */
    private String city;

    /** Estado o región donde se ubica el club. */
    private String state;

    /** País del club. */
    private String country;

    /** Indica si el club es privado (solo por invitación). */
    private Boolean privateClub;

    /** Número total de miembros del club (devuelto como String por Strava). */
    private String memberCount;

    /** URL del icono o imagen de perfil en tamaño medio. */
    private String profileMedium;

    /** URL de la imagen de perfil en tamaño completo. */
    private String profile;

    // --- Getters y Setters ---

    /** @return ID del club */
    public Long getId() { return id; }

    /** @param id ID del club */
    public void setId(Long id) { this.id = id; }

    /** @return Nombre del club */
    public String getName() { return name; }

    /** @param name Nombre del club */
    public void setName(String name) { this.name = name; }

    /** @return Nivel de detalle del recurso */
    public Integer getResourceState() { return resourceState; }

    /** @param resource_state Nivel de detalle del recurso */
    public void setResourceState(Integer resource_state) { this.resourceState = resource_state; }

    /** @return Tipo de deporte del club */
    public String getSportType() { return sportType; }

    /** @param sport_type Tipo de deporte del club */
    public void setSportType(String sport_type) { this.sportType = sport_type; }

    /** @return Ciudad del club */
    public String getCity() { return city; }

    /** @param city Ciudad del club */
    public void setCity(String city) { this.city = city; }

    /** @return Estado/región del club */
    public String getState() { return state; }

    /** @param state Estado/región del club */
    public void setState(String state) { this.state = state; }

    /** @return País del club */
    public String getCountry() { return country; }

    /** @param country País del club */
    public void setCountry(String country) { this.country = country; }

    /** @return true si el club es privado */
    public Boolean getPrivateClub() { return privateClub; }

    /** @param privateClub Define si el club es privado */
    public void setPrivateClub(Boolean privateClub) { this.privateClub = privateClub; }

    /** @return Número total de miembros */
    public String getMemberCount() { return memberCount; }

    /** @param member_count Número total de miembros */
    public void setMemberCount(String member_count) { this.memberCount = member_count; }

    /** @return Enlace a la imagen de perfil (tamaño medio) */
    public String getProfileMedium() { return profileMedium; }

    /** @param profile_medium Enlace a la imagen de perfil (tamaño medio) */
    public void setProfileMedium(String profile_medium) { this.profileMedium = profile_medium; }

    /** @return Enlace a la imagen de perfil (tamaño completo) */
    public String getProfile() { return profile; }

    /** @param profile Enlace a la imagen de perfil (tamaño completo) */
    public void setProfile(String profile) { this.profile = profile; }

    /**
     * Devuelve una representación legible del club.
     *
     * @return cadena con el formato: "Nombre (Deporte) - País"
     */
    @Override
    public String toString() {
        return name + " (" + sportType + ") - " + country;
    }

}
