package com.travelassistant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.travelassistant.dto.ForecastResponse;
import com.travelassistant.dto.GeocodingResponse;
import com.travelassistant.dto.WeatherResponse;
import com.travelassistant.exception.ExternalServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestTemplate restTemplate;

    @Value("${weather.geocoding.api.url}")
    private String geocodingApiUrl;

    @Value("${weather.forecast.api.url}")
    private String forecastApiUrl;

    public WeatherResponse getCurrentWeather(String location) {
        try {
            // Step 1: Get coordinates from location name using Geocoding API
            String geocodingUrl = UriComponentsBuilder.fromHttpUrl(geocodingApiUrl)
                    .queryParam("name", location)
                    .queryParam("count", 1)
                    .toUriString();

            GeocodingResponse geocodingResponse = restTemplate.getForObject(geocodingUrl, GeocodingResponse.class);

            if (geocodingResponse == null || geocodingResponse.getResults() == null ||
                    geocodingResponse.getResults().isEmpty()) {
                throw new ExternalServiceException("Location not found: " + location);
            }

            double latitude = geocodingResponse.getResults().get(0).getLatitude();
            double longitude = geocodingResponse.getResults().get(0).getLongitude();

            // Step 2: Get weather data using coordinates
            String forecastUrl = UriComponentsBuilder.fromHttpUrl(forecastApiUrl)
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("current_weather", true)
                    .toUriString();

            ForecastResponse forecastResponse = restTemplate.getForObject(forecastUrl, ForecastResponse.class);

            if (forecastResponse == null || forecastResponse.getCurrentWeather() == null) {
                throw new ExternalServiceException("Failed to get weather data for " + location);
            }

            // Convert to WeatherResponse
            return mapToWeatherResponse(forecastResponse, geocodingResponse, location);

        } catch (Exception e) {
            throw new ExternalServiceException("Failed to fetch weather data for " + location, e);
        }
    }

    private WeatherResponse mapToWeatherResponse(ForecastResponse forecastResponse,
            GeocodingResponse geocodingResponse,
            String location) {
        WeatherResponse response = new WeatherResponse();
        response.setLocation(location);
        response.setCountry(geocodingResponse.getResults().get(0).getCountry());
        response.setTemperature(forecastResponse.getCurrentWeather().getTemperature());
        response.setWindSpeed(forecastResponse.getCurrentWeather().getWindspeed());
        response.setWindDirection(forecastResponse.getCurrentWeather().getWinddirection());
        response.setWeatherCode(forecastResponse.getCurrentWeather().getWeathercode());
        response.setTimestamp(forecastResponse.getCurrentWeather().getTime());
        return response;
    }
}