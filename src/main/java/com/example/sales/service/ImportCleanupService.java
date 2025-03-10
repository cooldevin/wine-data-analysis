package com.example.sales.service;

import com.example.sales.entity.ImportStatus;
import com.example.sales.repository.ImportStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportCleanupService {
    private final ImportStatusRepository importStatusRepository;

    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    @Transactional
    public void cleanupStuckImports() {
        List<ImportStatus> stuckImports = importStatusRepository.findStuckImports();
        if (!stuckImports.isEmpty()) {
            log.info("发现 {} 个卡住的导入任务", stuckImports.size());
            
            for (ImportStatus status : stuckImports) {
                status.setStatus("error");
                status.setErrorMessages(status.getErrorMessages() + "\n系统检测到该导入任务已超时，自动标记为失败");
                importStatusRepository.save(status);
                log.info("已将导入任务 {} 标记为失败", status.getImportId());
            }
        }
    }
}
