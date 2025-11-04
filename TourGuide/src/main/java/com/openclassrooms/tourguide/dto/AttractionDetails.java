package com.openclassrooms.tourguide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AttractionDetails(
    @JsonProperty("Name of attraction")
    String name,

    @JsonProperty("Attraction's lat/long")
    String latLong,

    @JsonProperty("Distance to the attraction")
    String distance,

    @JsonProperty("Reward points")
    Integer point
) {}
