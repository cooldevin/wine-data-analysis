package com.example.sales.repository;

import com.example.sales.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
}
