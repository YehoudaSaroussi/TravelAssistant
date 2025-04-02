package com.travelassistant.dto;

import lombok.Data;

@Data
public class GeocodingResult {
    private String name;
    private double latitude;
    private double longitude;
    private String country;
}