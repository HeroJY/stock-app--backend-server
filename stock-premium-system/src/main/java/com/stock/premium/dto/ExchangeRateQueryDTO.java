package com.stock.premium.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * 汇率查询DTO
 * 
 * @author system
 * @since 2024-01-01
 */
@Data
public class ExchangeRateQueryDTO {
    
    /**
     * 货币对
     */
    @NotBlank(message = "货币对不能为空")
    private String currencyPair = "HKDCNY";
    
    /**
     * 开始日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 20;
}