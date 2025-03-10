package com.example.sales.controller;

import com.example.sales.service.SalesService;
import com.example.sales.dto.SalesOverviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;

@Controller
@RequestMapping("/sales/overview")
public class SalesOverviewController {

    private final SalesService salesService;

    @Autowired
    public SalesOverviewController(SalesService salesService) {
        this.salesService = salesService;
    }

    @GetMapping("/dashboard")
    @ResponseBody
    public ResponseEntity<SalesOverviewDTO> getDashboardData(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        SalesOverviewDTO overview = salesService.getSalesOverview(startDate, endDate);
        return ResponseEntity.ok(overview);
    }


}
