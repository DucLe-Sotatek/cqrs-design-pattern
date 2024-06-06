package com.javatechie.service;

import com.google.common.collect.Lists;
import com.javatechie.dto.ProductEvent;
import com.javatechie.dto.ProductUploadReq;
import com.javatechie.entity.Product;
import com.javatechie.repository.ProductRepository;
import com.javatechie.util.CsvUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ProductCommandService {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public Product createProduct(ProductEvent productEvent) {
        Product productDO = repository.save(productEvent.getProduct());
        ProductEvent event = new ProductEvent("CreateProduct", productDO);
        kafkaTemplate.send("product-event-topic", event);
        return productDO;
    }

    public Product updateProduct(long id, ProductEvent productEvent) {
        Product existingProduct = repository.findById(id).get();
        Product newProduct = productEvent.getProduct();
        existingProduct.setName(newProduct.getName());
        existingProduct.setPrice(newProduct.getPrice());
        existingProduct.setDescription(newProduct.getDescription());
        Product productDO = repository.save(existingProduct);
        ProductEvent event = new ProductEvent("UpdateProduct", productDO);
        kafkaTemplate.send("product-event-topic", event);
        return productDO;
    }

    public long uploadProducts(MultipartFile file) {
        final List<ProductUploadReq> reqs = CsvUtil.convertToModel(file, ProductUploadReq.class);
        log.info("convert from csv to java model finished !");
        final List<Product> products = reqs.stream().map(ProductUploadReq::toProduct).toList();
        log.info("convert from java model to entity finished !");
        bulkSave(products, 1000);
        log.info("save all product finished!");
        long l = repository.count();
        log.info("count finished!");
        return l;
    }


    public void bulkSave(List<Product> products, int batchSize) {
        Lists.partition(products, batchSize).forEach(
                child -> repository.saveAll(child)
        );
    }
}
