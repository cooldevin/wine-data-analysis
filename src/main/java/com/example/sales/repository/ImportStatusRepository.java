package com.example.sales.repository;

import com.example.sales.entity.ImportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportStatusRepository extends JpaRepository<ImportStatus, String> {
    List<ImportStatus> findTop10ByOrderByStartTimeDesc();

    @Query("SELECT i FROM ImportStatus i WHERE i.status = 'processing' AND i.startTime < :#{T(java.time.LocalDateTime).now().minusHours(1)}")
    List<ImportStatus> findStuckImports();
}
