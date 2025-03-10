package com.example.sales.controller;

import com.example.sales.dto.RegionSalesDTO;
import com.example.sales.entity.Sales;
import com.example.sales.service.SalesService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/data/storage")

public class SalesDataController {

    private final SalesService salesService;

    @Autowired
    public SalesDataController(SalesService salesService) {
        this.salesService = salesService;
    }

    

    @GetMapping("/list")
    @ResponseBody
    @RequiresRoles("USER")
    public ResponseEntity<Page<Sales>> listSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String salesRegion
    ) {

        PageRequest pageRequest = PageRequest.of(page, size);
        try {
            LocalDateTime start = null;
            LocalDateTime end = null;
            if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
                start = LocalDateTime.parse(startDate );
                end = LocalDateTime.parse(endDate);
            }
            if (StringUtils.isBlank(productName) && StringUtils.isBlank(startDate) &&
                    StringUtils.isBlank(endDate) && StringUtils.isBlank(salesRegion)) {
                return ResponseEntity.ok(salesService.findAll(pageRequest));
            }
            Page<Sales> salesPage = salesService.findByCondition(productName, salesRegion, start, end, pageRequest);
            return ResponseEntity.ok(salesPage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/byRegion")
    public ResponseEntity<List<RegionSalesDTO>> getSalesByRegion() {
        List<RegionSalesDTO> salesData = salesService.getSalesByRegion();
        return ResponseEntity.ok(salesData);
    }

    @PostMapping
    @ResponseBody
    @RequiresRoles("ADMIN")
    public ResponseEntity<Sales> createSales(@RequestBody Sales sales) {
        sales.setCreatedAt(LocalDateTime.now());
        Sales savedSales = salesService.save(sales);
        return ResponseEntity.ok(savedSales);
    }

    @PutMapping("/{id}")
    @ResponseBody
    @RequiresRoles("USER")
    public ResponseEntity<Sales> updateSales(
            @PathVariable Long id,
            @RequestBody Sales sales) {
        sales.setId(id);
        Sales updatedSales = salesService.update(sales);
        return ResponseEntity.ok(updatedSales);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @RequiresRoles("USER")
    public ResponseEntity<Void> deleteSales(@PathVariable Long id) {
        salesService.delete(id);
        return ResponseEntity.ok().build();
    }
}
