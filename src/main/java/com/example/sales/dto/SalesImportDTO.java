package com.example.sales.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SalesImportDTO {
    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("销售区域")
    private String salesRegion;

    @ExcelProperty("销售日期")
    private Date salesDate;

    @ExcelProperty("销售数量")
    private Integer salesQuantity;

    @ExcelProperty("销售单价")
    private BigDecimal unitPrice;

    // 用于记录导入时的错误信息
    private String errorMessage;
    private Integer rowIndex;
}
