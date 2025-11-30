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
 * Modelo para representar una actividad deportiva de Strava.
 * 
 * Esta clase encapsula toda la información relacionada con una actividad
 * deportiva obtenida desde la API de Strava, incluyendo estadísticas,
 * ubicación, tiempos y métricas de rendimiento.
 *
 * @author diego
 * @author ignacio
 * @author alvaro
 * @version 1.0
 */


public class Activity {
    
    /**
     *   Identificador único de la actividad en Strava
     *   Este ID es generado por Strava y es único para cada actividad
     */
    private Long id;
    
    /**
     *   Estado del recurso en la API
     *   Indica el nivel de detalle de los datos (1=meta, 2=summary, 3=detail)
     */
    private Integer resourceState;
    
    /**
     *   Identificador externo de la actividad
     *   ID proporcionado por el dispositivo o aplicación que creó la actividad
     */
    private String externalId;
    
    /**
     *   ID de la carga/upload de la actividad
     *   Referencia a la carga original si la actividad fue importada
     */
    private Long uploadId;
    
    /**
     *   Nombre de la actividad
     *   Título descriptivo asignado por el usuario o generado automáticamente
     */
    private String name;
    
    /**
     *   Descripción de la actividad
     *   Notas o descripción personal de la actividad
     */
    private String description;
    
    /**
     *   Tipo principal de la actividad
     *   Ej: "Run", "Ride", "Swim"
     */
    private String type; // Run, Ride, etc.
    
    /**
     *   Tipo de deporte específico
     *   Ej: "MountainBikeRide", "TrailRun", "VirtualRide"
     */
    private String sportType; // MountainBikeRide, TrailRun, etc.
    
    /**
     *   ID del atleta propietario de la actividad
     */
    private Long athleteId;
    
    // Distancias y tiempos
    
    /**
     *   Distancia total de la actividad
     *   Almacenada en metros
     */
    private Float distance; // En metros
    
    /**
     *   Tiempo total en movimiento
     *   Almacenado en segundos
     */
    private Integer movingTime; // En segundos
    
    /**
     *   Tiempo total transcurrido (incluyendo pausas)
     *   Almacenado en segundos
     */
    private Integer elapsedTime; // En segundos
    
    /**
     *   Ganancia total de elevación
     *   Almacenada en metros
     */
    private Float totalElevationGain; // En metros
    
    // Fechas
    
    /**
     *   Fecha y hora de inicio de la actividad en UTC
     *   Almacenado como ZonedDateTime para incluir la zona horaria
     */
    private ZonedDateTime startDate;
    
    /**
     *   Fecha y hora de inicio en la zona horaria local de la actividad
     *   Almacenado como LocalDateTime (sin zona horaria explícita)
     */
    private LocalDateTime startDateLocal;
    
    /**
     *   Zona horaria de la actividad
     *   Ej: "(GMT-08:00) America/Los_Angeles"
     */
    private String timezone;
    
    /**
     *   Desplazamiento horario respecto a UTC
     *   Valor en segundos
     */
    private Float utcOffset;
    
    // Ubicación
    
    /**
     *   Coordenadas de inicio [latitud, longitud]
     */
    private List<Float> startLatlng;
    
    /**
     *   Coordenadas de fin [latitud, longitud]
     */
    private List<Float> endLatlng;
    
    // Estadísticas
    
    /**
     *   Velocidad media
     *   Almacenada en metros por segundo (m/s)
     */
    private Float averageSpeed; // m/s
    
    /**
     *   Velocidad máxima
     *   Almacenada en metros por segundo (m/s)
     */
    private Float maxSpeed; // m/s
    
    /**
     *   Cadencia media (pasos o pedaladas por minuto)
     */
    private Float averageCadence;
    
    /**
     *   Temperatura media durante la actividad (si está disponible)
     *   En grados Celsius
     */
    private Float averageTemp;
    
    /**
     *   Potencia media (si está disponible)
     *   En vatios (Watts)
     */
    private Float averageWatts;
    
    /**
     *   Potencia media ponderada (si está disponible)
     *   En vatios (Watts)
     */
    private Integer weightedAverageWatts;
    
    /**
     *   Trabajo total realizado
     *   En kilojulios (kJ)
     */
    private Float kilojoules;
    
    /**
     *   Potencia máxima registrada
     *   En vatios (Watts)
     */
    private Integer maxWatts;
    
    /**
     *   Calorías quemadas estimadas
     */
    private Float calories;
    
    // Elevación
    
    /**
     *   Elevación máxima alcanzada
     *   En metros
     */
    private Float elevHigh;
    
    /**
     *   Elevación mínima alcanzada
     *   En metros
     */
    private Float elevLow;
    
    // Flags (Booleanos)
    
    /**
     *   Indica si la actividad se realizó en un rodillo o entrenador
     */
    private Boolean trainer;
    
    /**
     *   Indica si la actividad fue marcada como un desplazamiento (ej. al trabajo)
     */
    private Boolean commute;
    
    /**
     *   Indica si la actividad fue introducida manualmente
     */
    private Boolean manual;
    
    /**
     *   Indica si la actividad es privada
     *   Se usa 'privateActivity' porque 'private' es palabra reservada en Java
     */
    private Boolean privateActivity; // "private" es palabra reservada
    
    /**
     *   Indica si la actividad ha sido marcada (flagged) por problemas
     */
    private Boolean flagged;
    
    /**
     *   Indica si la actividad tiene datos de frecuencia cardíaca
     */
    private Boolean hasHeartrate;
    
    /**
     *   Indica si el dispositivo registró potencia (vatios)
     */
    private Boolean deviceWatts;
    
    /**
     *   Indica si el atleta autenticado ha dado "Kudos" a esta actividad
     */
    private Boolean hasKudoed;
    
    // Contadores
    
    /**
     *   Número de logros (PRs, KOMs/QOMs) en la actividad
     */
    private Integer achievementCount;
    
    /**
     *   Número total de "Kudos" recibidos
     */
    private Integer kudosCount;
    
    /**
     *   Número total de comentarios recibidos
     */
    private Integer commentCount;
    
    /**
     *   Número de atletas que participaron en la actividad (grupal)
     */
    private Integer athleteCount;
    
    /**
     *   Número de fotos adjuntas a la actividad
     */
    private Integer photoCount;
    
    /**
     *   Recuento total de fotos (incluyendo fotos de Instagram)
     */
    private Integer totalPhotoCount;
    
    /**
     *   Número de Récords Personales (PR) logrados
     */
    private Integer prCount;
    
    // Otros
    
    /**
     *   ID del equipamiento (bicicleta, zapatillas) utilizado
     */
    private String gearId;
    
    /**
     *   Tipo de entrenamiento (si está especificado)
     *   Ej: 0 (carrera), 1 (entrenamiento), 2 (larga distancia), 3 (series)
     */
    private Integer workoutType;
    
    /**
     *   Nombre del dispositivo que grabó la actividad
     *   Ej: "Garmin Edge 530"
     */
    private String deviceName;
    
    /**
     *   Token para incrustar (embed) la actividad en una web
     */
    private String embedToken;
    
    // Constructores
    
    /**
     *   Constructor por defecto
     *   Crea una instancia vacía de Activity.
     */
    public Activity() {}
    
    // Getters y Setters
    
    /**
     *   Obtiene el ID único de la actividad
     * @return El ID de la actividad
     */
    public Long getId() {
        return id;
    }
    
    /**
     *   Establece el ID único de la actividad
     * @param id El nuevo ID para la actividad
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     *   Obtiene el estado del recurso
     * @return El nivel de detalle de los datos (1, 2 o 3)
     */
    public Integer getResourceState() {
        return resourceState;
    }
    
    /**
     *   Establece el estado del recurso
     * @param resourceState El nuevo estado del recurso
     */
    public void setResourceState(Integer resourceState) {
        this.resourceState = resourceState;
    }
    
    /**
     *   Obtiene el ID externo
     * @return El ID externo de la actividad
     */
    public String getExternalId() {
        return externalId;
    }
    
    /**
     *   Establece el ID externo
     * @param externalId El nuevo ID externo
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    /**
     *   Obtiene el ID de la carga (upload)
     * @return El ID de la carga
     */
    public Long getUploadId() {
        return uploadId;
    }
    
    /**
     *   Establece el ID de la carga (upload)
     * @param uploadId El nuevo ID de la carga
     */
    public void setUploadId(Long uploadId) {
        this.uploadId = uploadId;
    }
    
    /**
     *   Obtiene el nombre de la actividad
     * @return El nombre
     */
    public String getName() {
        return name;
    }
    
    /**
     *   Establece el nombre de la actividad
     * @param name El nuevo nombre
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     *   Obtiene la descripción de la actividad
     * @return La descripción
     */
    public String getDescription() {
        return description;
    }
    
    /**
     *   Establece la descripción de la actividad
     * @param description La nueva descripción
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     *   Obtiene el tipo principal de la actividad
     * @return El tipo (Ej: "Run")
     */
    public String getType() {
        return type;
    }
    
    /**
     *   Establece el tipo principal de la actividad
     * @param type El nuevo tipo
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     *   Obtiene el tipo de deporte específico
     * @return El tipo de deporte (Ej: "TrailRun")
     */
    public String getSportType() {
        return sportType;
    }
    
    /**
     *   Establece el tipo de deporte específico
     * @param sportType El nuevo tipo de deporte
     */
    public void setSportType(String sportType) {
        this.sportType = sportType;
    }
    
    /**
     *   Obtiene el ID del atleta
     * @return El ID del atleta
     */
    public Long getAthleteId() {
        return athleteId;
    }
    
    /**
     *   Establece el ID del atleta
     * @param athleteId El nuevo ID del atleta
     */
    public void setAthleteId(Long athleteId) {
        this.athleteId = athleteId;
    }
    
    /**
     *   Obtiene la distancia en metros
     * @return La distancia en metros
     */
    public Float getDistance() {
        return distance;
    }
    
    /**
     *   Establece la distancia en metros
     * @param distance La nueva distancia en metros
     */
    public void setDistance(Float distance) {
        this.distance = distance;
    }
    
    /**
     *   Obtiene el tiempo en movimiento en segundos
     * @return El tiempo en movimiento en segundos
     */
    public Integer getMovingTime() {
        return movingTime;
    }
    
    /**
     *   Establece el tiempo en movimiento en segundos
     * @param movingTime El nuevo tiempo en movimiento
     */
    public void setMovingTime(Integer movingTime) {
        this.movingTime = movingTime;
    }
    
    /**
     *   Obtiene el tiempo transcurrido total en segundos
     * @return El tiempo transcurrido en segundos
     */
    public Integer getElapsedTime() {
        return elapsedTime;
    }
    
    /**
     *   Establece el tiempo transcurrido total en segundos
     * @param elapsedTime El nuevo tiempo transcurrido
     */
    public void setElapsedTime(Integer elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
    
    /**
     *   Obtiene la ganancia total de elevación en metros
     * @return La ganancia de elevación en metros
     */
    public Float getTotalElevationGain() {
        return totalElevationGain;
    }
    
    /**
     *   Establece la ganancia total de elevación en metros
     * @param totalElevationGain La nueva ganancia de elevación
     */
    public void setTotalElevationGain(Float totalElevationGain) {
        this.totalElevationGain = totalElevationGain;
    }
    
    /**
     *   Obtiene la fecha de inicio (UTC)
     * @return La fecha de inicio como ZonedDateTime
     */
    public ZonedDateTime getStartDate() {
        return startDate;
    }
    
    /**
     *   Establece la fecha de inicio (UTC)
     * @param startDate La nueva fecha de inicio
     */
    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }
    
    /**
     *   Obtiene la fecha de inicio local
     * @return La fecha de inicio local como LocalDateTime
     */
    public LocalDateTime getStartDateLocal() {
        return startDateLocal;
    }
    
    /**
     *   Establece la fecha de inicio local
     * @param startDateLocal La nueva fecha de inicio local
     */
    public void setStartDateLocal(LocalDateTime startDateLocal) {
        this.startDateLocal = startDateLocal;
    }
    
    /**
     *   Obtiene la zona horaria
     * @return El string de la zona horaria
     */
    public String getTimezone() {
        return timezone;
    }
    
    /**
     *   Establece la zona horaria
     * @param timezone El nuevo string de la zona horaria
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    /**
     *   Obtiene el desplazamiento UTC en segundos
     * @return El desplazamiento UTC
     */
    public Float getUtcOffset() {
        return utcOffset;
    }
    
    /**
     *   Establece el desplazamiento UTC en segundos
     * @param utcOffset El nuevo desplazamiento UTC
     */
    public void setUtcOffset(Float utcOffset) {
        this.utcOffset = utcOffset;
    }
    
    /**
     *   Obtiene las coordenadas de inicio
     * @return Una lista [latitud, longitud]
     */
    public List<Float> getStartLatlng() {
        return startLatlng;
    }
    
    /**
     *   Establece las coordenadas de inicio
     * @param startLatlng Una lista [latitud, longitud]
     */
    public void setStartLatlng(List<Float> startLatlng) {
        this.startLatlng = startLatlng;
    }
    
    /**
     *   Obtiene las coordenadas de fin
     * @return Una lista [latitud, longitud]
     */
    public List<Float> getEndLatlng() {
        return endLatlng;
    }
    
    /**
     *   Establece las coordenadas de fin
     * @param endLatlng Una lista [latitud, longitud]
     */
    public void setEndLatlng(List<Float> endLatlng) {
        this.endLatlng = endLatlng;
    }
    
    /**
     *   Obtiene la velocidad media en m/s
     * @return La velocidad media
     */
    public Float getAverageSpeed() {
        return averageSpeed;
    }
    
    /**
     *   Establece la velocidad media en m/s
     * @param averageSpeed La nueva velocidad media
     */
    public void setAverageSpeed(Float averageSpeed) {
        this.averageSpeed = averageSpeed;
    }
    
    /**
     *   Obtiene la velocidad máxima en m/s
     * @return La velocidad máxima
     */
    public Float getMaxSpeed() {
        return maxSpeed;
    }
    
    /**
     *   Establece la velocidad máxima en m/s
     * @param maxSpeed La nueva velocidad máxima
     */
    public void setMaxSpeed(Float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
    
    /**
     *   Obtiene la cadencia media
     * @return La cadencia media
     */
    public Float getAverageCadence() {
        return averageCadence;
    }
    
    /**
     *   Establece la cadencia media
     * @param averageCadence La nueva cadencia media
     */
    public void setAverageCadence(Float averageCadence) {
        this.averageCadence = averageCadence;
    }
    
    /**
     *   Obtiene la temperatura media
     * @return La temperatura media en Celsius
     */
    public Float getAverageTemp() {
        return averageTemp;
    }
    
    /**
     *   Establece la temperatura media
     * @param averageTemp La nueva temperatura media
     */
    public void setAverageTemp(Float averageTemp) {
        this.averageTemp = averageTemp;
    }
    
    /**
     *   Obtiene la potencia media en vatios
     * @return La potencia media
     */
    public Float getAverageWatts() {
        return averageWatts;
    }
    
    /**
     *   Establece la potencia media en vatios
     * @param averageWatts La nueva potencia media
     */
    public void setAverageWatts(Float averageWatts) {
        this.averageWatts = averageWatts;
    }
    
    /**
     *   Obtiene la potencia media ponderada en vatios
     * @return La potencia media ponderada
     */
    public Integer getWeightedAverageWatts() {
        return weightedAverageWatts;
    }
    
    /**
     *   Establece la potencia media ponderada en vatios
     * @param weightedAverageWatts La nueva potencia media ponderada
     */
    public void setWeightedAverageWatts(Integer weightedAverageWatts) {
        this.weightedAverageWatts = weightedAverageWatts;
    }
    
    /**
     *   Obtiene el trabajo total en kilojulios
     * @return El trabajo en kJ
     */
    public Float getKilojoules() {
        return kilojoules;
    }
    
    /**
     *   Establece el trabajo total en kilojulios
     * @param kilojoules El nuevo trabajo en kJ
     */
    public void setKilojoules(Float kilojoules) {
        this.kilojoules = kilojoules;
    }
    
    /**
     *   Obtiene la potencia máxima en vatios
     * @return La potencia máxima
     */
    public Integer getMaxWatts() {
        return maxWatts;
    }
    
    /**
     *   Establece la potencia máxima en vatios
     * @param maxWatts La nueva potencia máxima
     */
    public void setMaxWatts(Integer maxWatts) {
        this.maxWatts = maxWatts;
    }
    
    /**
     *   Obtiene las calorías estimadas
     * @return Las calorías
     */
    public Float getCalories() {
        return calories;
    }
    
    /**
     *   Establece las calorías estimadas
     * @param calories Las nuevas calorías
     */
    public void setCalories(Float calories) {
        this.calories = calories;
    }
    
    /**
     *   Obtiene la elevación máxima en metros
     * @return La elevación máxima
     */
    public Float getElevHigh() {
        return elevHigh;
    }
    
    /**
     *   Establece la elevación máxima en metros
     * @param elevHigh La nueva elevación máxima
     */
    public void setElevHigh(Float elevHigh) {
        this.elevHigh = elevHigh;
    }
    
    /**
     *   Obtiene la elevación mínima en metros
     * @return La elevación mínima
     */
    public Float getElevLow() {
        return elevLow;
    }
    
    /**
     *   Establece la elevación mínima en metros
     * @param elevLow La nueva elevación mínima
     */
    public void setElevLow(Float elevLow) {
        this.elevLow = elevLow;
    }
    
    /**
     *   Comprueba si la actividad fue en rodillo/entrenador
     * @return true si fue en rodillo, false en caso contrario
     */
    public Boolean getTrainer() {
        return trainer;
    }
    
    /**
     *   Establece si la actividad fue en rodillo/entrenador
     * @param trainer El nuevo valor
     */
    public void setTrainer(Boolean trainer) {
        this.trainer = trainer;
    }
    
    /**
     *   Comprueba si la actividad fue un desplazamiento
     * @return true si fue un desplazamiento, false en caso contrario
     */
    public Boolean getCommute() {
        return commute;
    }
    
    /**
     *   Establece si la actividad fue un desplazamiento
     * @param commute El nuevo valor
     */
    public void setCommute(Boolean commute) {
        this.commute = commute;
    }
    
    /**
     *   Comprueba si la actividad fue introducida manualmente
     * @return true si fue manual, false en caso contrario
     */
    public Boolean getManual() {
        return manual;
    }
    
    /**
     *   Establece si la actividad fue introducida manualmente
     * @param manual El nuevo valor
     */
    public void setManual(Boolean manual) {
        this.manual = manual;
    }
    
    /**
     *   Comprueba si la actividad es privada
     * @return true si es privada, false en caso contrario
     */
    public Boolean getPrivateActivity() {
        return privateActivity;
    }
    
    /**
     *   Establece si la actividad es privada
     * @param privateActivity El nuevo valor
     */
    public void setPrivateActivity(Boolean privateActivity) {
        this.privateActivity = privateActivity;
    }
    
    /**
     *   Comprueba si la actividad está marcada (flagged)
     * @return true si está marcada, false en caso contrario
     */
    public Boolean getFlagged() {
        return flagged;
    }
    
    /**
     *   Establece si la actividad está marcada (flagged)
     * @param flagged El nuevo valor
     */
    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
    }
    
    /**
     *   Comprueba si la actividad tiene datos de frecuencia cardíaca
     * @return true si tiene datos de FC, false en caso contrario
     */
    public Boolean getHasHeartrate() {
        return hasHeartrate;
    }
    
    /**
     *   Establece si la actividad tiene datos de frecuencia cardíaca
     * @param hasHeartrate El nuevo valor
     */
    public void setHasHeartrate(Boolean hasHeartrate) {
        this.hasHeartrate = hasHeartrate;
    }
    
    /**
     *   Comprueba si el dispositivo registró potencia (vatios)
     * @return true si registró vatios, false en caso contrario
     */
    public Boolean getDeviceWatts() {
        return deviceWatts;
    }
    
    /**
     *   Establece si el dispositivo registró potencia (vatios)
     * @param deviceWatts El nuevo valor
     */
    public void setDeviceWatts(Boolean deviceWatts) {
        this.deviceWatts = deviceWatts;
    }
    
    /**
     *   Comprueba si el usuario autenticado ha dado "Kudos"
     * @return true si ha dado Kudos, false en caso contrario
     */
    public Boolean getHasKudoed() {
        return hasKudoed;
    }
    
    /**
     *   Establece si el usuario autenticado ha dado "Kudos"
     * @param hasKudoed El nuevo valor
     */
    public void setHasKudoed(Boolean hasKudoed) {
        this.hasKudoed = hasKudoed;
    }
    
    /**
     *   Obtiene el número de logros
     * @return El número de logros
     */
    public Integer getAchievementCount() {
        return achievementCount;
    }
    
    /**
     *   Establece el número de logros
     * @param achievementCount El nuevo número de logros
     */
    public void setAchievementCount(Integer achievementCount) {
        this.achievementCount = achievementCount;
    }
    
    /**
     *   Obtiene el número de Kudos
     * @return El número de Kudos
     */
    public Integer getKudosCount() {
        return kudosCount;
    }
    
    /**
     *   Establece el número de Kudos
     * @param kudosCount El nuevo número de Kudos
     */
    public void setKudosCount(Integer kudosCount) {
        this.kudosCount = kudosCount;
    }
    
    /**
     *   Obtiene el número de comentarios
     * @return El número de comentarios
     */
    public Integer getCommentCount() {
        return commentCount;
    }
    
    /**
     *   Establece el número de comentarios
     * @param commentCount El nuevo número de comentarios
     */
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }
    
    /**
     *   Obtiene el número de atletas en la actividad
     * @return El número de atletas
     */
    public Integer getAthleteCount() {
        return athleteCount;
    }
    
    /**
     *   Establece el número de atletas en la actividad
     * @param athleteCount El nuevo número de atletas
     */
    public void setAthleteCount(Integer athleteCount) {
        this.athleteCount = athleteCount;
    }
    
    /**
     *   Obtiene el número de fotos
     * @return El número de fotos
     */
    public Integer getPhotoCount() {
        return photoCount;
    }
    
    /**
     *   Establece el número de fotos
     * @param photoCount El nuevo número de fotos
     */
    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }
    
    /**
     *   Obtiene el número total de fotos (incluyendo externas)
     * @return El número total de fotos
     */
    public Integer getTotalPhotoCount() {
        return totalPhotoCount;
    }
    
    /**
     *   Establece el número total de fotos
     * @param totalPhotoCount El nuevo número total de fotos
     */
    public void setTotalPhotoCount(Integer totalPhotoCount) {
        this.totalPhotoCount = totalPhotoCount;
    }
    
    /**
     *   Obtiene el número de Récords Personales (PR)
     * @return El número de PRs
     */
    public Integer getPrCount() {
        return prCount;
    }
    
    /**
     *   Establece el número de Récords Personales (PR)
     * @param prCount El nuevo número de PRs
     */
    public void setPrCount(Integer prCount) {
        this.prCount = prCount;
    }
    
    /**
     * Obtiene el ID del equipamiento
     * @return El ID del equipamiento
     */
    public String getGearId() {
        return gearId;
    }
    
    /**
     * Establece el ID del equipamiento
     * @param gearId El nuevo ID del equipamiento
     */
    public void setGearId(String gearId) {
        this.gearId = gearId;
    }
    
    /**
     * Obtiene el tipo de entrenamiento
     * @return El código del tipo de entrenamiento
     */
    public Integer getWorkoutType() {
        return workoutType;
    }
    
    /**
     * Establece el tipo de entrenamiento
     * @param workoutType El nuevo código del tipo de entrenamiento
     */
    public void setWorkoutType(Integer workoutType) {
        this.workoutType = workoutType;
    }
    
    /**
     * Obtiene el nombre del dispositivo
     * @return El nombre del dispositivo
     */
    public String getDeviceName() {
        return deviceName;
    }
    
    /**
     *  Establece el nombre del dispositivo
     * @param deviceName El nuevo nombre del dispositivo
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    /**
     * Obtiene el token de incrustación (embed)
     * @return El token
     */
    public String getEmbedToken() {
        return embedToken;
    }
    
    /**
     * Establece el token de incrustación (embed)
     * @param embedToken El nuevo token
     */
    public void setEmbedToken(String embedToken) {
        this.embedToken = embedToken;
    }
    
    // Métodos de utilidad
    
    /**
     * Calcula la distancia en kilómetros
     * Convierte la distancia (almacenada en metros) a kilómetros.
     * @return La distancia en km, o null si la distancia no está definida.
     */
    public Float getDistanceInKm() {
        return distance != null ? distance / 1000 : null;
    }
    
    /**
     * Calcula el tiempo transcurrido en minutos
     * Convierte el tiempo transcurrido (almacenado en segundos) a minutos.
     * @return El tiempo transcurrido en minutos, o null si no está definido.
     */
    public Integer getElapsedTimeInMinutes() {
        return elapsedTime != null ? elapsedTime / 60 : null;
    }
    
    /**
     * @return El tiempo en movimiento en minutos, o null si no está definido.
     */
    public Integer getMovingTimeInMinutes() {
        return movingTime != null ? movingTime / 60 : null;
    }
    
    /**
     * @return La velocidad media en km/h, o null si no está definida.
     */
    public Float getAverageSpeedKmh() {
        return averageSpeed != null ? averageSpeed * 3.6f : null;
    }
    
    /**
     * @return La velocidad máxima en km/h, o null si no está definida.
     */
    public Float getMaxSpeedKmh() {
        return maxSpeed != null ? maxSpeed * 3.6f : null;
    }
    
    /**
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