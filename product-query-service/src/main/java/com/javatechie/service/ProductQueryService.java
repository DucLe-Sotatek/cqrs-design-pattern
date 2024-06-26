package com.javatechie.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.dto.FilterProductReq;
import com.javatechie.dto.ProductEvent;
import com.javatechie.entity.Product;
import com.javatechie.repository.ProductRepository;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductQueryService {

    @Autowired private ProductRepository repository;
    @Autowired private ObjectMapper objectMapper;

    public List<Product> getProducts(FilterProductReq req) {
        final Pageable pageable = PageRequest.of(req.page(), req.size());
        return repository.filterProducts(
                req.name(), req.description(), req.priceFrom(), req.priceTo(), pageable);
    }

    @KafkaListener(topics = "product-event-topic", groupId = "product-event-group")
    public void processProductEvents(ProductEvent productEvent) {
        long start = System.currentTimeMillis();
        createOrUpdateProduct(productEvent);
        log.info("createOrUpdateProduct took {}", System.currentTimeMillis() - start);
    }

    private void createOrUpdateProduct(ProductEvent productEvent) {
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
        final List<Product> products = syncProdEvents.stream().map(el -> objectMapper.convertValue(el, Product.class)).toList();
        repository.saveAll(products);
        log.info("Synced :: {} record", products.size());
    }
}
