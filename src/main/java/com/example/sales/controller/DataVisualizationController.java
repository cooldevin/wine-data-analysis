package com.example.sales.controller;

import com.example.sales.service.DataVisualizationService;
import com.example.sales.dto.VisualizationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data/visualization")
public class DataVisualizationController {

    private final DataVisualizationService visualizationService;

    @Autowired
    public DataVisualizationController(DataVisualizationService visualizationService) {
        this.visualizationService = visualizationService;
    }

    @GetMapping
    public ResponseEntity<VisualizationData> getVisualizationData(
            @RequestParam String timeRange,
            @RequestParam String dimension) {
        VisualizationData data = visualizationService.getVisualizationData(timeRange, dimension);
        return ResponseEntity.ok(data);
    }
}
