package com.example.sales.service;

import com.example.sales.entity.Product;
import com.example.sales.repository.ProductRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> findProducts(String name, Pageable pageable) {
        if (StringUtils.isBlank(name)) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByNameContaining(name, pageable);
    }

    @Transactional
    public Product saveProduct(Product product) {
        if (product.getId() != null) {
            Product existing = productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("产品不存在"));
            existing.setName(product.getName());
            existing.setDescription(product.getDescription());
            existing.setPrice(product.getPrice());
            existing.setCategory(product.getCategory());
            return productRepository.save(existing);
        }
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("产品不存在");
        }
        productRepository.deleteById(id);
    }
}
