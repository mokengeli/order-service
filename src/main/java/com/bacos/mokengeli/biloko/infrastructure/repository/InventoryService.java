package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "inventory-service")
public interface InventoryService {

    @GetMapping("/api/inventory/product/{productId}")
    Optional<Product> findById(@PathVariable("productId") Long productId);
    @GetMapping("/api/inventory/product/by-ids")
    Optional<List<Product>> findByIds(@RequestParam("ids") List<Long> ids);
    @PutMapping("/api/inventory/article/remove")
    void removeArticle(@RequestBody List<ActionArticleRequest> removeProductRequests);
}
