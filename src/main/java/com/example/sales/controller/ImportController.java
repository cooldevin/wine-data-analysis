package com.example.sales.controller;

import com.example.sales.entity.ImportStatus;
import com.example.sales.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/sales/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] template = importService.generateImportTemplate();
        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=sales_import_template.xlsx")
                .body(template);
    }



    @PostMapping("/upload")
    @ResponseBody
    public CompletableFuture<ResponseEntity<Map<String, Object>>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        return importService.importFile(file)
                .thenApply(result -> {
                    if ("success".equals(result.get("status"))) {
                        return ResponseEntity.ok(result);
                    } else {
                        return ResponseEntity.badRequest().body(result);
                    }
                });
    }

    @GetMapping("/status/{importId}")
    @ResponseBody
    public ResponseEntity<ImportStatus> getImportStatus(@PathVariable String importId) {
        Optional<ImportStatus> status = importService.getImportStatus(importId);
        return status.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
