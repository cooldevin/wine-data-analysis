package com.example.sales.service;

import com.example.sales.dto.VisualizationData;
import com.example.sales.repository.SalesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataVisualizationService {

    private final SalesRepository salesRepository;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public DataVisualizationService(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    @Transactional(readOnly = true)
    public VisualizationData getVisualizationData(String timeRange, String dimension) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, timeRange);

        // Convert LocalDate to LocalDateTime for repository query
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Map<String, Object>> rawData = salesRepository.findSalesDataByDateRange(
            startDateTime, endDateTime);

        if (rawData == null || rawData.isEmpty()) {
            return createEmptyVisualizationData();
        }

        VisualizationData data = new VisualizationData();
        
        // Process time series data
        Map<String, List<Map<String, Object>>> groupedByDate = rawData.stream()
            .collect(Collectors.groupingBy(
                record -> (String) record.get("date"),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<String> dates = new ArrayList<>(groupedByDate.keySet());
        Collections.sort(dates);

        List<Double> sales = new ArrayList<>();
        List<Double> revenue = new ArrayList<>();
        List<Double> profit = new ArrayList<>();

        dates.forEach(date -> {
            List<Map<String, Object>> records = groupedByDate.get(date);
            sales.add(calculateTotal(records, "sales"));
            revenue.add(calculateTotal(records, "revenue"));
            profit.add(calculateTotal(records, "profit"));
        });

        data.setDates(dates);
        data.setSales(sales);
        data.setRevenue(revenue);
        data.setProfit(profit);

        // Process summary data
        List<VisualizationData.SummaryItem> summary = generateSummary(rawData, dimension);
        data.setSummary(summary);

        // Process details
        List<VisualizationData.DetailItem> details = rawData.stream()
            .map(this::convertToDetailItem)
            .collect(Collectors.toList());
        data.setDetails(details);

        return data;
    }

    private VisualizationData createEmptyVisualizationData() {
        VisualizationData data = new VisualizationData();
        data.setDates(new ArrayList<>());
        data.setSales(new ArrayList<>());
        data.setRevenue(new ArrayList<>());
        data.setProfit(new ArrayList<>());
        data.setSummary(new ArrayList<>());
        data.setDetails(new ArrayList<>());
        return data;
    }

    private LocalDate calculateStartDate(LocalDate endDate, String timeRange) {
        switch (timeRange) {
            case "7d":
                return endDate.minusDays(7);
            case "30d":
                return endDate.minusDays(30);
            case "90d":
                return endDate.minusDays(90);
            case "1y":
                return endDate.minusYears(1);
            default:
                return endDate.minusDays(30);
        }
    }

    private Double calculateTotal(List<Map<String, Object>> records, String field) {
        return records.stream()
            .mapToDouble(record -> {
                Object value = record.get(field);
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
                return 0.0;
            })
            .sum();
    }

    private List<VisualizationData.SummaryItem> generateSummary(
            List<Map<String, Object>> rawData, String dimension) {
        Map<String, Double> productTotals = rawData.stream()
            .collect(Collectors.groupingBy(
                record -> (String) record.get("product"),
                Collectors.summingDouble(record -> {
                    Object value = record.get(dimension);
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                    return 0.0;
                })
            ));

        return productTotals.entrySet().stream()
            .map(entry -> {
                VisualizationData.SummaryItem item = new VisualizationData.SummaryItem();
                item.setName(entry.getKey());
                item.setValue(entry.getValue());
                return item;
            })
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(5)
            .collect(Collectors.toList());
    }

    private VisualizationData.DetailItem convertToDetailItem(Map<String, Object> record) {
        VisualizationData.DetailItem item = new VisualizationData.DetailItem();
        item.setDate((String) record.get("date"));
        item.setProduct((String) record.get("product"));
        item.setSales(getDoubleValue(record.get("sales")));
        item.setRevenue(getDoubleValue(record.get("revenue")));
        item.setProfit(getDoubleValue(record.get("profit")));
        return item;
    }

    private Double getDoubleValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
}
