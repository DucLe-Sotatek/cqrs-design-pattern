package com.javatechie.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record FilterProductReq(
        String name,
        String description,
        @JsonProperty("price_from") Double priceFrom,
        @JsonProperty("price_to") Double priceTo,
        int page,
        int size) {}
