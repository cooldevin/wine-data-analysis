CREATE TABLE import_status (
    import_id VARCHAR(36) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    total_rows INT,
    success_rows INT,
    error_messages TEXT,
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入状态记录表';
