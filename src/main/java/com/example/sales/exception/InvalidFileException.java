package com.example.sales.exception;

public class InvalidFileException extends RuntimeException {
    private final String fileName;
    private final String reason;

    public InvalidFileException(String fileName, String reason) {
        super(String.format("Invalid file '%s': %s", fileName, reason));
        this.fileName = fileName;
        this.reason = reason;
    }

    public String getFileName() {
        return fileName;
    }

    public String getReason() {
        return reason;
    }
}
