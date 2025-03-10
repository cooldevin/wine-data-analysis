package com.example.sales.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

public class SalesPredictionDTO {

    private Map<YearMonth, BigDecimal> historicalData;
    private Map<YearMonth, BigDecimal> predictions;
    private BigDecimal r2Score;

    public Map<YearMonth, BigDecimal> getHistoricalData() {
        return historicalData;
    }

    public void setHistoricalData(Map<YearMonth, BigDecimal> historicalData) {
        this.historicalData = historicalData;
    }

    public Map<YearMonth, BigDecimal> getPredictions() {
        return predictions;
    }

    public void setPredictions(Map<YearMonth, BigDecimal> predictions) {
        this.predictions = predictions;
    }

    public BigDecimal getR2Score() {
        return r2Score;
    }

    public void setR2Score(BigDecimal r2Score) {
        this.r2Score = r2Score;
    }
}
