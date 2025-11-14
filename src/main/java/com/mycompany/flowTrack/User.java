package com.mycompany.flowTrack.model;
import java.time.*;
import com.google.gson.JsonObject;

/**
 *
 * @author diego
 */
public class User {
    
    private Long id;  // ID único de Strava
    private String username;
    private String firstname;
    private String lastname;
    private String city;
    private String state;
    private String country;
    private String sex;
    private boolean premium;
    private String profileMedium;
    private String profile;
    
    // Tokens de autenticación
    private String accessToken;
    private String refreshToken;
    private LocalDateTime tokenExpiresAt;
    
    // Preferencias
    private String datePreference;
    private String measurementPreference;
    
    // Constructores
    public User() {}
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFirstname() {
        return firstname;
    }
    
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    
    public String getLastname() {
        return lastname;
    }
    
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getSex() {
        return sex;
    }
    
    public void setSex(String sex) {
        this.sex = sex;
    }
    
    public boolean isPremium() {
        return premium;
    }
    
    public void setPremium(boolean premium) {
        this.premium = premium;
    }
    
    public String getProfileMedium() {
        return profileMedium;
    }
    
    public void setProfileMedium(String profileMedium) {
        this.profileMedium = profileMedium;
    }
    
    public String getProfile() {
        return profile;
    }
    
    public void setProfile(String profile) {
        this.profile = profile;
    }
    
    // Getters y Setters para tokens
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }
    
    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }
    
    public String getDatePreference() {
        return datePreference;
    }
    
    public void setDatePreference(String datePreference) {
        this.datePreference = datePreference;
    }
    
    public String getMeasurementPreference() {
        return measurementPreference;
    }
    
    public void setMeasurementPreference(String measurementPreference) {
        this.measurementPreference = measurementPreference;
    }
    
    // Métodos de utilidad
    public boolean isTokenExpired() {
        if (tokenExpiresAt == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(tokenExpiresAt);
    }
    
    public String getFullName() {
        return firstname + " " + lastname;
    }
    
    // Método estático para crear desde la respuesta de la API
    public static User fromStravaAPI(JsonObject athleteData, String accessToken, String refreshToken, long expiresIn) {
        User user = new User();
        
        // Datos del atleta
        user.setId(athleteData.get("id").getAsLong());
        
        if (athleteData.has("username") && !athleteData.get("username").isJsonNull()) {
            user.setUsername(athleteData.get("username").getAsString());
        }
        
        user.setFirstname(athleteData.get("firstname").getAsString());
        user.setLastname(athleteData.get("lastname").getAsString());
        
        if (athleteData.has("city") && !athleteData.get("city").isJsonNull()) {
            user.setCity(athleteData.get("city").getAsString());
        }
        
        if (athleteData.has("state") && !athleteData.get("state").isJsonNull()) {
            user.setState(athleteData.get("state").getAsString());
        }
        
        if (athleteData.has("country") && !athleteData.get("country").isJsonNull()) {
            user.setCountry(athleteData.get("country").getAsString());
        }
        
        if (athleteData.has("sex") && !athleteData.get("sex").isJsonNull()) {
            user.setSex(athleteData.get("sex").getAsString());
        }
        
        user.setPremium(athleteData.get("premium").getAsBoolean());
        user.setProfileMedium(athleteData.get("profile_medium").getAsString());
        user.setProfile(athleteData.get("profile").getAsString());
        
        if (athleteData.has("date_preference")) {
            user.setDatePreference(athleteData.get("date_preference").getAsString());
        }
        
        if (athleteData.has("measurement_preference")) {
            user.setMeasurementPreference(athleteData.get("measurement_preference").getAsString());
        }
        
        // Tokens
        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        
        return user;
    }
    
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