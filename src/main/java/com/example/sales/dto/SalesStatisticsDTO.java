package com.example.sales.dto;

import java.math.BigDecimal;

public class SalesStatisticsDTO {
    private String groupKey;
    private Double totalAmount;

    public SalesStatisticsDTO(String groupKey, Double totalAmount) {
        this.groupKey = groupKey;
        this.totalAmount = totalAmount;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
