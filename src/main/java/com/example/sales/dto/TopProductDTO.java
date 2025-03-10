package com.example.sales.dto;

import java.math.BigDecimal;

public class TopProductDTO {
    private String productName;
    private Long quantity;
    private BigDecimal totalRevenue;
    private BigDecimal averagePrice;

    public TopProductDTO(String productName, Long quantity, BigDecimal totalRevenue) {
        this.productName = productName;
        this.quantity = quantity;
        this.totalRevenue = totalRevenue;
        this.averagePrice = quantity > 0 ? totalRevenue.divide(BigDecimal.valueOf(quantity), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }
}
