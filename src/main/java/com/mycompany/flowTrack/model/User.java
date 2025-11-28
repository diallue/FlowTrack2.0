package com.mycompany.flowTrack.model;

import java.time.*;
import com.google.gson.JsonObject;

/**
 * Representa un usuario autenticado en Strava.
 *
 * Contiene información personal, preferencias, tokens de autenticación y métodos
 * de utilidad para manejar el estado de expiración del token y obtener el nombre completo.
 */
public class User {

    /** ID único del usuario en Strava. */
    private Long id;

    /** Nombre de usuario público en Strava. */
    private String username;

    /** Nombre real del usuario. */
    private String firstname;

    /** Apellido del usuario. */
    private String lastname;

    /** Ciudad del usuario. */
    private String city;

    /** Estado o región del usuario. */
    private String state;

    /** País del usuario. */
    private String country;

    /** Género declarado del usuario ("M" o "F"). */
    private String sex;

    /** Indica si el usuario es Strava Premium. */
    private boolean premium;

    /** URL de perfil en resolución media. */
    private String profileMedium;

    /** URL de perfil en resolución completa. */
    private String profile;

    // Tokens de autenticación

    /** Token de acceso actual para la API de Strava. */
    private String accessToken;

    /** Token de refresco para renovar el token de acceso. */
    private String refreshToken;

    /** Fecha y hora de expiración del token de acceso. */
    private LocalDateTime tokenExpiresAt;

    // Preferencias

    /** Preferencia de formato de fecha del usuario. */
    private String datePreference;

    /** Preferencia de sistema métrico o imperial. */
    private String measurementPreference;

    // Constructores

    /** Constructor vacío por defecto. */
    public User() {}

    // --- Getters y Setters ---

    /** @return ID único del usuario */
    public Long getId() { return id; }
    /** @param id ID único del usuario */
    public void setId(Long id) { this.id = id; }

    /** @return Nombre de usuario */
    public String getUsername() { return username; }
    /** @param username Nombre de usuario */
    public void setUsername(String username) { this.username = username; }

    /** @return Nombre real */
    public String getFirstname() { return firstname; }
    /** @param firstname Nombre real */
    public void setFirstname(String firstname) { this.firstname = firstname; }

    /** @return Apellido */
    public String getLastname() { return lastname; }
    /** @param lastname Apellido */
    public void setLastname(String lastname) { this.lastname = lastname; }

    /** @return Ciudad del usuario */
    public String getCity() { return city; }
    /** @param city Ciudad del usuario */
    public void setCity(String city) { this.city = city; }

    /** @return Estado/región del usuario */
    public String getState() { return state; }
    /** @param state Estado/región del usuario */
    public void setState(String state) { this.state = state; }

    /** @return País del usuario */
    public String getCountry() { return country; }
    /** @param country País del usuario */
    public void setCountry(String country) { this.country = country; }

    /** @return Género declarado */
    public String getSex() { return sex; }
    /** @param sex Género declarado */
    public void setSex(String sex) { this.sex = sex; }

    /** @return true si el usuario es Strava Premium */
    public boolean isPremium() { return premium; }
    /** @param premium Define si el usuario es Premium */
    public void setPremium(boolean premium) { this.premium = premium; }

    /** @return URL de perfil (resolución media) */
    public String getProfileMedium() { return profileMedium; }
    /** @param profileMedium URL de perfil (resolución media) */
    public void setProfileMedium(String profileMedium) { this.profileMedium = profileMedium; }

    /** @return URL de perfil (resolución completa) */
    public String getProfile() { return profile; }
    /** @param profile URL de perfil (resolución completa) */
    public void setProfile(String profile) { this.profile = profile; }

    /** @return Token de acceso actual */
    public String getAccessToken() { return accessToken; }
    /** @param accessToken Token de acceso actual */
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    /** @return Token de refresco */
    public String getRefreshToken() { return refreshToken; }
    /** @param refreshToken Token de refresco */
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    /** @return Fecha y hora de expiración del token de acceso */
    public LocalDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    /** @param tokenExpiresAt Fecha y hora de expiración del token */
    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }

    /** @return Preferencia de formato de fecha */
    public String getDatePreference() { return datePreference; }
    /** @param datePreference Preferencia de formato de fecha */
    public void setDatePreference(String datePreference) { this.datePreference = datePreference; }

    /** @return Preferencia de sistema métrico o imperial */
    public String getMeasurementPreference() { return measurementPreference; }
    /** @param measurementPreference Preferencia de sistema métrico o imperial */
    public void setMeasurementPreference(String measurementPreference) { this.measurementPreference = measurementPreference; }

    // --- Métodos de utilidad ---

    /**
     * Indica si el token de acceso ha expirado.
     *
     * @return true si el token no está definido o ha expirado, false si aún es válido
     */
    public boolean isTokenExpired() {
        if (tokenExpiresAt == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(tokenExpiresAt);
    }

    /**
     * Devuelve el nombre completo del usuario (nombre + apellido).
     *
     * @return Nombre completo
     */
    public String getFullName() {
        return firstname + " " + lastname;
    }

    /**
     * Representación legible del usuario.
     *
     * @return Cadena con ID, username, nombre, apellido, estado Premium y expiración del token
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", premium=" + premium +
                ", tokenExpired=" + isTokenExpired() +
                '}';
    }
}
