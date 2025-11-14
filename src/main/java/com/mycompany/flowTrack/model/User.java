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
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }
    public String getProfileMedium() { return profileMedium; }
    public void setProfileMedium(String profileMedium) { this.profileMedium = profileMedium; }
    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public LocalDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }
    public String getDatePreference() { return datePreference; }
    public void setDatePreference(String datePreference) { this.datePreference = datePreference; }
    public String getMeasurementPreference() { return measurementPreference; }
    public void setMeasurementPreference(String measurementPreference) { this.measurementPreference = measurementPreference; }

    
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