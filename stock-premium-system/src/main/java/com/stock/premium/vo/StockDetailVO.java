package com.stock.premium.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 股票详细信息VO
 * 
 * @author system
 * @since 2024-01-01
 */
@Data
@ApiModel(value = "股票详细信息", description = "包含股票基本信息、价格和溢价率的详细数据")
public class StockDetailVO {

    @ApiModelProperty(value = "股票代码", example = "600036", required = true)
    private String stockCode;

    @ApiModelProperty(value = "股票中文名称", example = "招商银行", required = true)
    private String stockName;

    @ApiModelProperty(value = "A股最新价格", example = "44.83", notes = "从stock_price_record表获取的最新A股价格")
    private BigDecimal aStockPrice;

    @ApiModelProperty(value = "H股最新价格", example = "52.65", notes = "从stock_price_record表获取的最新H股价格")
    private BigDecimal hStockPrice;

    @ApiModelProperty(value = "溢价率(%)", example = "7.0264", notes = "从premium_rate_record表获取的最新溢价率")
    private BigDecimal premiumRate;

    @ApiModelProperty(value = "A股代码", example = "600036")
    private String aStockCode;

    @ApiModelProperty(value = "H股代码", example = "03968")
    private String hStockCode;

    @ApiModelProperty(value = "汇率", example = "0.9113", notes = "港币对人民币汇率")
    private BigDecimal exchangeRate;
}
