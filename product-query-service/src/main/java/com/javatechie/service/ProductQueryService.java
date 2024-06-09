package com.javatechie.service;


import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.javatechie.dto.FilterProductReq;
import com.javatechie.dto.ProductEvent;
import com.javatechie.entity.Product;
import com.javatechie.repository.ProductRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private static final int MAX_ROW_HANDLED_IN_MOMENT = 1000;
    private static final int MAX_ROW_HANDLED_IN_ONE_TREAD = 100;
    private static final int MAX_HANDLE_THREAD = 10;

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
        long start = new Date().getTime();
        log.info("Message is started processing at :: {}", start);
        final List<Product> products =
                syncProdEvents.stream()
                        .map(el -> objectMapper.convertValue(el, Product.class))
                        .toList();
        final ExecutorService executorService = Executors.newFixedThreadPool(MAX_HANDLE_THREAD);
        final List<CompletableFuture<List<Product>>> futures = new ArrayList<>();
        Lists.partition(products, MAX_ROW_HANDLED_IN_ONE_TREAD).forEach(
                part -> {
                    futures.add(
                            CompletableFuture.supplyAsync(
                                    () -> repository.saveAll(part),
                                    executorService
                            )
                    );
                }
        );
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("Saved {} products in read server", products.size());
        log.info("Message was finished processed in :: {}", System.currentTimeMillis() - start);
    }
}
