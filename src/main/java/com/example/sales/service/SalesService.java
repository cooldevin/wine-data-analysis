package com.example.sales.service;

import com.example.sales.annotation.Loggable;
import com.example.sales.dto.*;
import com.example.sales.entity.Sales;
import com.example.sales.repository.SalesRepository;
import com.example.sales.repository.SalesRepository.ProductSalesStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.time.LocalDate;


@Service
public class SalesService {

    private final SalesRepository salesRepository;

    @Autowired
    public SalesService(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    @Loggable(operation = "导入销售记录")
    @Transactional
    public Sales saveSalesFromImport(SalesImportDTO dto) {
        Sales sales = new Sales();
        sales.setProductName(dto.getProductName());
        sales.setSalesRegion(dto.getSalesRegion());
        sales.setSalesDate(LocalDateTime.ofInstant(
                dto.getSalesDate().toInstant(),
                java.time.ZoneId.systemDefault()
        ));
        sales.setSalesQuantity(dto.getSalesQuantity());
        sales.setUnitPrice(dto.getUnitPrice());
        sales.setTotalAmount(dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getSalesQuantity())));

        return salesRepository.save(sales);
    }

    @Loggable(operation = "批量导入销售记录")
    @Transactional
    public void saveBatchSales(List<Sales> salesList, String importId, String importBatch) {
        salesList.forEach(sales -> {
            sales.setImportId(importId);
            sales.setImportBatch(importBatch);
        });
        salesRepository.saveAll(salesList);
    }

    @Loggable(operation = "获取导入状态")
    public Map<String, Object> getImportStatus(String importId) {
        Map<String, Object> status = new HashMap<>();
        Long totalRecords = salesRepository.countByImportId(importId);
        status.put("totalRecords", totalRecords);
        status.put("status", totalRecords > 0 ? "completed" : "processing");
        return status;
    }

    @Loggable(operation = "查询销售记录")
    public Page<Sales> findSales(String productName, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable, String salesByRegion) {
        if (productName != null && !productName.isEmpty()) {
            if (startDate != null && endDate != null) {
                return salesRepository.findByProductNameAndCreatedAtBetweenAndSalesRegion(
                        productName, startDate, endDate, salesByRegion, pageable);
            }
            return salesRepository.findByProductNameContainingAndSalesRegion(productName, salesByRegion, pageable);
        }

        if (startDate != null && endDate != null) {
            return salesRepository.findBySalesDateBetween(startDate, endDate, pageable);
        }

        return salesRepository.findAll(pageable);
    }

    @Loggable(operation = "添加销售记录")
    @Transactional
    public Sales addSales(Sales sales) {
        // 计算总金额
        sales.setTotalAmount(sales.getUnitPrice().multiply(BigDecimal.valueOf(sales.getSalesQuantity())));
        return salesRepository.save(sales);
    }

    @Loggable(operation = "批量保存销售记录")
    @Transactional
    public void saveAll(List<Sales> salesList) {
        salesRepository.saveAll(salesList);
    }

    @Loggable(operation = "获取销量排名")
    public Page<ProductSalesStats> getTopSellingProducts(Pageable pageable) {
        return salesRepository.findTopSellingProducts(pageable);
    }

    @Loggable(operation = "统计区域销售额")
    public List<RegionSalesDTO> getSalesByRegion() {
        List<Object[]> results = salesRepository.findSalesByRegion();
        return results.stream()
                .map(row -> new RegionSalesDTO(
                        (String) row[0],
                        ((Number) row[1]).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    @Loggable(operation = "统计月度销售额")
    public List<SalesStatisticsDTO> getSalesByMonth() {
        List<Object[]> results = salesRepository.findSalesByMonth();
        return results.stream()
                .map(row -> new SalesStatisticsDTO(
                        (String) row[0],
                        ((Number) row[1]).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    @Loggable(operation = "计算时间段销售总额")
    public Double calculateTotalSales(LocalDateTime startDate, LocalDateTime endDate) {
        return salesRepository.calculateTotalSalesAmount(startDate, endDate);
    }

    @Loggable(operation = "获取所有销售区域")
    public List<String> getAllRegions() {
        return salesRepository.findDistinctRegions();
    }

    @Loggable(operation = "获取区域销售统计")
    public List<RegionalSalesStats> getRegionStatistics(String region, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results;
        if (region != null && !region.isEmpty()) {
            results = salesRepository.findRegionStatisticsForRegion(region, startDate, endDate);
        } else {
            results = salesRepository.findRegionStatisticsForAllRegions(startDate, endDate);
        }

        return results.stream()
                .map(row -> new RegionalSalesStats(
                        (String) row[0],
                        ((Number) row[1]).doubleValue(),
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
    }

    @Loggable(operation = "获取销售概览数据")
    public SalesOverviewDTO getSalesOverview(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        SalesOverviewDTO overview = new SalesOverviewDTO();

        // 计算总销售额和订单数
        Double totalSales = calculateTotalSales(start, end);
        overview.setTotalSales(BigDecimal.valueOf(totalSales != null ? totalSales : 0.0));

        Long orderCount = salesRepository.countBySalesDateBetween(start, end);
        overview.setTotalOrders(orderCount);

        // 计算平均订单金额
        if (orderCount > 0) {
            overview.setAverageOrderValue(overview.getTotalSales().divide(
                    BigDecimal.valueOf(orderCount), 2, BigDecimal.ROUND_HALF_UP));
        } else {
            overview.setAverageOrderValue(BigDecimal.ZERO);
        }

        // 获取区域销售数据
        List<Object[]> regionSales = salesRepository.findRegionStatisticsForAllRegions(start, end);
        Map<String, BigDecimal> salesByRegion = regionSales.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> BigDecimal.valueOf(((Number) row[1]).doubleValue()),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
        overview.setSalesByRegion(salesByRegion);

        // 获取热销产品
        List<Object[]> topProducts = salesRepository.findTopSellingProductsWithRevenue(start, end);
        List<TopProductDTO> topProductsList = topProducts.stream()
                .map(row -> new TopProductDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        BigDecimal.valueOf(((Number) row[2]).doubleValue())
                ))
                .limit(5)
                .collect(Collectors.toList());
        overview.setTopProducts(topProductsList);

        // 获取销售趋势
        List<Object[]> salesTrend = salesRepository.findSalesTrendByDay(start, end);
        Map<String, BigDecimal> trendMap = salesTrend.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> BigDecimal.valueOf(((Number) row[1]).doubleValue()),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
        overview.setSalesTrend(trendMap);

        return overview;
    }

    @Loggable(operation = "查询所有销售记录")
    public Page<Sales> findAll(Pageable pageable) {
        return salesRepository.findAll(pageable);
    }

    @Loggable(operation = "条件查询销售记录")
    public Page<Sales> findByCondition(String productName, String salesRegion, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return salesRepository.findByCondition(productName, salesRegion, startDate, endDate, pageable);
    }

    @Loggable(operation = "按产品名称查询")
    public Page<Sales> findByProductName(String productName, String salesRegion, Pageable pageable) {
        return salesRepository.findByProductNameContainingAndSalesRegion(productName, salesRegion, pageable);
    }

    @Loggable(operation = "按日期范围查询")
    public Page<Sales> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return salesRepository.findBySalesDateBetween(startDate, endDate, pageable);
    }

    @Loggable(operation = "按产品名称和日期范围查询")
    public Page<Sales> findByProductNameAndSalesRegionAndDateRange(
            String productName,
            String salesRegion,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        return salesRepository.findByProductNameAndCreatedAtBetweenAndSalesRegion(
                productName, startDate, endDate, salesRegion, pageable);
    }

    @Loggable(operation = "保存销售记录")
    @Transactional
    public Sales save(Sales sales) {
        // 计算总金额
        if (sales.getUnitPrice() != null && sales.getSalesQuantity() != null) {
            sales.setTotalAmount(sales.getUnitPrice().multiply(
                    BigDecimal.valueOf(sales.getSalesQuantity())
            ));
        }
        return salesRepository.save(sales);
    }

    @Loggable(operation = "更新销售记录")
    @Transactional
    public Sales update(Sales sales) {
        // 确保记录存在
        if (!salesRepository.existsById(sales.getId())) {
            throw new IllegalArgumentException("销售记录不存在: " + sales.getId());
        }
        // 计算总金额
        if (sales.getUnitPrice() != null && sales.getSalesQuantity() != null) {
            sales.setTotalAmount(sales.getUnitPrice().multiply(
                    BigDecimal.valueOf(sales.getSalesQuantity())
            ));
        }
        return salesRepository.save(sales);
    }

    @Loggable(operation = "删除销售记录")
    @Transactional
    public void delete(Long id) {
        if (!salesRepository.existsById(id)) {
            throw new IllegalArgumentException("销售记录不存在: " + id);
        }
        salesRepository.deleteById(id);
    }

    public Page<SalesDTO> querySales(String productName, String salesRegion,
                                     String startDate, String endDate,
                                     int page, int size) {
        // Implementation of querySales method
        return null; // Placeholder return, actual implementation needed
    }
}
