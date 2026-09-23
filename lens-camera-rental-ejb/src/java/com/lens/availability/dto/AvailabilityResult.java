package com.lens.availability.dto;

import java.io.Serializable;

/**
 *
 * @author Duong Ngoc Han
 */
public class AvailabilityResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean available;
    private final int totalPhysicalDevices;
    private final int occupiedCapacity;
    private final int availableCapacity;
    private final String message;

    public AvailabilityResult(boolean available, int totalPhysicalDevices, int occupiedCapacity, int availableCapacity, String message) {
        this.available = available;
        this.totalPhysicalDevices = totalPhysicalDevices;
        this.occupiedCapacity = occupiedCapacity;
        this.availableCapacity = availableCapacity;
        this.message = message;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getTotalPhysicalDevices() {
        return totalPhysicalDevices;
    }

    public int getOccupiedCapacity() {
        return occupiedCapacity;
    }

    public int getAvailableCapacity() {
        return availableCapacity;
    }

    public String getMessage() {
        return message;
    }
    
}
