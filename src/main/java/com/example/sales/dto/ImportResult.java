package com.example.sales.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    private int totalRows;
    private int successRows;
    private List<String> errors;
    private String status;

    public ImportResult() {
        this.errors = new ArrayList<>();
        this.status = "processing";
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessRows() {
        return successRows;
    }

    public void setSuccessRows(int successRows) {
        this.successRows = successRows;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}
