package com.example.sales.repository;

import com.example.sales.dto.RegionalSalesStats;
import com.example.sales.entity.Sales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface SalesRepository extends JpaRepository<Sales, Long>, JpaSpecificationExecutor<Sales> {
    
    // 分页查询所有销售记录
    Page<Sales> findAll(Pageable pageable);
    
    // 按产品名称分页查询
    Page<Sales> findByProductNameContainingAndSalesRegion(String productName, String salesRegion,Pageable pageable);
    
    // 按日期范围分页查询
    Page<Sales> findBySalesDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Sales> findByProductNameAndCreatedAtBetweenAndSalesRegion(String productName, LocalDateTime createdAtAfter, LocalDateTime createdAtBefore, String salesRegion, Pageable pageable);
    
    // 统计特定日期范围内的销售总额
    @Query("SELECT SUM(s.unitPrice * s.salesQuantity) FROM Sales s WHERE s.salesDate BETWEEN :startDate AND :endDate")
    Double calculateTotalSalesAmount(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Sales s " +
            "WHERE (:productName IS NULL OR s.productName LIKE %:productName%) " +
            "AND (:salesRegion IS NULL OR s.salesRegion = :salesRegion) " +
            "AND (:startDate IS NULL OR s.salesDate >= :startDate) " +
            "AND (:endDate IS NULL OR s.salesDate <= :endDate)")
    Page<Sales> findByCondition(
            @Param("productName") String productName,
            @Param("salesRegion") String salesRegion,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);



    interface ProductSalesStats {
        String getProductName();
        Long getCount();
        Double getTotal();
    }

    // 获取销量前N的产品
    @Query(value = "SELECT s.product_name as productName, COUNT(*) as count, SUM(s.total_amount) as total " +
           "FROM sales s GROUP BY s.product_name ORDER BY count DESC",
           countQuery = "SELECT COUNT(DISTINCT s.product_name) FROM sales s",
           nativeQuery = true)
    Page<ProductSalesStats> findTopSellingProducts(Pageable pageable);
    
    // 按地区统计销售额
    @Query("SELECT s.salesRegion, SUM(s.totalAmount) " +
           "FROM Sales s GROUP BY s.salesRegion ORDER BY SUM(s.totalAmount) DESC")
    List<Object[]> findSalesByRegion();
    
    @Query(value = "SELECT s.sales_region as groupKey, SUM(s.sales_quantity * s.unit_price) as totalAmount " +
           "FROM sales s GROUP BY s.sales_region ORDER BY totalAmount DESC LIMIT 5", nativeQuery = true)
    List<Object[]> getSalesByRegion();
    
    @Query(value = "SELECT DATE_FORMAT(s.sales_date, '%Y-%m') as groupKey, " +
           "SUM(s.sales_quantity * s.unit_price) as totalAmount " +
           "FROM sales s GROUP BY DATE_FORMAT(s.sales_date, '%Y-%m') " +
           "ORDER BY groupKey DESC LIMIT 12", nativeQuery = true)
    List<Object[]> getSalesByMonth();
    
    @Query("SELECT CONCAT(YEAR(s.salesDate), '-', MONTH(s.salesDate)) as month, " +
           "SUM(s.totalAmount) FROM Sales s " +
           "GROUP BY YEAR(s.salesDate), MONTH(s.salesDate) " +
           "ORDER BY YEAR(s.salesDate) DESC, MONTH(s.salesDate) DESC")
    List<Object[]> findSalesByMonth();
    
    // 获取所有销售区域
    @Query("SELECT DISTINCT s.salesRegion FROM Sales s ORDER BY s.salesRegion")
    List<String> findDistinctRegions();

    // 按指定区域统计销售数据
    @Query("SELECT s.salesRegion, SUM(s.totalAmount), COUNT(s) " +
           "FROM Sales s " +
           "WHERE s.salesRegion = :region " +
           "AND s.salesDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.salesRegion")
    List<Object[]> findRegionStatisticsForRegion(
        @Param("region") String region,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // 统计所有区域销售数据
    @Query("SELECT s.salesRegion, SUM(s.totalAmount), COUNT(s) " +
           "FROM Sales s " +
           "WHERE s.salesDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.salesRegion " +
           "ORDER BY SUM(s.totalAmount) DESC")
    List<Object[]> findRegionStatisticsForAllRegions(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    Long countBySalesDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s.productName, SUM(s.salesQuantity), SUM(s.totalAmount) " +
           "FROM Sales s " +
           "WHERE s.salesDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.productName " +
           "ORDER BY SUM(s.totalAmount) DESC")
    List<Object[]> findTopSellingProductsWithRevenue(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT DATE_FORMAT(s.sales_date, '%Y-%m-%d') as date, SUM(s.total_amount) " +
           "FROM sales s " +
           "WHERE s.sales_date BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE_FORMAT(s.sales_date, '%Y-%m-%d') " +
           "ORDER BY date", nativeQuery = true)
    List<Object[]> findSalesTrendByDay(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // 导入相关的方法
    Long countByImportId(String importId);

    @Query("SELECT COUNT(s) FROM Sales s WHERE s.importId = :importId AND s.importBatch = :importBatch")
    Long countByImportIdAndImportBatch(
        @Param("importId") String importId,
        @Param("importBatch") String importBatch);

    @Query(value = "INSERT INTO sales (product_name, sales_region, sales_date, sales_quantity, unit_price, total_amount, import_id, import_batch, created_at) " +
           "VALUES (:#{#sales.productName}, :#{#sales.salesRegion}, :#{#sales.salesDate}, :#{#sales.salesQuantity}, " +
           ":#{#sales.unitPrice}, :#{#sales.totalAmount}, :#{#sales.importId}, :#{#sales.importBatch}, :#{#sales.createdAt})",
           nativeQuery = true)
    void batchInsert(@Param("sales") Sales sales);

    // 获取指定日期范围内的销售数据，用于数据可视化
    @Query(value = "SELECT DATE_FORMAT(s.sales_date, '%Y-%m-%d') as date, " +
           "s.product_name as product, " +
           "SUM(s.sales_quantity) as sales, " +
           "SUM(s.total_amount) as revenue, " +
           "SUM(s.total_amount - (s.unit_price * s.sales_quantity * 0.7)) as profit " +
           "FROM sales s " +
           "WHERE s.sales_date BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE_FORMAT(s.sales_date, '%Y-%m-%d'), s.product_name " +
           "ORDER BY date ASC, product ASC", nativeQuery = true)
    List<Map<String, Object>> findSalesDataByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
}
