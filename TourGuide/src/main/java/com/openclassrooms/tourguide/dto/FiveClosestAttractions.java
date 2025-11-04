package com.openclassrooms.tourguide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FiveClosestAttractions(
        @JsonProperty("User's location lat/long")
        String location,

        @JsonProperty("Closest attractions")
        AttractionDetails[] attractionDetailsList
) {}
