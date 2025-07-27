package com.stock.premium.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 汇率简单信息VO
 * 
 * @author system
 * @since 2024-01-01
 */
@Data
@ApiModel("汇率简单信息")
public class ExchangeRateSimpleVO {

    @ApiModelProperty("货币对")
    private String currencyPair;

    @ApiModelProperty("汇率")
    private BigDecimal rate;

    @ApiModelProperty("记录时间")
    private LocalDateTime recordTime;

    @ApiModelProperty("交易日期")
    private LocalDate tradeDate;

    @ApiModelProperty("数据来源")
    private String dataSource;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;
}