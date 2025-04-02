package com.travelassistant.dto;

import java.util.List;
import lombok.Data;

@Data
public class GeocodingResponse {
    private List<GeocodingResult> results;
}