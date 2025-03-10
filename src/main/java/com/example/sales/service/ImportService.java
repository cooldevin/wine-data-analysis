package com.example.sales.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.example.sales.dto.SalesImportDTO;
import com.example.sales.entity.ImportStatus;
import com.example.sales.entity.Sales;
import com.example.sales.repository.ImportStatusRepository;
import com.example.sales.service.excel.SalesDataListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportService {
    private final JdbcTemplate jdbcTemplate;
    private final ImportStatusRepository importStatusRepository;
    private final SalesService salesService;
    
    private static final int BATCH_SIZE = 1000;
    private static final String INSERT_SQL = 
        "INSERT INTO sales (product_name, sales_region, sales_date, " +
        "sales_quantity, unit_price, total_amount, import_id, import_batch, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String[] CSV_HEADERS = {"产品名称", "销售区域", "销售日期", "销售数量", "销售单价"};

    @Transactional
    public void executeBatchInsert(List<Object[]> batchData) {
        jdbcTemplate.batchUpdate(INSERT_SQL, batchData);
    }

    protected void updateImportStatus(String importId, int totalRows, int successRows, List<String> errorMessages) {
        ImportStatus status = importStatusRepository.findById(importId)
            .orElseThrow(() -> new IllegalArgumentException("导入ID不存在: " + importId));
        status.setEndTime(LocalDateTime.now());
        status.setTotalRows(totalRows);
        status.setSuccessRows(successRows);
        status.setStatus(errorMessages.isEmpty() ? "completed" : "error");
        status.setErrorMessages(String.join("\n", errorMessages));
        importStatusRepository.save(status);
    }

    protected void validateData(SalesImportDTO data, int rowIndex) {
        List<String> errors = new ArrayList<>();

        if (data.getProductName() == null || data.getProductName().trim().isEmpty()) {
            errors.add("产品名称不能为空");
        }

        if (data.getSalesRegion() == null || data.getSalesRegion().trim().isEmpty()) {
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

    protected Object[] convertToRowData(SalesImportDTO data, String importId, String importBatch) {
        BigDecimal totalAmount = data.getUnitPrice().multiply(BigDecimal.valueOf(data.getSalesQuantity()));
        return new Object[] {
            data.getProductName(),
            data.getSalesRegion(),
            new java.sql.Timestamp(data.getSalesDate().getTime()),
            data.getSalesQuantity(),
            data.getUnitPrice(),
            totalAmount,
            importId,
            importBatch,
            new java.sql.Timestamp(System.currentTimeMillis())
        };
    }

    public byte[] generateImportTemplate() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ExcelWriter writer = EasyExcel.write(outputStream).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("销售数据")
                    .head(Arrays.asList(
                        new ArrayList<>(Arrays.asList("产品名称")),
                        new ArrayList<>(Arrays.asList("销售区域")),
                        new ArrayList<>(Arrays.asList("销售日期")),
                        new ArrayList<>(Arrays.asList("销售数量")),
                        new ArrayList<>(Arrays.asList("销售单价"))
                    ))
                    .build();
            
            // 添加示例数据
            List<List<Object>> dataList = new ArrayList<>();
            dataList.add(Arrays.asList("茶叶", "华南区", "2024-01-01", "100", "15.5"));
            dataList.add(Arrays.asList("咖啡", "华东区", "2024-01-02", "50", "25.0"));
            
            writer.write(dataList, writeSheet);
            writer.finish();
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("生成导入模板失败", e);
            throw new RuntimeException("生成导入模板失败", e);
        }
    }

    public List<ImportStatus> getRecentImports() {
        return importStatusRepository.findTop10ByOrderByStartTimeDesc();
    }

    public Optional<ImportStatus> getImportStatus(String importId) {
        return importStatusRepository.findById(importId);
    }

    @Async("fileImportExecutor")
    public CompletableFuture<Map<String, Object>> importFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String importId = UUID.randomUUID().toString();
        String importBatch = LocalDateTime.now().toString();
        Map<String, Object> result = new HashMap<>();
        
        // 创建导入状态记录
        ImportStatus importStatus = new ImportStatus();
        importStatus.setImportId(importId);
        importStatus.setFileName(originalFilename);
        importStatus.setStatus("processing");
        importStatus.setStartTime(LocalDateTime.now());
        importStatusRepository.save(importStatus);
        
        try {
            if (originalFilename == null) {
                throw new IllegalArgumentException("文件名不能为空");
            }

            if (originalFilename.endsWith(".xlsx") || originalFilename.endsWith(".xls")) {
                handleExcelFile(file, importId, importBatch, result);
            } else if (originalFilename.endsWith(".csv")) {
                handleCsvFile(file, importId, importBatch, result);
            } else {
                throw new IllegalArgumentException("不支持的文件类型，仅支持.xlsx、.xls和.csv文件");
            }
            
            result.put("status", "success");
            result.put("importId", importId);
            
        } catch (Exception e) {
            log.error("导入文件失败", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        
        return CompletableFuture.completedFuture(result);
    }

    private void handleExcelFile(MultipartFile file, String importId, String importBatch, Map<String, Object> result) throws Exception {
        SalesDataListener listener = new SalesDataListener(
            // 处理每一行数据的回调
            (data, rowIndex) -> {
                // 数据处理已经在监听器中完成
                log.debug("处理第{}行数据", rowIndex);
            },
            // 完成回调
            () -> {
                log.info("文件处理完成");
            },
            salesService,
            importId
        );

        EasyExcel.read(file.getInputStream(), SalesImportDTO.class, listener).sheet().doRead();

        // 更新导入状态
        updateImportStatus(importId, listener.getTotalRows(), listener.getSuccessRows(), listener.getErrorMessages());

        // 设置结果信息
        result.put("totalRows", listener.getTotalRows());
        result.put("successRows", listener.getSuccessRows());
        result.put("errorMessages", listener.getErrorMessages());
    }

    private void handleCsvFile(MultipartFile file, String importId, String importBatch, Map<String, Object> result) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withHeader(CSV_HEADERS)
                     .withFirstRecordAsHeader()
                     .withTrim())) {

            List<String> errorMessages = new ArrayList<>();
            int totalRows = 0;
            int successRows = 0;
            List<SalesImportDTO> dataList = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                totalRows++;
                try {
                    SalesImportDTO dto = parseCSVRecord(record);
                    dataList.add(dto);
                    
                    if (dataList.size() >= 1000) {
                        successRows += processBatch(dataList);
                        dataList.clear();
                    }
                } catch (Exception e) {
                    String error = String.format("第%d行数据错误: %s", record.getRecordNumber(), e.getMessage());
                    errorMessages.add(error);
                    log.error(error, e);
                }
            }

            if (!dataList.isEmpty()) {
                successRows += processBatch(dataList);
            }

            result.put("totalRows", totalRows);
            result.put("successRows", successRows);
            result.put("errorMessages", errorMessages);
        }
    }

    private SalesImportDTO parseCSVRecord(CSVRecord record) throws Exception {
        SalesImportDTO dto = new SalesImportDTO();
        dto.setProductName(record.get("产品名称"));
        dto.setSalesRegion(record.get("销售区域"));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dto.setSalesDate(dateFormat.parse(record.get("销售日期")));
        
        dto.setSalesQuantity(Integer.parseInt(record.get("销售数量")));
        dto.setUnitPrice(new BigDecimal(record.get("销售单价")));
        
        return dto;
    }

    private int processBatch(List<SalesImportDTO> dataList) {
        int successCount = 0;
        for (SalesImportDTO dto : dataList) {
            try {
                salesService.saveSalesFromImport(dto);
                successCount++;
            } catch (Exception e) {
                log.error("保存数据失败", e);
            }
        }
        return successCount;
    }
}
