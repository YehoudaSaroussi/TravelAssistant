package com.travelassistant.dto;

import lombok.Data;

@Data
public class CurrentWeather {
    private double temperature;
    private double windspeed;
    private int winddirection;
    private int weathercode;
    private String time;
}