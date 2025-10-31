/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.model;

/**
 *
 * @author diego
 */

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo para representar una actividad de Strava
 * @author diego
 */
public class Activity {
    
    // Identificadores
    private Long id;
    private Integer resourceState;
    private String externalId;
    private Long uploadId;
    
    // Información básica
    private String name;
    private String description;
    private String type;              // Run, Ride, etc.
    private String sportType;         // MountainBikeRide, TrailRun, etc.
    
    // Atleta propietario
    private Long athleteId;
    
    // Distancias y tiempos
    private Float distance;           // En metros
    private Integer movingTime;       // En segundos
    private Integer elapsedTime;      // En segundos
    private Float totalElevationGain; // En metros
    
    // Fechas
    private ZonedDateTime startDate;
    private LocalDateTime startDateLocal;
    private String timezone;
    private Float utcOffset;
    
    // Ubicación
    private List<Float> startLatlng;
    private List<Float> endLatlng;
    
    // Estadísticas
    private Float averageSpeed;       // m/s
    private Float maxSpeed;           // m/s
    private Float averageCadence;
    private Float averageTemp;
    private Float averageWatts;
    private Integer weightedAverageWatts;
    private Float kilojoules;
    private Integer maxWatts;
    private Float calories;
    
    // Elevación
    private Float elevHigh;
    private Float elevLow;
    
    // Flags
    private Boolean trainer;
    private Boolean commute;
    private Boolean manual;
    private Boolean privateActivity;  // "private" es palabra reservada
    private Boolean flagged;
    private Boolean hasHeartrate;
    private Boolean deviceWatts;
    private Boolean hasKudoed;
    
    // Contadores
    private Integer achievementCount;
    private Integer kudosCount;
    private Integer commentCount;
    private Integer athleteCount;
    private Integer photoCount;
    private Integer totalPhotoCount;
    private Integer prCount;
    
    // Otros
    private String gearId;
    private Integer workoutType;
    private String deviceName;
    private String embedToken;
    
    // Constructores
    public Activity() {}
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getResourceState() {
        return resourceState;
    }
    
    public void setResourceState(Integer resourceState) {
        this.resourceState = resourceState;
    }
    
    public String getExternalId() {
        return externalId;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    public Long getUploadId() {
        return uploadId;
    }
    
    public void setUploadId(Long uploadId) {
        this.uploadId = uploadId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSportType() {
        return sportType;
    }
    
    public void setSportType(String sportType) {
        this.sportType = sportType;
    }
    
    public Long getAthleteId() {
        return athleteId;
    }
    
    public void setAthleteId(Long athleteId) {
        this.athleteId = athleteId;
    }
    
    public Float getDistance() {
        return distance;
    }
    
    public void setDistance(Float distance) {
        this.distance = distance;
    }
    
    public Integer getMovingTime() {
        return movingTime;
    }
    
    public void setMovingTime(Integer movingTime) {
        this.movingTime = movingTime;
    }
    
    public Integer getElapsedTime() {
        return elapsedTime;
    }
    
    public void setElapsedTime(Integer elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
    
    public Float getTotalElevationGain() {
        return totalElevationGain;
    }
    
    public void setTotalElevationGain(Float totalElevationGain) {
        this.totalElevationGain = totalElevationGain;
    }
    
    public ZonedDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getStartDateLocal() {
        return startDateLocal;
    }
    
    public void setStartDateLocal(LocalDateTime startDateLocal) {
        this.startDateLocal = startDateLocal;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public Float getUtcOffset() {
        return utcOffset;
    }
    
    public void setUtcOffset(Float utcOffset) {
        this.utcOffset = utcOffset;
    }
    
    public List<Float> getStartLatlng() {
        return startLatlng;
    }
    
    public void setStartLatlng(List<Float> startLatlng) {
        this.startLatlng = startLatlng;
    }
    
    public List<Float> getEndLatlng() {
        return endLatlng;
    }
    
    public void setEndLatlng(List<Float> endLatlng) {
        this.endLatlng = endLatlng;
    }
    
    public Float getAverageSpeed() {
        return averageSpeed;
    }
    
    public void setAverageSpeed(Float averageSpeed) {
        this.averageSpeed = averageSpeed;
    }
    
    public Float getMaxSpeed() {
        return maxSpeed;
    }
    
    public void setMaxSpeed(Float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
    
    public Float getAverageCadence() {
        return averageCadence;
    }
    
    public void setAverageCadence(Float averageCadence) {
        this.averageCadence = averageCadence;
    }
    
    public Float getAverageTemp() {
        return averageTemp;
    }
    
    public void setAverageTemp(Float averageTemp) {
        this.averageTemp = averageTemp;
    }
    
    public Float getAverageWatts() {
        return averageWatts;
    }
    
    public void setAverageWatts(Float averageWatts) {
        this.averageWatts = averageWatts;
    }
    
    public Integer getWeightedAverageWatts() {
        return weightedAverageWatts;
    }
    
    public void setWeightedAverageWatts(Integer weightedAverageWatts) {
        this.weightedAverageWatts = weightedAverageWatts;
    }
    
    public Float getKilojoules() {
        return kilojoules;
    }
    
    public void setKilojoules(Float kilojoules) {
        this.kilojoules = kilojoules;
    }
    
    public Integer getMaxWatts() {
        return maxWatts;
    }
    
    public void setMaxWatts(Integer maxWatts) {
        this.maxWatts = maxWatts;
    }
    
    public Float getCalories() {
        return calories;
    }
    
    public void setCalories(Float calories) {
        this.calories = calories;
    }
    
    public Float getElevHigh() {
        return elevHigh;
    }
    
    public void setElevHigh(Float elevHigh) {
        this.elevHigh = elevHigh;
    }
    
    public Float getElevLow() {
        return elevLow;
    }
    
    public void setElevLow(Float elevLow) {
        this.elevLow = elevLow;
    }
    
    public Boolean getTrainer() {
        return trainer;
    }
    
    public void setTrainer(Boolean trainer) {
        this.trainer = trainer;
    }
    
    public Boolean getCommute() {
        return commute;
    }
    
    public void setCommute(Boolean commute) {
        this.commute = commute;
    }
    
    public Boolean getManual() {
        return manual;
    }
    
    public void setManual(Boolean manual) {
        this.manual = manual;
    }
    
    public Boolean getPrivateActivity() {
        return privateActivity;
    }
    
    public void setPrivateActivity(Boolean privateActivity) {
        this.privateActivity = privateActivity;
    }
    
    public Boolean getFlagged() {
        return flagged;
    }
    
    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
    }
    
    public Boolean getHasHeartrate() {
        return hasHeartrate;
    }
    
    public void setHasHeartrate(Boolean hasHeartrate) {
        this.hasHeartrate = hasHeartrate;
    }
    
    public Boolean getDeviceWatts() {
        return deviceWatts;
    }
    
    public void setDeviceWatts(Boolean deviceWatts) {
        this.deviceWatts = deviceWatts;
    }
    
    public Boolean getHasKudoed() {
        return hasKudoed;
    }
    
    public void setHasKudoed(Boolean hasKudoed) {
        this.hasKudoed = hasKudoed;
    }
    
    public Integer getAchievementCount() {
        return achievementCount;
    }
    
    public void setAchievementCount(Integer achievementCount) {
        this.achievementCount = achievementCount;
    }
    
    public Integer getKudosCount() {
        return kudosCount;
    }
    
    public void setKudosCount(Integer kudosCount) {
        this.kudosCount = kudosCount;
    }
    
    public Integer getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }
    
    public Integer getAthleteCount() {
        return athleteCount;
    }
    
    public void setAthleteCount(Integer athleteCount) {
        this.athleteCount = athleteCount;
    }
    
    public Integer getPhotoCount() {
        return photoCount;
    }
    
    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }
    
    public Integer getTotalPhotoCount() {
        return totalPhotoCount;
    }
    
    public void setTotalPhotoCount(Integer totalPhotoCount) {
        this.totalPhotoCount = totalPhotoCount;
    }
    
    public Integer getPrCount() {
        return prCount;
    }
    
    public void setPrCount(Integer prCount) {
        this.prCount = prCount;
    }
    
    public String getGearId() {
        return gearId;
    }
    
    public void setGearId(String gearId) {
        this.gearId = gearId;
    }
    
    public Integer getWorkoutType() {
        return workoutType;
    }
    
    public void setWorkoutType(Integer workoutType) {
        this.workoutType = workoutType;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public String getEmbedToken() {
        return embedToken;
    }
    
    public void setEmbedToken(String embedToken) {
        this.embedToken = embedToken;
    }
    
    // Métodos de utilidad
    public Float getDistanceInKm() {
        return distance != null ? distance / 1000 : null;
    }
    
    public Integer getElapsedTimeInMinutes() {
        return elapsedTime != null ? elapsedTime / 60 : null;
    }
    
    public Integer getMovingTimeInMinutes() {
        return movingTime != null ? movingTime / 60 : null;
    }
    
    public Float getAverageSpeedKmh() {
        return averageSpeed != null ? averageSpeed * 3.6f : null;
    }
    
    public Float getMaxSpeedKmh() {
        return maxSpeed != null ? maxSpeed * 3.6f : null;
    }
    
    // Método estático para crear desde la respuesta de la API
    public static Activity fromStravaAPI(JsonObject activityData) {
        Activity activity = new Activity();
        
        // IDs
        activity.setId(activityData.get("id").getAsLong());
        activity.setResourceState(activityData.get("resource_state").getAsInt());
        
        if (activityData.has("external_id") && !activityData.get("external_id").isJsonNull()) {
            activity.setExternalId(activityData.get("external_id").getAsString());
        }
        
        if (activityData.has("upload_id") && !activityData.get("upload_id").isJsonNull()) {
            activity.setUploadId(activityData.get("upload_id").getAsLong());
        }
        
        // Atleta
        if (activityData.has("athlete")) {
            JsonObject athlete = activityData.getAsJsonObject("athlete");
            activity.setAthleteId(athlete.get("id").getAsLong());
        }
        
        // Información básica
        activity.setName(activityData.get("name").getAsString());
        
        if (activityData.has("description") && !activityData.get("description").isJsonNull()) {
            activity.setDescription(activityData.get("description").getAsString());
        }
        
        activity.setType(activityData.get("type").getAsString());
        activity.setSportType(activityData.get("sport_type").getAsString());
        
        // Distancias y tiempos
        activity.setDistance(activityData.get("distance").getAsFloat());
        activity.setMovingTime(activityData.get("moving_time").getAsInt());
        activity.setElapsedTime(activityData.get("elapsed_time").getAsInt());
        activity.setTotalElevationGain(activityData.get("total_elevation_gain").getAsFloat());
        
        // Fechas
        if (activityData.has("start_date")) {
            activity.setStartDate(ZonedDateTime.parse(activityData.get("start_date").getAsString()));
        }
        
        if (activityData.has("start_date_local")) {
            activity.setStartDateLocal(LocalDateTime.parse(
                activityData.get("start_date_local").getAsString(),
                DateTimeFormatter.ISO_DATE_TIME
            ));
        }
        
        if (activityData.has("timezone") && !activityData.get("timezone").isJsonNull()) {
            activity.setTimezone(activityData.get("timezone").getAsString());
        }
        
        if (activityData.has("utc_offset")) {
            activity.setUtcOffset(activityData.get("utc_offset").getAsFloat());
        }
        
        // Ubicación
        if (activityData.has("start_latlng") && !activityData.get("start_latlng").isJsonNull()) {
            JsonArray latlng = activityData.getAsJsonArray("start_latlng");
            List<Float> coords = new ArrayList<>();
            coords.add(latlng.get(0).getAsFloat());
            coords.add(latlng.get(1).getAsFloat());
            activity.setStartLatlng(coords);
        }
        
        if (activityData.has("end_latlng") && !activityData.get("end_latlng").isJsonNull()) {
            JsonArray latlng = activityData.getAsJsonArray("end_latlng");
            List<Float> coords = new ArrayList<>();
            coords.add(latlng.get(0).getAsFloat());
            coords.add(latlng.get(1).getAsFloat());
            activity.setEndLatlng(coords);
        }
        
        // Estadísticas
        if (activityData.has("average_speed")) {
            activity.setAverageSpeed(activityData.get("average_speed").getAsFloat());
        }
        
        if (activityData.has("max_speed")) {
            activity.setMaxSpeed(activityData.get("max_speed").getAsFloat());
        }
        
        if (activityData.has("average_cadence") && !activityData.get("average_cadence").isJsonNull()) {
            activity.setAverageCadence(activityData.get("average_cadence").getAsFloat());
        }
        
        if (activityData.has("average_temp") && !activityData.get("average_temp").isJsonNull()) {
            activity.setAverageTemp(activityData.get("average_temp").getAsFloat());
        }
        
        if (activityData.has("average_watts") && !activityData.get("average_watts").isJsonNull()) {
            activity.setAverageWatts(activityData.get("average_watts").getAsFloat());
        }
        
        if (activityData.has("weighted_average_watts") && !activityData.get("weighted_average_watts").isJsonNull()) {
            activity.setWeightedAverageWatts(activityData.get("weighted_average_watts").getAsInt());
        }
        
        if (activityData.has("kilojoules") && !activityData.get("kilojoules").isJsonNull()) {
            activity.setKilojoules(activityData.get("kilojoules").getAsFloat());
        }
        
        if (activityData.has("max_watts") && !activityData.get("max_watts").isJsonNull()) {
            activity.setMaxWatts(activityData.get("max_watts").getAsInt());
        }
        
        if (activityData.has("calories") && !activityData.get("calories").isJsonNull()) {
            activity.setCalories(activityData.get("calories").getAsFloat());
        }
        
        // Elevación
        if (activityData.has("elev_high") && !activityData.get("elev_high").isJsonNull()) {
            activity.setElevHigh(activityData.get("elev_high").getAsFloat());
        }
        
        if (activityData.has("elev_low") && !activityData.get("elev_low").isJsonNull()) {
            activity.setElevLow(activityData.get("elev_low").getAsFloat());
        }
        
        // Flags
        activity.setTrainer(activityData.get("trainer").getAsBoolean());
        activity.setCommute(activityData.get("commute").getAsBoolean());
        activity.setManual(activityData.get("manual").getAsBoolean());
        activity.setPrivateActivity(activityData.get("private").getAsBoolean());
        activity.setFlagged(activityData.get("flagged").getAsBoolean());
        activity.setHasHeartrate(activityData.get("has_heartrate").getAsBoolean());
        
        if (activityData.has("device_watts")) {
            activity.setDeviceWatts(activityData.get("device_watts").getAsBoolean());
        }
        
        activity.setHasKudoed(activityData.get("has_kudoed").getAsBoolean());
        
        // Contadores
        activity.setAchievementCount(activityData.get("achievement_count").getAsInt());
        activity.setKudosCount(activityData.get("kudos_count").getAsInt());
        activity.setCommentCount(activityData.get("comment_count").getAsInt());
        activity.setAthleteCount(activityData.get("athlete_count").getAsInt());
        activity.setPhotoCount(activityData.get("photo_count").getAsInt());
        activity.setTotalPhotoCount(activityData.get("total_photo_count").getAsInt());
        activity.setPrCount(activityData.get("pr_count").getAsInt());
        
        // Otros
        if (activityData.has("gear_id") && !activityData.get("gear_id").isJsonNull()) {
            activity.setGearId(activityData.get("gear_id").getAsString());
        }
        
        if (activityData.has("workout_type") && !activityData.get("workout_type").isJsonNull()) {
            activity.setWorkoutType(activityData.get("workout_type").getAsInt());
        }
        
        if (activityData.has("device_name") && !activityData.get("device_name").isJsonNull()) {
            activity.setDeviceName(activityData.get("device_name").getAsString());
        }
        
        if (activityData.has("embed_token") && !activityData.get("embed_token").isJsonNull()) {
            activity.setEmbedToken(activityData.get("embed_token").getAsString());
        }
        
        return activity;
    }
    
    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", sportType='" + sportType + '\'' +
                ", distance=" + getDistanceInKm() + "km" +
                ", elapsedTime=" + getElapsedTimeInMinutes() + "min" +
                ", startDateLocal=" + startDateLocal +
                '}';
    }
}
