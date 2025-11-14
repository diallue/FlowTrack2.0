/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.model;

/**
 *
 * @author diego
 */
public class Clubs {
    private Long id;
    private String name;
    private Integer resource_state;
    private String sport_type;
    private String city;
    private String state;
    private String country;
    private Boolean privateClub;
    private String member_count;
    private String profile_medium;
    private String profile;

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getResource_state() { return resource_state; }
    public void setResource_state(Integer resource_state) { this.resource_state = resource_state; }

    public String getSport_type() { return sport_type; }
    public void setSport_type(String sport_type) { this.sport_type = sport_type; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Boolean getPrivateClub() { return privateClub; }
    public void setPrivateClub(Boolean privateClub) { this.privateClub = privateClub; }

    public String getMember_count() { return member_count; }
    public void setMember_count(String member_count) { this.member_count = member_count; }

    public String getProfile_medium() { return profile_medium; }
    public void setProfile_medium(String profile_medium) { this.profile_medium = profile_medium; }

    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }

    @Override
    public String toString() {
        return name + " (" + sport_type + ") - " + country;
    }

}
