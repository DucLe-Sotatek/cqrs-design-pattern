package com.javatechie.repository;


import com.javatechie.entity.Product;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, String> {

    @Query(
            """
            select p from Product p where
                (:name is null or p.name = :name)
                and (:description is null or p.description = :description)
                and ( :priceFrom is null or p.price >= :priceFrom)
                and ( :priceTo is null or p.price <= :priceTo)
            """)
    List<Product> filterProducts(
            String name, String description, Double priceFrom, Double priceTo, Pageable pageable);
}
