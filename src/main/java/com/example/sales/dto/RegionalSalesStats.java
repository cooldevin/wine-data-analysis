package com.example.sales.dto;

import java.math.BigDecimal;

public class RegionalSalesStats {
    private String region;
    private Double totalAmount;
    private Long orderCount;
    private BigDecimal averagePrice;

    public RegionalSalesStats() {}

    public RegionalSalesStats(String region, Double totalAmount, Long orderCount) {
        this.region = region;
        this.totalAmount = totalAmount;
        this.orderCount = orderCount;
        this.averagePrice = orderCount > 0 
            ? BigDecimal.valueOf(totalAmount).divide(BigDecimal.valueOf(orderCount), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
        updateAveragePrice();
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
        updateAveragePrice();
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    private void updateAveragePrice() {
        if (totalAmount != null && orderCount != null && orderCount > 0) {
            this.averagePrice = BigDecimal.valueOf(totalAmount)
                .divide(BigDecimal.valueOf(orderCount), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.averagePrice = BigDecimal.ZERO;
        }
    }
}
