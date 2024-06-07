package com.javatechie.service;

import com.javatechie.dto.FilterProductReq;
import com.javatechie.dto.ProductEvent;
import com.javatechie.entity.Product;
import com.javatechie.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Filter;

@Slf4j
@Service
public class ProductQueryService {

    @Autowired
    private ProductRepository repository;

    public List<Product> getProducts(FilterProductReq req) {
        final Pageable pageable = PageRequest.of(req.page(), req.size());
        return repository.filterProducts(req.name(), req.description(),req.priceFrom(), req.priceTo(), pageable);
    }

    @KafkaListener(topics = "product-event-topic", groupId = "product-event-group")
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
