-- 添加导入相关的字段
ALTER TABLE sales
    ADD COLUMN import_id VARCHAR(36) NULL COMMENT '导入批次ID',
    ADD COLUMN import_batch VARCHAR(50) NULL COMMENT '导入批次号',
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    ADD INDEX idx_import_id (import_id);
