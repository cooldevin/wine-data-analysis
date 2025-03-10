package com.example.sales.controller;

import com.example.sales.annotation.Loggable;
import org.apache.shiro.authz.annotation.RequiresRoles;
import com.example.sales.dto.ImportResult;
import com.example.sales.service.FileImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Controller
@RequestMapping("/import")
public class FileImportController {

    @Autowired
    private FileImportService fileImportService;

    

    @PostMapping("/upload")
    @ResponseBody
    @RequiresRoles("USER")
    @Loggable(operation = "文件导入")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择文件");
        }

        if (!fileImportService.isValidFileType(file.getOriginalFilename())) {
            return ResponseEntity.badRequest().body("仅支持.xlsx和.csv文件");
        }

        String importId = UUID.randomUUID().toString();
        fileImportService.importFile(file, importId);
        return ResponseEntity.ok().body(importId);
    }

    @GetMapping("/status/{importId}")
    @ResponseBody
    @RequiresRoles("USER")
    public ResponseEntity<ImportResult> getImportStatus(@PathVariable String importId) {
        ImportResult result = fileImportService.getImportResult(importId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
