package com.stock.premium.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 股票基础信息实体类
 * 
 * @author system
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("stock_info")
public class StockInfo {

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
     * 股票名称
     */
    @TableField("stock_name")
    private String stockName;

    /**
     * 市场类型：A股/H股
     */
    @TableField("market_type")
    private String marketType;

    /**
     * A股代码
     */
    @TableField("a_stock_code")
    private String aStockCode;

    /**
     * H股代码
     */
    @TableField("h_stock_code")
    private String hStockCode;

    /**
     * 交易所：SH/SZ/HK
     */
    @TableField("exchange")
    private String exchange;

    /**
     * 所属行业
     */
    @TableField("industry")
    private String industry;

    /**
     * 状态：1-正常，0-停用
     */
    @TableField("status")
    private Integer status;

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

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}