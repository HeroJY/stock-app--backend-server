package com.stock.premium.controller;

import com.stock.premium.entity.PremiumRateRecord;
import com.stock.premium.entity.StockPriceRecord;
import com.stock.premium.service.DataCollectionService;
import com.stock.premium.service.PremiumRateService;
import com.stock.premium.service.TencentFinanceService;
import com.stock.premium.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据采集控制器
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Api(tags = "数据采集接口")
@RestController
@RequestMapping("/data")
public class DataCollectionController {

    @Autowired
    private TencentFinanceService tencentFinanceService;

    @Autowired
    private DataCollectionService dataCollectionService;

    @Autowired
    private PremiumRateService premiumRateService;

    @ApiOperation("测试腾讯财经API - 获取单个股票价格")
    @GetMapping("/stock-price/{stockCode}")
    public Result<StockPriceRecord> getStockPrice(
            @ApiParam("股票代码") @PathVariable String stockCode,
            @ApiParam("市场类型：A或H") @RequestParam(defaultValue = "A") String marketType) {
        try {
            StockPriceRecord record = tencentFinanceService.getStockPrice(stockCode, marketType);
            if (record != null) {
                return Result.success("获取股票价格成功", record);
            } else {
                return Result.error("获取股票价格失败");
            }
        } catch (Exception e) {
            log.error("获取股票价格异常", e);
            return Result.error("获取股票价格异常: " + e.getMessage());
        }
    }

    @ApiOperation("测试腾讯财经API - 获取汇率")
    @GetMapping("/exchange-rate/{currencyPair}")
    public Result<Map<String, Object>> getExchangeRate(
            @ApiParam("货币对，如HKDCNY") @PathVariable String currencyPair) {
        try {
            BigDecimal rate = tencentFinanceService.getExchangeRate(currencyPair);
            Map<String, Object> data = new HashMap<>();
            data.put("currency_pair", currencyPair);
            data.put("exchange_rate", rate);
            data.put("timestamp", System.currentTimeMillis());
            
            return Result.success("获取汇率成功", data);
        } catch (Exception e) {
            log.error("获取汇率异常", e);
            return Result.error("获取汇率异常: " + e.getMessage());
        }
    }

    @ApiOperation("检查市场开市状态")
    @GetMapping("/market-status/{marketType}")
    public Result<Map<String, Object>> getMarketStatus(
            @ApiParam("市场类型：A或H") @PathVariable String marketType) {
        try {
            boolean isOpen = tencentFinanceService.isMarketOpen(marketType);
            Map<String, Object> data = new HashMap<>();
            data.put("market_type", marketType);
            data.put("is_open", isOpen);
            data.put("timestamp", System.currentTimeMillis());
            
            return Result.success("获取市场状态成功", data);
        } catch (Exception e) {
            log.error("获取市场状态异常", e);
            return Result.error("获取市场状态异常: " + e.getMessage());
        }
    }

    @ApiOperation("手动触发数据采集")
    @PostMapping("/collect")
    public Result<String> collectData() {
        try {
            dataCollectionService.collectAllStockData();
            return Result.success("数据采集任务已触发");
        } catch (Exception e) {
            log.error("手动数据采集异常", e);
            return Result.error("数据采集失败: " + e.getMessage());
        }
    }

    @ApiOperation("采集指定股票数据")
    @PostMapping("/collect/{stockCode}")
    public Result<String> collectStockData(
            @ApiParam("股票代码") @PathVariable String stockCode) {
        try {
            dataCollectionService.collectStockData(stockCode);
            return Result.success("股票数据采集完成: " + stockCode);
        } catch (Exception e) {
            log.error("采集指定股票数据异常", e);
            return Result.error("采集失败: " + e.getMessage());
        }
    }

    @ApiOperation("查询溢价率记录")
    @GetMapping("/premium-rates")
    public Result<List<PremiumRateRecord>> getPremiumRates(
            @ApiParam("股票代码") @RequestParam(required = false) String stockCode,
            @ApiParam("交易日期，格式：yyyy-MM-dd") @RequestParam(required = false) String tradeDate) {
        try {
            List<PremiumRateRecord> records;
            
            if (stockCode != null && tradeDate != null) {
                LocalDate date = LocalDate.parse(tradeDate);
                records = premiumRateService.getPremiumRatesByStockAndDate(stockCode, date);
            } else if (tradeDate != null) {
                LocalDate date = LocalDate.parse(tradeDate);
                records = premiumRateService.getLatestPremiumRatesByDate(date);
            } else {
                // 查询今日所有记录
                records = premiumRateService.getLatestPremiumRatesByDate(LocalDate.now());
            }
            
            return Result.success("查询成功", records);
        } catch (Exception e) {
            log.error("查询溢价率记录异常", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}