package com.javatechie.entity;

import lombok.Data;

@Data
public class ProductSyncEvent {
    private String id;
    private String name;
    private String description;
    private double price;

    public Product toProduct() {
        return Product.builder().id(id).name(name).description(description).price(price).build();
    }
}
