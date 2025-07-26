package com.stock.premium.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实时价格记录实体类
 * 
 * @author system
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("stock_price_record")
public class StockPriceRecord {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 股票代码
     */
    @TableField("stock_code")
    private String stockCode;

    /**
     * 市场类型：A股/H股
     */
    @TableField("market_type")
    private String marketType;

    /**
     * 当前价格
     */
    @TableField("current_price")
    private BigDecimal currentPrice;

    /**
     * 开盘价
     */
    @TableField("open_price")
    private BigDecimal openPrice;

    /**
     * 最高价
     */
    @TableField("high_price")
    private BigDecimal highPrice;

    /**
     * 最低价
     */
    @TableField("low_price")
    private BigDecimal lowPrice;

    /**
     * 昨收价
     */
    @TableField("pre_close_price")
    private BigDecimal preClosePrice;

    /**
     * 成交量
     */
    @TableField("volume")
    private Long volume;

    /**
     * 成交额
     */
    @TableField("turnover")
    private BigDecimal turnover;

    /**
     * 涨跌幅(%)
     */
    @TableField("change_rate")
    private BigDecimal changeRate;

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