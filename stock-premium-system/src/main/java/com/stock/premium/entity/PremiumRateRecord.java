package com.stock.premium.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 溢价率记录实体类
 * 
 * @author system
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("premium_rate_record")
public class PremiumRateRecord {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 股票代码（A股代码）
     */
    @TableField("stock_code")
    private String stockCode;

    /**
     * A股价格
     */
    @TableField("a_stock_price")
    private BigDecimal aStockPrice;

    /**
     * H股价格
     */
    @TableField("h_stock_price")
    private BigDecimal hStockPrice;

    /**
     * 汇率
     */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;

    /**
     * 溢价率(%)
     */
    @TableField("premium_rate")
    private BigDecimal premiumRate;

    /**
     * 记录时间
     */
    @TableField("record_time")
    private LocalDateTime recordTime;

    /**
     * 交易日期
     */
    @TableField("trade_date")
    private LocalDate tradeDate;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}