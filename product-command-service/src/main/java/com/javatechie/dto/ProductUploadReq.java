package com.javatechie.dto;

import com.javatechie.entity.Product;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class ProductUploadReq {

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "description")
    private String description;

    @CsvBindByName(column = "amount")
    private int price;

    public Product toProduct() {
        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .build();
    }
}
