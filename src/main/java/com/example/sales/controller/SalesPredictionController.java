package com.example.sales.controller;

import com.example.sales.dto.SalesPredictionDTO;
import com.example.sales.service.SalesPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/api/sales/prediction")
public class SalesPredictionController {

    private final SalesPredictionService predictionService;

    @Autowired
    public SalesPredictionController(SalesPredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping
    public ResponseEntity<SalesPredictionDTO> getPrediction(
            @RequestParam(defaultValue = "12") int months) {
        
        Map<YearMonth, BigDecimal> historicalData = predictionService.getHistoricalSalesData(months);
        SalesPredictionDTO prediction = predictionService.predictSales(historicalData);
        
        return ResponseEntity.ok(prediction);
    }
}
