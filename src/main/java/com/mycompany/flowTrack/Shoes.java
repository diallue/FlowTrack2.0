/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.flowTrack.model;

/**
 *
 * @author diego
 */
public class Shoes {
    private String id;
    private boolean primary;
    private String name;
    private int resource_state;
    private double distance;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getResource_state() { return resource_state; }
    public void setResource_state(int resource_state) { this.resource_state = resource_state; }
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
}
