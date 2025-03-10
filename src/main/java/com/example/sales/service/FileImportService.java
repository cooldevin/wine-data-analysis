package com.example.sales.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.sales.aspect.LoggingAspect;
import com.example.sales.dto.ImportResult;
import com.example.sales.entity.Sales;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileImportService {
    
    @Autowired
    private SalesService salesService;
    
    private static final int BATCH_SIZE = 1000;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Map<String, ImportResult> importResults = new ConcurrentHashMap<>();
    @Autowired
    private LoggingAspect loggingAspect;

    public boolean isValidFileType(String fileName) {
        if (fileName == null) {
            return false;
        }
        return fileName.endsWith(".xlsx") || fileName.endsWith(".csv");
    }
    
    @Async("fileImportExecutor")
    public void importFile(MultipartFile file, String importId) {
        ImportResult result = new ImportResult();
        importResults.put(importId, result);
        
        try {
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<String, String>>() {

                private final List<Sales> salesBatch = new ArrayList<>();
                private int totalRows = 0;
                private int headerRow = 0;
                
                @Override
                public void invoke(Map<String, String> data, AnalysisContext context) {
                    // 跳过表头行
                    if (headerRow == 0) {
                        headerRow++;
                        return;
                    }
                    
                    totalRows++;
                    try {
                        Sales sales = convertToSales(data);
                        if (sales == null) {
                            System.out.println("第 " + totalRows + " 行数据格式错误");
                            result.addError("第 " + totalRows + " 行数据格式错误");
                        }
                        if (sales != null) {
                            salesBatch.add(sales);
                            result.setSuccessRows(result.getSuccessRows() + 1);
                        }
                        
                        if (salesBatch.size() >= BATCH_SIZE) {
                            saveBatch();
                        }
                    } catch (Exception e) {
                        result.addError("第 " + totalRows + " 行数据格式错误: " + e.getMessage());
                    }
                }
                
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    if (!salesBatch.isEmpty()) {
                        saveBatch();
                    }
                    result.setTotalRows(totalRows);
                    result.setStatus("completed");
                }
                
                private void saveBatch() {
                    try {
                        salesService.saveAll(salesBatch);
                        salesBatch.clear();
                    } catch (Exception e) {
                        result.addError("批量保存数据失败: " + e.getMessage());
                    }
                }
            }).sheet().doRead();
            
        } catch (IOException e) {
            result.addError("文件读取失败: " + e.getMessage());
            result.setStatus("failed");
        }
    }
    
    private Sales convertToSales(Map<String, String> data) {
        if (isEmptyRow(data)) {
            return null;
        }
        
        Sales sales = new Sales();
        
        // 产品名称验证和转换
        String productName = data.get("产品名称");
        if (StringUtils.isBlank(productName)) {
            throw new IllegalArgumentException("产品名称不能为空");
        }
        sales.setProductName(productName.trim());
        
        // 销售区域验证和转换
        String region = data.get("销售区域");
        if (StringUtils.isBlank(region)) {
            throw new IllegalArgumentException("销售区域不能为空");
        }
        sales.setSalesRegion(region.trim());
        
        // 销售日期验证和转换
        String salesDate = data.get("销售日期");
        if (StringUtils.isBlank(salesDate)) {
            throw new IllegalArgumentException("销售日期不能为空");
        }
        try {
            sales.setSalesDate(LocalDateTime.parse(salesDate.trim(), DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("销售日期格式错误，正确格式为：yyyy-MM-dd HH:mm:ss");
        }
        
        // 销售数量验证和转换
        String quantity = data.get("销售数量");
        if (StringUtils.isBlank(quantity)) {
            throw new IllegalArgumentException("销售数量不能为空");
        }
        try {
            int qty = Integer.parseInt(quantity.trim());
            if (qty <= 0) {
                throw new IllegalArgumentException("销售数量必须大于0");
            }
            sales.setSalesQuantity(qty);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("销售数量必须为整数");
        }
        
        // 销售单价验证和转换
        String unitPrice = data.get("销售单价");
        if (StringUtils.isBlank(unitPrice)) {
            throw new IllegalArgumentException("销售单价不能为空");
        }
        try {
            BigDecimal price = new BigDecimal(unitPrice.trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("销售单价必须大于0");
            }
            sales.setUnitPrice(price);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("销售单价格式错误");
        }
        
        // 计算总金额
        sales.setTotalAmount(sales.getUnitPrice().multiply(BigDecimal.valueOf(sales.getSalesQuantity())));
        
        return sales;
    }
    
    private boolean isEmptyRow(Map<String, String> data) {
        return data.values().stream().allMatch(StringUtils::isBlank);
    }
    
    public ImportResult getImportResult(String importId) {
        return importResults.get(importId);
    }
}
