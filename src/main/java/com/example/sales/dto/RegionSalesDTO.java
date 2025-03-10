package com.example.sales.dto;

public class RegionSalesDTO {
    private String region;
    private Double totalSales;

    public RegionSalesDTO(String region, Double totalSales) {
        this.region = region;
        this.totalSales = totalSales;
    }

    // Getters and Setters
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Double totalSales) {
        this.totalSales = totalSales;
    }
}