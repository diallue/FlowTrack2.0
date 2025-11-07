/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.model;

import java.util.List;

/**
 *
 * @author diego
 */
public class Athletes {
    private long id;
    private String username;
    private int resource_state;
    private String firstname;
    private String lastname;
    private String city;
    private String state;
    private String country;
    private String sex;
    private boolean premium;
    private String created_at;
    private String updated_at;
    private int badge_type_id;
    private String profile_medium;
    private String profile;
    private Integer follower_count;
    private Integer friend_count;
    private Integer mutual_friend_count;
    private int athlete_type;
    private String date_preference;
    private String measurement_preference;
    private List<Clubs> clubs;
    private Integer ftp;
    private Double weight;
    private List<Bikes> bikes;
    private List<Shoes> shoes;
    
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getResource_state() { return resource_state; }
    public void setResource_state(int resource_state) { this.resource_state = resource_state; }
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
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
    public int getBadge_type_id() { return badge_type_id; }
    public void setBadge_type_id(int badge_type_id) { this.badge_type_id = badge_type_id; }
    public String getProfile_medium() { return profile_medium; }
    public void setProfile_medium(String profile_medium) { this.profile_medium = profile_medium; }
    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }
    public Integer getFollower_count() { return follower_count; }
    public void setFollower_count(Integer follower_count) { this.follower_count = follower_count; }
    public Integer getFriend_count() { return friend_count; }
    public void setFriend_count(Integer friend_count) { this.friend_count = friend_count; }
    public Integer getMutual_friend_count() { return mutual_friend_count; }
    public void setMutual_friend_count(Integer mutual_friend_count) { this.mutual_friend_count = mutual_friend_count; }
    public int getAthlete_type() { return athlete_type; }
    public void setAthlete_type(int athlete_type) { this.athlete_type = athlete_type; }
    public String getDate_preference() { return date_preference; }
    public void setDate_preference(String date_preference) { this.date_preference = date_preference; }
    public String getMeasurement_preference() { return measurement_preference; }
    public void setMeasurement_preference(String measurement_preference) { this.measurement_preference = measurement_preference; }
    public List<Clubs> getClubs() { return clubs; }
    public void setClubs(List<Clubs> clubs) { this.clubs = clubs; }
    public Integer getFtp() { return ftp; }
    public void setFtp(Integer ftp) { this.ftp = ftp; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public List<Bikes> getBikes() { return bikes; }
    public void setBikes(List<Bikes> bikes) { this.bikes = bikes; }
    public List<Shoes> getShoes() { return shoes; }
    public void setShoes(List<Shoes> shoes) { this.shoes = shoes; }

    @Override
    public String toString() {
        return firstname + " " + lastname + " (" + username + "), " + country;
    }
}
