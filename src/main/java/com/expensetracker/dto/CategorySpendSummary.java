package com.expensetracker.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Data Transfer Object representing category-wise expense aggregation
 * for spending analytics and Chart.js visualization.
 */
public class CategorySpendSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private String categoryName;
    private BigDecimal totalAmount;
    private double percentage;

    public CategorySpendSummary() {
    }

    public CategorySpendSummary(String categoryName, BigDecimal totalAmount, double percentage) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.percentage = percentage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getFormattedPercentage() {
        return String.format("%.1f%%", percentage);
    }

    public String getFormattedAmount() {
        return totalAmount != null ? totalAmount.setScale(2, RoundingMode.HALF_UP).toString() : "0.00";
    }

    @Override
    public String toString() {
        return "CategorySpendSummary{" +
                "categoryName='" + categoryName + '\'' +
                ", totalAmount=" + totalAmount +
                ", percentage=" + percentage +
                '}';
    }
}
