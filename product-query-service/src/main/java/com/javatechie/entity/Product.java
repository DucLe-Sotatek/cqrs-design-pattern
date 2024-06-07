package com.javatechie.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "PRODUCT_QUERY",
        indexes = {
            @Index(columnList = "name"),
            @Index(columnList = "description"),
            @Index(columnList = "price"),
            @Index(columnList = "name, description"),
            @Index(columnList = "name, price"),
            @Index(columnList = "description, price"),
            @Index(columnList = "price, description"),
        })
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id private String id;
    private String name;
    private String description;
    private double price;
}
