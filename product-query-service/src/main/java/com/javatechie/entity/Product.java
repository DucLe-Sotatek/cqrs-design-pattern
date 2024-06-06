package com.javatechie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PRODUCT_QUERY", indexes = {
        @Index(columnList = "name"),
        @Index(columnList = "description"),
        @Index(columnList = "price"),

        @Index(columnList = "name, description"),
        @Index(columnList = "name, price"),

        @Index(columnList = "description, price"),
        @Index(columnList = "price, description"),
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String description;
    private double price;
}
