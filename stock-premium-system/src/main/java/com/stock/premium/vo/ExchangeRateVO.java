package com.stock.premium.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 汇率返回VO
 * 
 * @author system
 * @since 2024-01-01
 */
@Data
public class ExchangeRateVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 货币对
     */
    private String currencyPair;
    
    /**
     * 汇率
     */
    private BigDecimal rate;
    
    /**
     * 记录时间
     */
    private LocalDateTime recordTime;
    
    /**
     * 交易日期
     */
    private LocalDate tradeDate;
    
    /**
     * 数据来源
     */
    private String dataSource;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    // 统计字段
    /**
     * 最高汇率
     */
    private BigDecimal maxRate;
    
    /**
     * 最低汇率
     */
    private BigDecimal minRate;
    
    /**
     * 平均汇率
     */
    private BigDecimal avgRate;
    
    /**
     * 开盘汇率
     */
    private BigDecimal openRate;
    
    /**
     * 收盘汇率
     */
    private BigDecimal closeRate;
}