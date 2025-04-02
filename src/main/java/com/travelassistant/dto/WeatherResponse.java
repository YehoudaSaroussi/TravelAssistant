package com.travelassistant.dto;

import lombok.Data;

@Data
public class WeatherResponse {
    private String location;
    private String country;
    private double temperature;
    private double windSpeed;
    private int windDirection;
    private int weatherCode;
    private String timestamp;

    /**
     * Gets a human-readable weather description based on the weather code.
     * Weather codes follow WMO (World Meteorological Organization) standards.
     */
    public String getDescription() {
        return switch (weatherCode) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Foggy";
            case 51 -> "Light drizzle";
            case 53 -> "Moderate drizzle";
            case 55 -> "Dense drizzle";
            case 56, 57 -> "Freezing drizzle";
            case 61 -> "Light rain";
            case 63 -> "Moderate rain";
            case 65 -> "Heavy rain";
            case 66, 67 -> "Freezing rain";
            case 71 -> "Light snow";
            case 73 -> "Moderate snow";
            case 75 -> "Heavy snow";
            case 77 -> "Snow grains";
            case 80 -> "Light rain showers";
            case 81 -> "Moderate rain showers";
            case 82 -> "Violent rain showers";
            case 85 -> "Light snow showers";
            case 86 -> "Heavy snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Unknown weather condition";
        };
    }
}