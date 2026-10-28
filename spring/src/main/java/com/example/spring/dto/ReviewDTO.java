package com.example.spring.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 批阅请求
 */
@Data
public class ReviewDTO {

    @NotNull(message = "提交ID不能为空")
    private Long submissionId;

    @NotNull(message = "评分不能为空")
    @DecimalMin(value = "0", message = "评分超出范围（0~100）")
    @DecimalMax(value = "100", message = "评分超出范围（0~100）")
    private BigDecimal score;

    @Size(max = 500, message = "评语不超过500字")
    private String comment;
}
