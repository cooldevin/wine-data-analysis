package com.example.sales.service;

import com.example.sales.dto.SalesPredictionDTO;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class SalesPredictionService {

    public SalesPredictionDTO predictSales(Map<YearMonth, BigDecimal> historicalData) {
        // 将数据转换为时间序列（X为月份索引，Y为销售额）
        SimpleRegression regression = new SimpleRegression();
        TreeMap<YearMonth, BigDecimal> sortedData = new TreeMap<>(historicalData);
        
        int i = 0;
        for (Map.Entry<YearMonth, BigDecimal> entry : sortedData.entrySet()) {
            regression.addData(i++, entry.getValue().doubleValue());
        }

        // 计算R²值
        BigDecimal r2Score = BigDecimal.valueOf(regression.getRSquare())
            .setScale(4, RoundingMode.HALF_UP);

        // 预测未来3个月
        YearMonth lastMonth = sortedData.lastKey();
        Map<YearMonth, BigDecimal> predictions = new LinkedHashMap<>();
        
        for (int month = 1; month <= 3; month++) {
            YearMonth predictMonth = lastMonth.plusMonths(month);
            double prediction = regression.predict(i + month - 1);
            // 确保预测值不为负
            prediction = Math.max(0, prediction);
            predictions.put(predictMonth, 
                BigDecimal.valueOf(prediction).setScale(2, RoundingMode.HALF_UP));
        }

        // 构建返回结果
        SalesPredictionDTO result = new SalesPredictionDTO();
        result.setHistoricalData(sortedData);
        result.setPredictions(predictions);
        result.setR2Score(r2Score);

        return result;
    }

    public Map<YearMonth, BigDecimal> getHistoricalSalesData(int monthsToInclude) {
        // 这里应该从数据库获取实际的销售数据
        // 现在使用模拟数据进行演示
        Map<YearMonth, BigDecimal> data = new LinkedHashMap<>();
        YearMonth currentMonth = YearMonth.now();
        
        for (int i = monthsToInclude - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            // 生成一些示例数据，带有季节性趋势
            double baseValue = 10000 + (i * 500); // 基础增长趋势
            double seasonality = Math.sin(2 * Math.PI * (month.getMonthValue() / 12.0)) * 2000; // 季节性波动
            double randomNoise = Math.random() * 1000 - 500; // 随机波动
            
            BigDecimal value = BigDecimal.valueOf(baseValue + seasonality + randomNoise)
                .setScale(2, RoundingMode.HALF_UP);
            data.put(month, value);
        }
        
        return data;
    }
}
