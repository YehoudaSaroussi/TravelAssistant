package com.travelassistant.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CountryInfoResponse {

    @JsonProperty("name")
    private String name;

    @JsonProperty("capital")
    private String capital;

    @JsonProperty("region")
    private String region;

    @JsonProperty("subregion")
    private String subregion;

    @JsonProperty("population")
    private long population;

    @JsonProperty("area")
    private double area;

    @JsonProperty("languages")
    private Map<String, String> languages;

    @JsonProperty("currencies")
    private Map<String, String> currencies;

    @JsonProperty("flag")
    private String flag;
}