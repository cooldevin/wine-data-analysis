package com.example.sales.controller;

import com.example.sales.annotation.Loggable;
import com.example.sales.entity.Product;
import com.example.sales.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.apache.shiro.authz.annotation.RequiresRoles;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }



    @GetMapping("/api/list")
    @ResponseBody
    @RequiresRoles("ROLE_USER")
    public ResponseEntity<Page<Product>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(productService.findProducts(name, PageRequest.of(page, size)));
    }

    @PostMapping("/api/save")
    @ResponseBody
    @RequiresRoles("ROLE_ADMIN")
    public ResponseEntity<?> saveProduct(@RequestBody Product product) {
        try {
            Product saved = productService.saveProduct(product);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    @RequiresRoles("ROLE_ADMIN")
    @Loggable(operation = "删除产品")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
