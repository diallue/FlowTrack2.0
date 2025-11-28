/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.model;

import java.util.List;

/**
 * Representa a un atleta obtenido desde la API de Strava.
 * 
 * Contiene información personal, preferencias del atleta, estado,
 * equipamiento asignado (bicicletas y zapatillas) y clubs asociados.
 * Este modelo se usa para mapear directamente la respuesta JSON completa
 * que Strava devuelve al consultar el perfil del usuario autenticado.
 */
public class Athletes {
    private long id;                     // ID único del atleta en Strava
    private String username;             // Nombre de usuario público
    private int resourceState;           // Nivel de detalle del recurso devuelto
    private String firstname;            // Nombre real del atleta
    private String lastname;             // Apellido del atleta
    private String city;                 // Ciudad del atleta
    private String state;                // Estado/Provincia del atleta
    private String country;              // País del atleta
    private String sex;                  // Género declarado ("M", "F")
    private boolean premium;             // Indica si el usuario es Strava Premium
    private String createdAt;            // Fecha de creación del perfil
    private String updatedAt;            // Fecha de última actualización del perfil
    private int badgeTypeId;             // Tipo de insignia asignada por Strava
    private String profileMedium;        // URL de foto del atleta en resolución media
    private String profile;              // URL de foto del atleta en resolución completa
    private Integer followerCount;       // Número de seguidores
    private Integer friendCount;         // Número de amigos
    private Integer mutualFriendCount;   // Amigos en común
    private int athleteType;             // Tipo de atleta (ej. 1=corredor, 2=ciclista)
    private String datePreference;       // Preferencia de formato de fecha
    private String measurementPreference;// Sistema métrico o imperial
    private List<Clubs> clubs;           // Lista de clubs a los que pertenece
    private Integer ftp;                 // Umbral de potencia funcional (solo ciclismo)
    private Double weight;               // Peso del atleta según Strava
    private List<Bikes> bikes;           // Bicicletas registradas por el usuario
    private List<Shoes> shoes;           // Zapatillas registradas por el usuario

    /** @return ID único del atleta */
    public long getId() { return id; }
    /** @param id ID único del atleta */
    public void setId(long id) { this.id = id; }

    /** @return Nombre de usuario de Strava */
    public String getUsername() { return username; }
    /** @param username Nombre de usuario de Strava */
    public void setUsername(String username) { this.username = username; }

    /** @return Estado del recurso devuelto por Strava */
    public int getResourceState() { return resourceState; }
    /** @param resourceState Estado del recurso */
    public void setResourceState(int resourceState) { this.resourceState = resourceState; }

    /** @return Nombre real */
    public String getFirstname() { return firstname; }
    /** @param firstname Nombre real */
    public void setFirstname(String firstname) { this.firstname = firstname; }

    /** @return Apellido real */
    public String getLastname() { return lastname; }
    /** @param lastname Apellido real */
    public void setLastname(String lastname) { this.lastname = lastname; }

    /** @return Ciudad del atleta */
    public String getCity() { return city; }
    /** @param city Ciudad del atleta */
    public void setCity(String city) { this.city = city; }

    /** @return Estado/Provincia */
    public String getState() { return state; }
    /** @param state Estado/Provincia */
    public void setState(String state) { this.state = state; }

    /** @return País del atleta */
    public String getCountry() { return country; }
    /** @param country País del atleta */
    public void setCountry(String country) { this.country = country; }

    /** @return Género del atleta */
    public String getSex() { return sex; }
    /** @param sex Género del atleta */
    public void setSex(String sex) { this.sex = sex; }

    /** @return true si es usuario Premium */
    public boolean isPremium() { return premium; }
    /** @param premium Define si es usuario Premium */
    public void setPremium(boolean premium) { this.premium = premium; }

    /** @return Fecha de creación del perfil */
    public String getCreatedAt() { return createdAt; }
    /** @param createdAt Fecha de creación del perfil */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /** @return Última fecha de actualización */
    public String getUpdatedAt() { return updatedAt; }
    /** @param updatedAt Última fecha de actualización */
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    /** @return Tipo de insignia asignada */
    public int getBadgeTypeId() { return badgeTypeId; }
    /** @param badgeTypeId Tipo de insignia asignada */
    public void setBadgeTypeId(int badgeTypeId) { this.badgeTypeId = badgeTypeId; }

    /** @return URL de foto de perfil en media resolución */
    public String getProfileMedium() { return profileMedium; }
    /** @param profileMedium URL de foto media */
    public void setProfileMedium(String profileMedium) { this.profileMedium = profileMedium; }

    /** @return URL de foto de perfil en alta resolución */
    public String getProfile() { return profile; }
    /** @param profile URL de foto en alta resolución */
    public void setProfile(String profile) { this.profile = profile; }

    /** @return Número de seguidores */
    public Integer getFollowerCount() { return followerCount; }
    /** @param followerCount Número de seguidores */
    public void setFollowerCount(Integer followerCount) { this.followerCount = followerCount; }

    /** @return Número de amigos */
    public Integer getFriendCount() { return friendCount; }
    /** @param friendCount Número de amigos */
    public void setFriendCount(Integer friendCount) { this.friendCount = friendCount; }

    /** @return Número de amigos en común */
    public Integer getMutualFriendCount() { return mutualFriendCount; }
    /** @param mutualFriendCount Número de amigos en común */
    public void setMutualFriendCount(Integer mutualFriendCount) { this.mutualFriendCount = mutualFriendCount; }

    /** @return Tipo de atleta según Strava */
    public int getAthleteType() { return athleteType; }
    /** @param athleteType Tipo de atleta */
    public void setAthleteType(int athleteType) { this.athleteType = athleteType; }

    /** @return Preferencia de formato de fecha */
    public String getDatePreference() { return datePreference; }
    /** @param datePreference Preferencia de formato de fecha */
    public void setDatePreference(String datePreference) { this.datePreference = datePreference; }

    /** @return Sistema métrico elegido */
    public String getMeasurementPreference() { return measurementPreference; }
    /** @param measurementPreference Sistema métrico o imperial */
    public void setMeasurementPreference(String measurementPreference) { this.measurementPreference = measurementPreference; }

    /** @return Lista de clubs del atleta */
    public List<Clubs> getClubs() { return clubs; }
    /** @param clubs Lista de clubs */
    public void setClubs(List<Clubs> clubs) { this.clubs = clubs; }

    /** @return FTP (Functional Threshold Power) */
    public Integer getFtp() { return ftp; }
    /** @param ftp FTP del atleta */
    public void setFtp(Integer ftp) { this.ftp = ftp; }

    /** @return Peso declarado por el atleta */
    public Double getWeight() { return weight; }
    /** @param weight Peso */
    public void setWeight(Double weight) { this.weight = weight; }

    /** @return Bicicletas registradas del atleta */
    public List<Bikes> getBikes() { return bikes; }
    /** @param bikes Lista de bicicletas */
    public void setBikes(List<Bikes> bikes) { this.bikes = bikes; }

    /** @return Zapatillas registradas del atleta */
    public List<Shoes> getShoes() { return shoes; }
    /** @param shoes Lista de zapatillas */
    public void setShoes(List<Shoes> shoes) { this.shoes = shoes; }

    /**
     * @return Representación legible del atleta: "Nombre Apellido (username), País"
     */
    @Override
    public String toString() {
        return firstname + " " + lastname + " (" + username + "), " + country;
    }
}
