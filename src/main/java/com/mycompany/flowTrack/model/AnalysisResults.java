package com.mycompany.flowTrack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


/**
 * Modelo para mapear la respuesta JSON recibida desde la API de Cycling Analytics.
 *
 * <p>Esta clase representa las métricas avanzadas calculadas por Cycling Analytics
 * a partir de una actividad previamente enviada. Incluye métricas de rendimiento
 * como TSS (load), IF (intensity), VI (variability), potencia normalizada,
 * trabajo total en Kj, entre otras.</p>
 *
 * <p>La anotación {@link JsonIgnoreProperties} asegura que si la API devuelve
 * nuevas propiedades no contempladas en este modelo, no provoquen errores al
 * deserializar.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisResults {

    /** ID de la actividad dentro de Cycling Analytics (no coincide con Strava). */
    private Long id;

    /** Training Stress Score (TSS), carga total de la actividad. */
    private Double load;

    /** Intensidad relativa (IF), basada en la potencia normalizada. */
    private Double intensity;

    /** Variability Index (VI), relación entre NP y potencia media. */
    private Double variability;

    /** Potencia normalizada ("epower" en la API de Cycling Analytics). */
    @JsonProperty("epower")
    private Double effectivePower;

    /** Trabajo total realizado en kilojulios (kJ). */
    private Double work;

    /** Potencia media calculada por Cycling Analytics. */
    @JsonProperty("avg_power")
    private Double avgPower;

    /** Potencia máxima registrada durante la actividad. */
    @JsonProperty("max_power")
    private Double maxPower;

    /** Frecuencia cardíaca media. */
    @JsonProperty("avg_heartrate")
    private Double avgHeartrate;

    /**
     * Curva de Potencia Crítica:
     * lista de pares [duración_en_segundos, potencia].
     */
    @JsonProperty("power_curve")
    private List<List<Number>> powerCurve;

    // --- Getters y setters con Javadoc ---

    /** @return ID de la actividad en Cycling Analytics. */
    public Long getId() { return id; }

    /** @param id Nuevo ID de la actividad en Cycling Analytics. */
    public void setId(Long id) { this.id = id; }

    /** @return Carga total (TSS). */
    public Double getLoad() { return load; }

    /** @param load Carga total (TSS). */
    public void setLoad(Double load) { this.load = load; }

    /** @return Intensidad relativa (IF). */
    public Double getIntensity() { return intensity; }

    /** @param intensity Intensidad relativa (IF). */
    public void setIntensity(Double intensity) { this.intensity = intensity; }

    /** @return Variability Index (VI). */
    public Double getVariability() { return variability; }

    /** @param variability Variability Index (VI). */
    public void setVariability(Double variability) { this.variability = variability; }

    /** @return Potencia normalizada (NP). */
    public Double getEffectivePower() { return effectivePower; }

    /** @param effectivePower Potencia normalizada (NP). */
    public void setEffectivePower(Double effectivePower) { this.effectivePower = effectivePower; }

    /** @return Trabajo total en kilojulios. */
    public Double getWork() { return work; }

    /** @param work Trabajo total en kilojulios. */
    public void setWork(Double work) { this.work = work; }

    /** @return Potencia media. */
    public Double getAvgPower() { return avgPower; }

    /** @param avgPower Potencia media. */
    public void setAvgPower(Double avgPower) { this.avgPower = avgPower; }

    /** @return Potencia máxima. */
    public Double getMaxPower() { return maxPower; }

    /** @param maxPower Potencia máxima. */
    public void setMaxPower(Double maxPower) { this.maxPower = maxPower; }

    /** @return Frecuencia cardíaca media. */
    public Double getAvgHeartrate() { return avgHeartrate; }

    /** @param avgHeartrate Frecuencia cardíaca media. */
    public void setAvgHeartrate(Double avgHeartrate) { this.avgHeartrate = avgHeartrate; }

    /**
     * @return Curva de potencia crítica.
     */
    public List<List<Number>> getPowerCurve() { return powerCurve; }

    /** @param powerCurve Curva de potencia crítica. */
    public void setPowerCurve(List<List<Number>> powerCurve) { this.powerCurve = powerCurve; }
}
