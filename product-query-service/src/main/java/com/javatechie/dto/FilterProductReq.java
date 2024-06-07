package com.javatechie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FilterProductReq(String name,
                               String description,
                               @JsonProperty("price_from") Integer priceFrom,
                               @JsonProperty("price_to") Integer priceTo,
                               int page,
                               int size) {
}
