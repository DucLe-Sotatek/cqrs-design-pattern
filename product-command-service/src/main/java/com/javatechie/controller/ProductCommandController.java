package com.javatechie.controller;


import com.javatechie.dto.ProductEvent;
import com.javatechie.entity.Product;
import com.javatechie.service.ProductCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/products")
public class ProductCommandController {

    @Autowired private ProductCommandService commandService;

    @PostMapping
    public Product createProduct(@RequestBody ProductEvent productEvent) {
        return commandService.createProduct(productEvent);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable String id, @RequestBody ProductEvent productEvent) {
        return commandService.updateProduct(id, productEvent);
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public long uploadProducts(@RequestParam("file") MultipartFile file) {
        return commandService.uploadProducts(file);
    }

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.CREATED)
    public long syncProducts() {
        return commandService.syncProductsV1();
    }

    @PostMapping("/upload_and_sync")
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadAndSync(@RequestParam("file") MultipartFile file) {
        commandService.doUploadAndSync(file);
    }
}
