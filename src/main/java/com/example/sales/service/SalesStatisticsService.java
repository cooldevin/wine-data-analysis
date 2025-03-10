package com.example.sales.service;

import com.example.sales.dto.RegionalSalesStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SalesStatisticsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<RegionalSalesStats> getRegionalSalesStats(String region, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT region, " +
            "       SUM(amount) as total_amount, " +
            "       COUNT(*) as order_count " +
            "FROM sales_orders " +
            "WHERE order_date BETWEEN ? AND ? "
        );

        if (region != null && !region.isEmpty()) {
            sql.append("AND region = ? ");
        }

        sql.append("GROUP BY region ORDER BY total_amount DESC");

        if (region != null && !region.isEmpty()) {
            return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> new RegionalSalesStats(
                    rs.getString("region"),
                    rs.getDouble("total_amount"),
                    rs.getLong("order_count")
                ),
                startDate, endDate, region
            );
        } else {
            return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> new RegionalSalesStats(
                    rs.getString("region"),
                    rs.getDouble("total_amount"),
                    rs.getLong("order_count")
                ),
                startDate, endDate
            );
        }
    }

    public List<String> getAllRegions() {
        return jdbcTemplate.queryForList(
            "SELECT DISTINCT region FROM sales_orders ORDER BY region",
            String.class
        );
    }
}
