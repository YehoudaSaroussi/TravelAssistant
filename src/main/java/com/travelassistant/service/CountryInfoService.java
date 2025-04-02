package com.travelassistant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.travelassistant.dto.CountryInfoResponse;

@Service
public class CountryInfoService {

    private final RestTemplate restTemplate;

    @Value("${countryinfo.api.url}")
    private String countryInfoApiUrl;

    public CountryInfoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CountryInfoResponse getCountryInfo(String countryName) {
        String url = countryInfoApiUrl + "/" + countryName;
        CountryInfoResponse response = restTemplate.getForObject(url, CountryInfoResponse.class);
        return response;
    }
}