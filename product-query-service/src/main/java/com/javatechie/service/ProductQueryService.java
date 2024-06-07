package com.javatechie.service;

import com.google.common.collect.Lists;
import com.javatechie.dto.ProductEvent;
import com.javatechie.entity.Product;
import com.javatechie.entity.ProductSyncEvent;
import com.javatechie.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ProductQueryService {

    @Autowired
    private ProductRepository repository;

    public List<Product> getProducts() {
        return repository.findAll();
    }

    @KafkaListener(topics = "product-event-topic",groupId = "product-event-group")
    public void processProductEvents(ProductEvent productEvent) {
        Product product = productEvent.getProduct();
        if (productEvent.getEventType().equals("CreateProduct")) {
            repository.save(product);
        }
        if (productEvent.getEventType().equals("UpdateProduct")) {
            Product existingProduct = repository.findById(product.getId()).get();
            existingProduct.setName(product.getName());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setDescription(product.getDescription());
            repository.save(existingProduct);
        }
    }

    @KafkaListener(topics = "product_sync_all_v1", groupId = "product_sync_group")
    public void processSyncAllProductsEventV1(List<LinkedHashMap<Object, Object>> syncProdEvents) {
        final List<Product> products = syncProdEvents.stream().map(Product::new).toList();
        repository.saveAll(products);
        log.info("Synced :: {} record", products.size());
    }
}
