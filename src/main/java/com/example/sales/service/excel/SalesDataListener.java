package com.example.sales.service.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.sales.dto.SalesImportDTO;
import com.example.sales.entity.Sales;
import com.example.sales.service.SalesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

@Slf4j

public class SalesDataListener extends AnalysisEventListener<SalesImportDTO> {
    private final BiConsumer<SalesImportDTO, Integer> rowCallback;
    private final Runnable completionCallback;
    private final AtomicInteger totalRows = new AtomicInteger(0);
    private final AtomicInteger successRows = new AtomicInteger(0);
    private final List<String> errorMessages = new ArrayList<>();
    private final List<Sales> salesList = new ArrayList<>();
    private final SalesService salesService;
    private final String importId;

    public SalesDataListener(BiConsumer<SalesImportDTO, Integer> rowCallback, Runnable completionCallback,
                          SalesService salesService, String importId) {
        this.rowCallback = rowCallback;
        this.completionCallback = completionCallback;
        this.salesService = salesService;
        this.importId = importId;
    }

    @Override
    public void invoke(SalesImportDTO data, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex() + 1;
        totalRows.incrementAndGet();
        try {
            validateData(data, rowIndex);
            Sales sales = convertToEntity(data);
            sales.setImportId(importId);
            salesList.add(sales);
            
            if (salesList.size() >= 1000) {
                saveData();
            }
            
            rowCallback.accept(data, rowIndex);
            successRows.incrementAndGet();
        } catch (Exception e) {
            log.error("处理第{}行数据失败: {}", rowIndex, e.getMessage());
            errorMessages.add(String.format("第%d行: %s", rowIndex, e.getMessage()));
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        try {
            if (!salesList.isEmpty()) {
                saveData();
            }
            log.info("数据处理完成，总行数：{}，成功行数：{}，错误行数：{}",
                totalRows.get(), successRows.get(), errorMessages.size());
            completionCallback.run();
        } catch (Exception e) {
            log.error("保存剩余数据失败", e);
            errorMessages.add("保存数据失败: " + e.getMessage());
        }
    }

    private void validateData(SalesImportDTO data, int rowIndex) {
        List<String> errors = new ArrayList<>();

        if (!StringUtils.hasText(data.getProductName())) {
            errors.add("产品名称不能为空");
        }

        if (!StringUtils.hasText(data.getSalesRegion())) {
            errors.add("销售区域不能为空");
        }

        if (data.getSalesDate() == null) {
            errors.add("销售日期不能为空");
        }

        if (data.getSalesQuantity() == null || data.getSalesQuantity() <= 0) {
            errors.add("销售数量必须大于0");
        }

        if (data.getUnitPrice() == null || data.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("销售单价必须大于0");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.format("第%d行数据错误：%s", rowIndex, String.join("; ", errors)));
        }
    }

    private Sales convertToEntity(SalesImportDTO dto) {
        Sales sales = new Sales();
        sales.setProductName(dto.getProductName());
        sales.setSalesRegion(dto.getSalesRegion());
        sales.setSalesDate(LocalDateTime.ofInstant(
            dto.getSalesDate().toInstant(), ZoneId.systemDefault()));
        sales.setSalesQuantity(dto.getSalesQuantity());
        sales.setUnitPrice(dto.getUnitPrice());
        sales.setTotalAmount(dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getSalesQuantity())));
        return sales;
    }

    private void saveData() {
        try {
            salesService.saveAll(salesList);
            salesList.clear();
        } catch (Exception e) {
            log.error("批量保存数据失败", e);
            throw new RuntimeException("保存数据失败: " + e.getMessage());
        }
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public int getTotalRows() {
        return totalRows.get();
    }

    public int getSuccessRows() {
        return successRows.get();
    }

    public String getImportId() {
        return importId;
    }
}
