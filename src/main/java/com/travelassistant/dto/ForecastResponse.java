package com.travelassistant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ForecastResponse {
    private double latitude;
    private double longitude;
    private String timezone;

    @JsonProperty("current_weather")
    private CurrentWeather currentWeather;
}