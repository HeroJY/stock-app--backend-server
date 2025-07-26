package com.stock.premium.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日统计数据实体类
 * 
 * @author system
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("daily_premium_stats")
public class DailyPremiumStats {

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
     * 交易日期
     */
    @TableField("trade_date")
    private LocalDate tradeDate;

    /**
     * 开盘溢价率(%)
     */
    @TableField("open_premium_rate")
    private BigDecimal openPremiumRate;

    /**
     * 收盘溢价率(%)
     */
    @TableField("close_premium_rate")
    private BigDecimal closePremiumRate;

    /**
     * 最高溢价率(%)
     */
    @TableField("max_premium_rate")
    private BigDecimal maxPremiumRate;

    /**
     * 最低溢价率(%)
     */
    @TableField("min_premium_rate")
    private BigDecimal minPremiumRate;

    /**
     * 平均溢价率(%)
     */
    @TableField("avg_premium_rate")
    private BigDecimal avgPremiumRate;

    /**
     * 95%分位数
     */
    @TableField("percentile_95")
    private BigDecimal percentile95;

    /**
     * 5%分位数
     */
    @TableField("percentile_5")
    private BigDecimal percentile5;

    /**
     * 记录数量
     */
    @TableField("record_count")
    private Integer recordCount;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}