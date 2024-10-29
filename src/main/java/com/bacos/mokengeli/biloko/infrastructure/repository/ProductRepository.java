package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "inventory-service")
public interface ProductRepository {

    @GetMapping("/api/inventory/product/{productId}")
    Optional<Product> findById(@PathVariable("productId") Long productId);
    @GetMapping("/api/inventory/product/by-ids")
    Optional<List<Product>> findByIds(@RequestParam("ids") List<Long> ids);
}
