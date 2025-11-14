/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.model;



import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @file Activity.java
 * @brief Modelo para representar una actividad deportiva de Strava
 * * Esta clase encapsula toda la información relacionada con una actividad
 * deportiva obtenida desde la API de Strava, incluyendo estadísticas,
 * ubicación, tiempos y métricas de rendimiento.
 * * @author diego
 * @author ignacio
 * @author alvaro
 * @date 2025
 * @version 1.0
 */

public class Activity {
    
    /**
     * @brief Identificador único de la actividad en Strava
     * @details Este ID es generado por Strava y es único para cada actividad
     */
    private Long id;
    
    /**
     * @brief Estado del recurso en la API
     * @details Indica el nivel de detalle de los datos (1=meta, 2=summary, 3=detail)
     */
    private Integer resourceState;
    
    /**
     * @brief Identificador externo de la actividad
     * @details ID proporcionado por el dispositivo o aplicación que creó la actividad
     */
    private String externalId;
    
    /**
     * @brief ID de la carga/upload de la actividad
     * @details Referencia a la carga original si la actividad fue importada
     */
    private Long uploadId;
    
    /**
     * @brief Nombre de la actividad
     * @details Título descriptivo asignado por el usuario o generado automáticamente
     */
    private String name;
    
    /**
     * @brief Descripción de la actividad
     * @details Notas o descripción personal de la actividad
     */
    private String description;
    
    /**
     * @brief Tipo principal de la actividad
     * @details Ej: "Run", "Ride", "Swim"
     */
    private String type; // Run, Ride, etc.
    
    /**
     * @brief Tipo de deporte específico
     * @details Ej: "MountainBikeRide", "TrailRun", "VirtualRide"
     */
    private String sportType; // MountainBikeRide, TrailRun, etc.
    
    /**
     * @brief ID del atleta propietario de la actividad
     */
    private Long athleteId;
    
    // Distancias y tiempos
    
    /**
     * @brief Distancia total de la actividad
     * @details Almacenada en metros
     */
    private Float distance; // En metros
    
    /**
     * @brief Tiempo total en movimiento
     * @details Almacenado en segundos
     */
    private Integer movingTime; // En segundos
    
    /**
     * @brief Tiempo total transcurrido (incluyendo pausas)
     * @details Almacenado en segundos
     */
    private Integer elapsedTime; // En segundos
    
    /**
     * @brief Ganancia total de elevación
     * @details Almacenada en metros
     */
    private Float totalElevationGain; // En metros
    
    // Fechas
    
    /**
     * @brief Fecha y hora de inicio de la actividad en UTC
     * @details Almacenado como ZonedDateTime para incluir la zona horaria
     */
    private ZonedDateTime startDate;
    
    /**
     * @brief Fecha y hora de inicio en la zona horaria local de la actividad
     * @details Almacenado como LocalDateTime (sin zona horaria explícita)
     */
    private LocalDateTime startDateLocal;
    
    /**
     * @brief Zona horaria de la actividad
     * @details Ej: "(GMT-08:00) America/Los_Angeles"
     */
    private String timezone;
    
    /**
     * @brief Desplazamiento horario respecto a UTC
     * @details Valor en segundos
     */
    private Float utcOffset;
    
    // Ubicación
    
    /**
     * @brief Coordenadas de inicio [latitud, longitud]
     */
    private List<Float> startLatlng;
    
    /**
     * @brief Coordenadas de fin [latitud, longitud]
     */
    private List<Float> endLatlng;
    
    // Estadísticas
    
    /**
     * @brief Velocidad media
     * @details Almacenada en metros por segundo (m/s)
     */
    private Float averageSpeed; // m/s
    
    /**
     * @brief Velocidad máxima
     * @details Almacenada en metros por segundo (m/s)
     */
    private Float maxSpeed; // m/s
    
    /**
     * @brief Cadencia media (pasos o pedaladas por minuto)
     */
    private Float averageCadence;
    
    /**
     * @brief Temperatura media durante la actividad (si está disponible)
     * @details En grados Celsius
     */
    private Float averageTemp;
    
    /**
     * @brief Potencia media (si está disponible)
     * @details En vatios (Watts)
     */
    private Float averageWatts;
    
    /**
     * @brief Potencia media ponderada (si está disponible)
     * @details En vatios (Watts)
     */
    private Integer weightedAverageWatts;
    
    /**
     * @brief Trabajo total realizado
     * @details En kilojulios (kJ)
     */
    private Float kilojoules;
    
    /**
     * @brief Potencia máxima registrada
     * @details En vatios (Watts)
     */
    private Integer maxWatts;
    
    /**
     * @brief Calorías quemadas estimadas
     */
    private Float calories;
    
    // Elevación
    
    /**
     * @brief Elevación máxima alcanzada
     * @details En metros
     */
    private Float elevHigh;
    
    /**
     * @brief Elevación mínima alcanzada
     * @details En metros
     */
    private Float elevLow;
    
    // Flags (Booleanos)
    
    /**
     * @brief Indica si la actividad se realizó en un rodillo o entrenador
     */
    private Boolean trainer;
    
    /**
     * @brief Indica si la actividad fue marcada como un desplazamiento (ej. al trabajo)
     */
    private Boolean commute;
    
    /**
     * @brief Indica si la actividad fue introducida manualmente
     */
    private Boolean manual;
    
    /**
     * @brief Indica si la actividad es privada
     * @details Se usa 'privateActivity' porque 'private' es palabra reservada en Java
     */
    private Boolean privateActivity; // "private" es palabra reservada
    
    /**
     * @brief Indica si la actividad ha sido marcada (flagged) por problemas
     */
    private Boolean flagged;
    
    /**
     * @brief Indica si la actividad tiene datos de frecuencia cardíaca
     */
    private Boolean hasHeartrate;
    
    /**
     * @brief Indica si el dispositivo registró potencia (vatios)
     */
    private Boolean deviceWatts;
    
    /**
     * @brief Indica si el atleta autenticado ha dado "Kudos" a esta actividad
     */
    private Boolean hasKudoed;
    
    // Contadores
    
    /**
     * @brief Número de logros (PRs, KOMs/QOMs) en la actividad
     */
    private Integer achievementCount;
    
    /**
     * @brief Número total de "Kudos" recibidos
     */
    private Integer kudosCount;
    
    /**
     * @brief Número total de comentarios recibidos
     */
    private Integer commentCount;
    
    /**
     * @brief Número de atletas que participaron en la actividad (grupal)
     */
    private Integer athleteCount;
    
    /**
     * @brief Número de fotos adjuntas a la actividad
     */
    private Integer photoCount;
    
    /**
     * @brief Recuento total de fotos (incluyendo fotos de Instagram)
     */
    private Integer totalPhotoCount;
    
    /**
     * @brief Número de Récords Personales (PR) logrados
     */
    private Integer prCount;
    
    // Otros
    
    /**
     * @brief ID del equipamiento (bicicleta, zapatillas) utilizado
     */
    private String gearId;
    
    /**
     * @brief Tipo de entrenamiento (si está especificado)
     * @details Ej: 0 (carrera), 1 (entrenamiento), 2 (larga distancia), 3 (series)
     */
    private Integer workoutType;
    
    /**
     * @brief Nombre del dispositivo que grabó la actividad
     * @details Ej: "Garmin Edge 530"
     */
    private String deviceName;
    
    /**
     * @brief Token para incrustar (embed) la actividad en una web
     */
    private String embedToken;
    
    // Constructores
    
    /**
     * @brief Constructor por defecto
     * @details Crea una instancia vacía de Activity.
     */
    public Activity() {}
    
    // Getters y Setters
    
    /**
     * @brief Obtiene el ID único de la actividad
     * @return El ID de la actividad
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @brief Establece el ID único de la actividad
     * @param id El nuevo ID para la actividad
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * @brief Obtiene el estado del recurso
     * @return El nivel de detalle de los datos (1, 2 o 3)
     */
    public Integer getResourceState() {
        return resourceState;
    }
    
    /**
     * @brief Establece el estado del recurso
     * @param resourceState El nuevo estado del recurso
     */
    public void setResourceState(Integer resourceState) {
        this.resourceState = resourceState;
    }
    
    /**
     * @brief Obtiene el ID externo
     * @return El ID externo de la actividad
     */
    public String getExternalId() {
        return externalId;
    }
    
    /**
     * @brief Establece el ID externo
     * @param externalId El nuevo ID externo
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    /**
     * @brief Obtiene el ID de la carga (upload)
     * @return El ID de la carga
     */
    public Long getUploadId() {
        return uploadId;
    }
    
    /**
     * @brief Establece el ID de la carga (upload)
     * @param uploadId El nuevo ID de la carga
     */
    public void setUploadId(Long uploadId) {
        this.uploadId = uploadId;
    }
    
    /**
     * @brief Obtiene el nombre de la actividad
     * @return El nombre
     */
    public String getName() {
        return name;
    }
    
    /**
     * @brief Establece el nombre de la actividad
     * @param name El nuevo nombre
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @brief Obtiene la descripción de la actividad
     * @return La descripción
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @brief Establece la descripción de la actividad
     * @param description La nueva descripción
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @brief Obtiene el tipo principal de la actividad
     * @return El tipo (Ej: "Run")
     */
    public String getType() {
        return type;
    }
    
    /**
     * @brief Establece el tipo principal de la actividad
     * @param type El nuevo tipo
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * @brief Obtiene el tipo de deporte específico
     * @return El tipo de deporte (Ej: "TrailRun")
     */
    public String getSportType() {
        return sportType;
    }
    
    /**
     * @brief Establece el tipo de deporte específico
     * @param sportType El nuevo tipo de deporte
     */
    public void setSportType(String sportType) {
        this.sportType = sportType;
    }
    
    /**
     * @brief Obtiene el ID del atleta
     * @return El ID del atleta
     */
    public Long getAthleteId() {
        return athleteId;
    }
    
    /**
     * @brief Establece el ID del atleta
     * @param athleteId El nuevo ID del atleta
     */
    public void setAthleteId(Long athleteId) {
        this.athleteId = athleteId;
    }
    
    /**
     * @brief Obtiene la distancia en metros
     * @return La distancia en metros
     */
    public Float getDistance() {
        return distance;
    }
    
    /**
     * @brief Establece la distancia en metros
     * @param distance La nueva distancia en metros
     */
    public void setDistance(Float distance) {
        this.distance = distance;
    }
    
    /**
     * @brief Obtiene el tiempo en movimiento en segundos
     * @return El tiempo en movimiento en segundos
     */
    public Integer getMovingTime() {
        return movingTime;
    }
    
    /**
     * @brief Establece el tiempo en movimiento en segundos
     * @param movingTime El nuevo tiempo en movimiento
     */
    public void setMovingTime(Integer movingTime) {
        this.movingTime = movingTime;
    }
    
    /**
     * @brief Obtiene el tiempo transcurrido total en segundos
     * @return El tiempo transcurrido en segundos
     */
    public Integer getElapsedTime() {
        return elapsedTime;
    }
    
    /**
     * @brief Establece el tiempo transcurrido total en segundos
     * @param elapsedTime El nuevo tiempo transcurrido
     */
    public void setElapsedTime(Integer elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
    
    /**
     * @brief Obtiene la ganancia total de elevación en metros
     * @return La ganancia de elevación en metros
     */
    public Float getTotalElevationGain() {
        return totalElevationGain;
    }
    
    /**
     * @brief Establece la ganancia total de elevación en metros
     * @param totalElevationGain La nueva ganancia de elevación
     */
    public void setTotalElevationGain(Float totalElevationGain) {
        this.totalElevationGain = totalElevationGain;
    }
    
    /**
     * @brief Obtiene la fecha de inicio (UTC)
     * @return La fecha de inicio como ZonedDateTime
     */
    public ZonedDateTime getStartDate() {
        return startDate;
    }
    
    /**
     * @brief Establece la fecha de inicio (UTC)
     * @param startDate La nueva fecha de inicio
     */
    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }
    
    /**
     * @brief Obtiene la fecha de inicio local
     * @return La fecha de inicio local como LocalDateTime
     */
    public LocalDateTime getStartDateLocal() {
        return startDateLocal;
    }
    
    /**
     * @brief Establece la fecha de inicio local
     * @param startDateLocal La nueva fecha de inicio local
     */
    public void setStartDateLocal(LocalDateTime startDateLocal) {
        this.startDateLocal = startDateLocal;
    }
    
    /**
     * @brief Obtiene la zona horaria
     * @return El string de la zona horaria
     */
    public String getTimezone() {
        return timezone;
    }
    
    /**
     * @brief Establece la zona horaria
     * @param timezone El nuevo string de la zona horaria
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    /**
     * @brief Obtiene el desplazamiento UTC en segundos
     * @return El desplazamiento UTC
     */
    public Float getUtcOffset() {
        return utcOffset;
    }
    
    /**
     * @brief Establece el desplazamiento UTC en segundos
     * @param utcOffset El nuevo desplazamiento UTC
     */
    public void setUtcOffset(Float utcOffset) {
        this.utcOffset = utcOffset;
    }
    
    /**
     * @brief Obtiene las coordenadas de inicio
     * @return Una lista [latitud, longitud]
     */
    public List<Float> getStartLatlng() {
        return startLatlng;
    }
    
    /**
     * @brief Establece las coordenadas de inicio
     * @param startLatlng Una lista [latitud, longitud]
     */
    public void setStartLatlng(List<Float> startLatlng) {
        this.startLatlng = startLatlng;
    }
    
    /**
     * @brief Obtiene las coordenadas de fin
     * @return Una lista [latitud, longitud]
     */
    public List<Float> getEndLatlng() {
        return endLatlng;
    }
    
    /**
     * @brief Establece las coordenadas de fin
     * @param endLatlng Una lista [latitud, longitud]
     */
    public void setEndLatlng(List<Float> endLatlng) {
        this.endLatlng = endLatlng;
    }
    
    /**
     * @brief Obtiene la velocidad media en m/s
     * @return La velocidad media
     */
    public Float getAverageSpeed() {
        return averageSpeed;
    }
    
    /**
     * @brief Establece la velocidad media en m/s
     * @param averageSpeed La nueva velocidad media
     */
    public void setAverageSpeed(Float averageSpeed) {
        this.averageSpeed = averageSpeed;
    }
    
    /**
     * @brief Obtiene la velocidad máxima en m/s
     * @return La velocidad máxima
     */
    public Float getMaxSpeed() {
        return maxSpeed;
    }
    
    /**
     * @brief Establece la velocidad máxima en m/s
     * @param maxSpeed La nueva velocidad máxima
     */
    public void setMaxSpeed(Float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
    
    /**
     * @brief Obtiene la cadencia media
     * @return La cadencia media
     */
    public Float getAverageCadence() {
        return averageCadence;
    }
    
    /**
     * @brief Establece la cadencia media
     * @param averageCadence La nueva cadencia media
     */
    public void setAverageCadence(Float averageCadence) {
        this.averageCadence = averageCadence;
    }
    
    /**
     * @brief Obtiene la temperatura media
     * @return La temperatura media en Celsius
     */
    public Float getAverageTemp() {
        return averageTemp;
    }
    
    /**
     * @brief Establece la temperatura media
     * @param averageTemp La nueva temperatura media
     */
    public void setAverageTemp(Float averageTemp) {
        this.averageTemp = averageTemp;
    }
    
    /**
     * @brief Obtiene la potencia media en vatios
     * @return La potencia media
     */
    public Float getAverageWatts() {
        return averageWatts;
    }
    
    /**
     * @brief Establece la potencia media en vatios
     * @param averageWatts La nueva potencia media
     */
    public void setAverageWatts(Float averageWatts) {
        this.averageWatts = averageWatts;
    }
    
    /**
     * @brief Obtiene la potencia media ponderada en vatios
     * @return La potencia media ponderada
     */
    public Integer getWeightedAverageWatts() {
        return weightedAverageWatts;
    }
    
    /**
     * @brief Establece la potencia media ponderada en vatios
     * @param weightedAverageWatts La nueva potencia media ponderada
     */
    public void setWeightedAverageWatts(Integer weightedAverageWatts) {
        this.weightedAverageWatts = weightedAverageWatts;
    }
    
    /**
     * @brief Obtiene el trabajo total en kilojulios
     * @return El trabajo en kJ
     */
    public Float getKilojoules() {
        return kilojoules;
    }
    
    /**
     * @brief Establece el trabajo total en kilojulios
     * @param kilojoules El nuevo trabajo en kJ
     */
    public void setKilojoules(Float kilojoules) {
        this.kilojoules = kilojoules;
    }
    
    /**
     * @brief Obtiene la potencia máxima en vatios
     * @return La potencia máxima
     */
    public Integer getMaxWatts() {
        return maxWatts;
    }
    
    /**
     * @brief Establece la potencia máxima en vatios
     * @param maxWatts La nueva potencia máxima
     */
    public void setMaxWatts(Integer maxWatts) {
        this.maxWatts = maxWatts;
    }
    
    /**
     * @brief Obtiene las calorías estimadas
     * @return Las calorías
     */
    public Float getCalories() {
        return calories;
    }
    
    /**
     * @brief Establece las calorías estimadas
     * @param calories Las nuevas calorías
     */
    public void setCalories(Float calories) {
        this.calories = calories;
    }
    
    /**
     * @brief Obtiene la elevación máxima en metros
     * @return La elevación máxima
     */
    public Float getElevHigh() {
        return elevHigh;
    }
    
    /**
     * @brief Establece la elevación máxima en metros
     * @param elevHigh La nueva elevación máxima
     */
    public void setElevHigh(Float elevHigh) {
        this.elevHigh = elevHigh;
    }
    
    /**
     * @brief Obtiene la elevación mínima en metros
     * @return La elevación mínima
     */
    public Float getElevLow() {
        return elevLow;
    }
    
    /**
     * @brief Establece la elevación mínima en metros
     * @param elevLow La nueva elevación mínima
     */
    public void setElevLow(Float elevLow) {
        this.elevLow = elevLow;
    }
    
    /**
     * @brief Comprueba si la actividad fue en rodillo/entrenador
     * @return true si fue en rodillo, false en caso contrario
     */
    public Boolean getTrainer() {
        return trainer;
    }
    
    /**
     * @brief Establece si la actividad fue en rodillo/entrenador
     * @param trainer El nuevo valor
     */
    public void setTrainer(Boolean trainer) {
        this.trainer = trainer;
    }
    
    /**
     * @brief Comprueba si la actividad fue un desplazamiento
     * @return true si fue un desplazamiento, false en caso contrario
     */
    public Boolean getCommute() {
        return commute;
    }
    
    /**
     * @brief Establece si la actividad fue un desplazamiento
     * @param commute El nuevo valor
     */
    public void setCommute(Boolean commute) {
        this.commute = commute;
    }
    
    /**
     * @brief Comprueba si la actividad fue introducida manualmente
     * @return true si fue manual, false en caso contrario
     */
    public Boolean getManual() {
        return manual;
    }
    
    /**
     * @brief Establece si la actividad fue introducida manualmente
     * @param manual El nuevo valor
     */
    public void setManual(Boolean manual) {
        this.manual = manual;
    }
    
    /**
     * @brief Comprueba si la actividad es privada
     * @return true si es privada, false en caso contrario
     */
    public Boolean getPrivateActivity() {
        return privateActivity;
    }
    
    /**
     * @brief Establece si la actividad es privada
     * @param privateActivity El nuevo valor
     */
    public void setPrivateActivity(Boolean privateActivity) {
        this.privateActivity = privateActivity;
    }
    
    /**
     * @brief Comprueba si la actividad está marcada (flagged)
     * @return true si está marcada, false en caso contrario
     */
    public Boolean getFlagged() {
        return flagged;
    }
    
    /**
     * @brief Establece si la actividad está marcada (flagged)
     * @param flagged El nuevo valor
     */
    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
    }
    
    /**
     * @brief Comprueba si la actividad tiene datos de frecuencia cardíaca
     * @return true si tiene datos de FC, false en caso contrario
     */
    public Boolean getHasHeartrate() {
        return hasHeartrate;
    }
    
    /**
     * @brief Establece si la actividad tiene datos de frecuencia cardíaca
     * @param hasHeartrate El nuevo valor
     */
    public void setHasHeartrate(Boolean hasHeartrate) {
        this.hasHeartrate = hasHeartrate;
    }
    
    /**
     * @brief Comprueba si el dispositivo registró potencia (vatios)
     * @return true si registró vatios, false en caso contrario
     */
    public Boolean getDeviceWatts() {
        return deviceWatts;
    }
    
    /**
     * @brief Establece si el dispositivo registró potencia (vatios)
     * @param deviceWatts El nuevo valor
     */
    public void setDeviceWatts(Boolean deviceWatts) {
        this.deviceWatts = deviceWatts;
    }
    
    /**
     * @brief Comprueba si el usuario autenticado ha dado "Kudos"
     * @return true si ha dado Kudos, false en caso contrario
     */
    public Boolean getHasKudoed() {
        return hasKudoed;
    }
    
    /**
     * @brief Establece si el usuario autenticado ha dado "Kudos"
     * @param hasKudoed El nuevo valor
     */
    public void setHasKudoed(Boolean hasKudoed) {
        this.hasKudoed = hasKudoed;
    }
    
    /**
     * @brief Obtiene el número de logros
     * @return El número de logros
     */
    public Integer getAchievementCount() {
        return achievementCount;
    }
    
    /**
     * @brief Establece el número de logros
     * @param achievementCount El nuevo número de logros
     */
    public void setAchievementCount(Integer achievementCount) {
        this.achievementCount = achievementCount;
    }
    
    /**
     * @brief Obtiene el número de Kudos
     * @return El número de Kudos
     */
    public Integer getKudosCount() {
        return kudosCount;
    }
    
    /**
     * @brief Establece el número de Kudos
     * @param kudosCount El nuevo número de Kudos
     */
    public void setKudosCount(Integer kudosCount) {
        this.kudosCount = kudosCount;
    }
    
    /**
     * @brief Obtiene el número de comentarios
     * @return El número de comentarios
     */
    public Integer getCommentCount() {
        return commentCount;
    }
    
    /**
     * @brief Establece el número de comentarios
     * @param commentCount El nuevo número de comentarios
     */
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }
    
    /**
     * @brief Obtiene el número de atletas en la actividad
     * @return El número de atletas
     */
    public Integer getAthleteCount() {
        return athleteCount;
    }
    
    /**
     * @brief Establece el número de atletas en la actividad
     * @param athleteCount El nuevo número de atletas
     */
    public void setAthleteCount(Integer athleteCount) {
        this.athleteCount = athleteCount;
    }
    
    /**
     * @brief Obtiene el número de fotos
     * @return El número de fotos
     */
    public Integer getPhotoCount() {
        return photoCount;
    }
    
    /**
     * @brief Establece el número de fotos
     * @param photoCount El nuevo número de fotos
     */
    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }
    
    /**
     * @brief Obtiene el número total de fotos (incluyendo externas)
     * @return El número total de fotos
     */
    public Integer getTotalPhotoCount() {
        return totalPhotoCount;
    }
    
    /**
     * @brief Establece el número total de fotos
     * @param totalPhotoCount El nuevo número total de fotos
     */
    public void setTotalPhotoCount(Integer totalPhotoCount) {
        this.totalPhotoCount = totalPhotoCount;
    }
    
    /**
     * @brief Obtiene el número de Récords Personales (PR)
     * @return El número de PRs
     */
    public Integer getPrCount() {
        return prCount;
    }
    
    /**
     * @brief Establece el número de Récords Personales (PR)
     * @param prCount El nuevo número de PRs
     */
    public void setPrCount(Integer prCount) {
        this.prCount = prCount;
    }
    
    /**
     * @brief Obtiene el ID del equipamiento
     * @return El ID del equipamiento
     */
    public String getGearId() {
        return gearId;
    }
    
    /**
     * @brief Establece el ID del equipamiento
     * @param gearId El nuevo ID del equipamiento
     */
    public void setGearId(String gearId) {
        this.gearId = gearId;
    }
    
    /**
     * @brief Obtiene el tipo de entrenamiento
     * @return El código del tipo de entrenamiento
     */
    public Integer getWorkoutType() {
        return workoutType;
    }
    
    /**
     * @brief Establece el tipo de entrenamiento
     * @param workoutType El nuevo código del tipo de entrenamiento
     */
    public void setWorkoutType(Integer workoutType) {
        this.workoutType = workoutType;
    }
    
    /**
     * @brief Obtiene el nombre del dispositivo
     * @return El nombre del dispositivo
     */
    public String getDeviceName() {
        return deviceName;
    }
    
    /**
     * @brief Establece el nombre del dispositivo
     * @param deviceName El nuevo nombre del dispositivo
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    /**
     * @brief Obtiene el token de incrustación (embed)
     * @return El token
     */
    public String getEmbedToken() {
        return embedToken;
    }
    
    /**
     * @brief Establece el token de incrustación (embed)
     * @param embedToken El nuevo token
     */
    public void setEmbedToken(String embedToken) {
        this.embedToken = embedToken;
    }
    
    // Métodos de utilidad
    
    /**
     * @brief Calcula la distancia en kilómetros
     * @details Convierte la distancia (almacenada en metros) a kilómetros.
     * @return La distancia en km, o null si la distancia no está definida.
     */
    public Float getDistanceInKm() {
        return distance != null ? distance / 1000 : null;
    }
    
    /**
     * @brief Calcula el tiempo transcurrido en minutos
     * @details Convierte el tiempo transcurrido (almacenado en segundos) a minutos.
     * @return El tiempo transcurrido en minutos, o null si no está definido.
     */
    public Integer getElapsedTimeInMinutes() {
        return elapsedTime != null ? elapsedTime / 60 : null;
    }
    
    /**
     * @brief Calcula el tiempo en movimiento en minutos
     * @details Convierte el tiempo en movimiento (almacenado en segundos) a minutos.
     * @return El tiempo en movimiento en minutos, o null si no está definido.
     */
    public Integer getMovingTimeInMinutes() {
        return movingTime != null ? movingTime / 60 : null;
    }
    
    /**
     * @brief Calcula la velocidad media en km/h
     * @details Convierte la velocidad media (almacenada en m/s) a km/h.
     * @return La velocidad media en km/h, o null si no está definida.
     */
    public Float getAverageSpeedKmh() {
        return averageSpeed != null ? averageSpeed * 3.6f : null;
    }
    
    /**
     * @brief Calcula la velocidad máxima en km/h
     * @details Convierte la velocidad máxima (almacenada en m/s) a km/h.
     * @return La velocidad máxima en km/h, o null si no está definida.
     */
    public Float getMaxSpeedKmh() {
        return maxSpeed != null ? maxSpeed * 3.6f : null;
    }
    
    /**
     * @brief Crea una instancia de Activity a partir de un JsonObject de la API de Strava.
     * @details Este método actúa como un "factory" que parsea la respuesta JSON
     * de Strava y mapea los campos a los atributos de esta clase.
     * Maneja la verificación de campos nulos o inexistentes.
     * @param activityData El objeto JSON que representa una actividad de Strava.
     * @return Una nueva instancia de Activity poblada con los datos del JSON.
     */
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
    
    /**
     * @brief Genera una representación en String de la actividad.
     * @details Muestra un resumen de los campos más importantes de la actividad
     * (ID, nombre, tipo, distancia, tiempo, fecha).
     * @return Un String formateado con información de la actividad.
     */
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