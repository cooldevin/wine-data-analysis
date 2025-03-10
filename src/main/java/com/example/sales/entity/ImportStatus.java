package com.example.sales.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "import_status")
public class ImportStatus {
    @Id
    @Column(length = 36)
    private String importId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String status;  // processing, completed, error

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column
    private Integer totalRows;

    @Column
    private Integer successRows;

    @Column(columnDefinition = "TEXT")
    private String errorMessages;
}
