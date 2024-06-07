package com.javatechie.controller;


import com.javatechie.dto.FilterProductReq;
import com.javatechie.entity.Product;
import com.javatechie.service.ProductQueryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/products")
@RestController
public class ProductQueryController {

    @Autowired private ProductQueryService queryService;

    @GetMapping
    public List<Product> fetchAllProducts(@RequestBody FilterProductReq req) {
        return queryService.getProducts(req);
    }
}
