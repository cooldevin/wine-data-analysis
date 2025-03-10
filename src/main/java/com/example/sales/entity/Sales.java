package com.example.sales.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales", indexes = {
    @Index(name = "idx_sales_date", columnList = "sales_date"),
    @Index(name = "idx_product_name", columnList = "product_name"),
    @Index(name = "idx_import_id", columnList = "import_id")
})
public class Sales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sales_date", nullable = false)
    private LocalDateTime salesDate;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "sales_region", nullable = false, length = 50)
    private String salesRegion;

    @Column(name = "sales_quantity", nullable = false)
    private Integer salesQuantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @Column(name = "import_id", length = 36)
    private String importId;

    @Column(name = "import_batch", length = 50)
    private String importBatch;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getSalesDate() {
        return salesDate;
    }

    public void setSalesDate(LocalDateTime salesDate) {
        this.salesDate = salesDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSalesRegion() {
        return salesRegion;
    }

    public void setSalesRegion(String salesRegion) {
        this.salesRegion = salesRegion;
    }

    public Integer getSalesQuantity() {
        return salesQuantity;
    }

    public void setSalesQuantity(Integer salesQuantity) {
        this.salesQuantity = salesQuantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public String getImportId() {
        return importId;
    }

    public void setImportId(String importId) {
        this.importId = importId;
    }

    public String getImportBatch() {
        return importBatch;
    }

    public void setImportBatch(String importBatch) {
        this.importBatch = importBatch;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
