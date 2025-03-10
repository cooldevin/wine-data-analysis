package com.example.sales.dto;

import java.util.List;
import java.util.Map;

public class VisualizationData {
    private List<String> dates;
    private List<Double> sales;
    private List<Double> revenue;
    private List<Double> profit;
    private List<SummaryItem> summary;
    private List<DetailItem> details;

    public static class SummaryItem {
        private String name;
        private Double value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    public static class DetailItem {
        private String date;
        private String product;
        private Double sales;
        private Double revenue;
        private Double profit;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public Double getSales() {
            return sales;
        }

        public void setSales(Double sales) {
            this.sales = sales;
        }

        public Double getRevenue() {
            return revenue;
        }

        public void setRevenue(Double revenue) {
            this.revenue = revenue;
        }

        public Double getProfit() {
            return profit;
        }

        public void setProfit(Double profit) {
            this.profit = profit;
        }
    }

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<Double> getSales() {
        return sales;
    }

    public void setSales(List<Double> sales) {
        this.sales = sales;
    }

    public List<Double> getRevenue() {
        return revenue;
    }

    public void setRevenue(List<Double> revenue) {
        this.revenue = revenue;
    }

    public List<Double> getProfit() {
        return profit;
    }

    public void setProfit(List<Double> profit) {
        this.profit = profit;
    }

    public List<SummaryItem> getSummary() {
        return summary;
    }

    public void setSummary(List<SummaryItem> summary) {
        this.summary = summary;
    }

    public List<DetailItem> getDetails() {
        return details;
    }

    public void setDetails(List<DetailItem> details) {
        this.details = details;
    }
}
