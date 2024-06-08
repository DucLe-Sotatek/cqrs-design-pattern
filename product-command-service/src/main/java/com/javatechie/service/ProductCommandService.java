package com.javatechie.service;


import com.google.common.collect.Lists;
import com.javatechie.dto.ProductEvent;
import com.javatechie.dto.ProductUploadReq;
import com.javatechie.entity.Product;
import com.javatechie.repository.ProductRepository;
import com.javatechie.util.CsvUtil;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ProductCommandService {

    @Autowired private ProductRepository productRepository;

    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;

    private static final int BATCH_SIZE = 1000;

    public Product createProduct(ProductEvent productEvent) {
        Product productDO = productRepository.save(productEvent.getProduct());
        ProductEvent event = new ProductEvent("CreateProduct", productDO);
        kafkaTemplate.send("product-event-topic", event);
        return productDO;
    }

    public Product updateProduct(String id, ProductEvent productEvent) {
        Product existingProduct = productRepository.findById(id).get();
        Product newProduct = productEvent.getProduct();
        existingProduct.setName(newProduct.getName());
        existingProduct.setPrice(newProduct.getPrice());
        existingProduct.setDescription(newProduct.getDescription());
        Product productDO = productRepository.save(existingProduct);
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
        long l = productRepository.count();
        log.info("count finished!");
        return l;
    }

    public void bulkSave(List<Product> products, int batchSize) {
        Lists.partition(products, batchSize).forEach(child -> productRepository.saveAll(child));
    }

    public long syncProductsV1() {
        List<Product> products = productRepository.findAll();
        Lists.partition(products, 1000)
                .forEach(
                        part -> {
                            ArrayList<Product> data = new ArrayList<>(part);
                            kafkaTemplate.send("product_sync_all_v1", data);
                            log.info("Sent {} prods", part.size());
                        });
        return 1;
    }

    public void doUploadAndSync(MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("file is empty!");
        }

        CsvParserSettings settings = new CsvParserSettings();
        settings.setMaxCharsPerColumn(10000); // Adjust as necessary
        settings.setDelimiterDetectionEnabled(true);
        CsvParser parser = new CsvParser(settings);

        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            parser.beginParsing(reader);

            String[] product;
            List<Product> products = new ArrayList<>();
            while ((product = parser.parseNext()) != null) {
                final Product pr = getFromRow(product);
                products.add(pr);
                if (BATCH_SIZE == products.size()) {
                    processBatch(products);
                    products.clear(); // Clear the buffer after processing
                }
            }

            // Process any remaining records
            if (!products.isEmpty()) {
                processBatch(products);
            }

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Product getFromRow(String[] row) {
        return Product.builder()
                .description(row[1])
                .name(row[2])
                .price(Double.parseDouble(row[3]))
                .build();
    }

    private void processBatch(List<Product> products) {
        List<Product> savedProducts = productRepository.saveAll(products);
        kafkaTemplate.send("product_sync_all_v1", savedProducts);
    }
}
