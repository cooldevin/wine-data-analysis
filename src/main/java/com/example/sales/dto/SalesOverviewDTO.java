package com.example.sales.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SalesOverviewDTO {
    private BigDecimal totalSales;
    private Long totalOrders;
    private BigDecimal averageOrderValue;
    private Map<String, BigDecimal> salesByRegion;
    private List<TopProductDTO> topProducts;
    private Map<String, BigDecimal> salesTrend;

    private Double growthRate;

    public SalesOverviewDTO() {}

    public Double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(Double growthRate) {
        this.growthRate = growthRate;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public Map<String, BigDecimal> getSalesByRegion() {
        return salesByRegion;
    }

    public void setSalesByRegion(Map<String, BigDecimal> salesByRegion) {
        this.salesByRegion = salesByRegion;
    }

    public List<TopProductDTO> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TopProductDTO> topProducts) {
        this.topProducts = topProducts;
    }

    public Map<String, BigDecimal> getSalesTrend() {
        return salesTrend;
    }

    public void setSalesTrend(Map<String, BigDecimal> salesTrend) {
        this.salesTrend = salesTrend;
    }
}
