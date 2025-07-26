package com.stock.premium.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 汇率记录实体类
 * 
 * @author system
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("exchange_rate_record")
public class ExchangeRateRecord {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 货币对：HKDCNY
     */
    @TableField("currency_pair")
    private String currencyPair;

    /**
     * 汇率
     */
    @TableField("rate")
    private BigDecimal rate;

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
     * 数据来源
     */
    @TableField("data_source")
    private String dataSource;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}