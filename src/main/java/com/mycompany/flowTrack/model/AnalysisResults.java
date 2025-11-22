package com.mycompany.flowTrack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


/**
 *
 * @author diego
 */

/**
 * Modelo para mapear la respuesta JSON de Cycling Analytics.
 * Ignora propiedades desconocidas para evitar errores si la API cambia.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisResults {

    // ID de la actividad en Cycling Analytics (no el de Strava)
    private Long id;
    
    // Métricas principales
    private Double load; // TSS
    private Double intensity; // IF
    private Double variability; // VI
    @JsonProperty("epower") // Mapea "epower" del JSON a este campo
    private Double effectivePower; // Normalized Power
    private Double work; // Kilojoules
    
    // Promedios y Máximos calculados por CA
    @JsonProperty("avg_power")
    private Double avgPower;
    @JsonProperty("max_power")
    private Double maxPower;
    @JsonProperty("avg_heartrate")
    private Double avgHeartrate;
    
    // Curva de Potencia Crítica: Array de arrays [[segundos, watts], [segundos, watts]...]
    @JsonProperty("power_curve")
    private List<List<Number>> powerCurve;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getLoad() { return load; }
    public void setLoad(Double load) { this.load = load; }
    public Double getIntensity() { return intensity; }
    public void setIntensity(Double intensity) { this.intensity = intensity; }
    public Double getVariability() { return variability; }
    public void setVariability(Double variability) { this.variability = variability; }
    public Double getEffectivePower() { return effectivePower; }
    public void setEffectivePower(Double effectivePower) { this.effectivePower = effectivePower; }
    public Double getWork() { return work; }
    public void setWork(Double work) { this.work = work; }
    public Double getAvgPower() { return avgPower; }
    public void setAvgPower(Double avgPower) { this.avgPower = avgPower; }
    public Double getMaxPower() { return maxPower; }
    public void setMaxPower(Double maxPower) { this.maxPower = maxPower; }
    public Double getAvgHeartrate() { return avgHeartrate; }
    public void setAvgHeartrate(Double avgHeartrate) { this.avgHeartrate = avgHeartrate; }
    public List<List<Number>> getPowerCurve() { return powerCurve; }
    public void setPowerCurve(List<List<Number>> powerCurve) { this.powerCurve = powerCurve; }
}
