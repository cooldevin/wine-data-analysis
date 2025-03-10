package com.example.sales.repository;

import com.example.sales.dto.SalesQueryDTO;
import com.example.sales.entity.Sales;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class SalesSpecification {
    
    public static Specification<Sales> buildSpecification(SalesQueryDTO queryDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.isNotBlank(queryDTO.getProductName())) {
                predicates.add(criteriaBuilder.like(root.get("productName"), 
                    "%" + queryDTO.getProductName() + "%"));
            }
            
            if (StringUtils.isNotBlank(queryDTO.getSalesRegion())) {
                predicates.add(criteriaBuilder.equal(root.get("salesRegion"), 
                    queryDTO.getSalesRegion()));
            }
            
            if (queryDTO.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("salesDate"), queryDTO.getStartDate()));
            }
            
            if (queryDTO.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("salesDate"), queryDTO.getEndDate()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
