package com.stock.premium.controller;

import com.stock.premium.common.Result;
import com.stock.premium.dto.ExchangeRateQueryDTO;
import com.stock.premium.service.ExchangeRateService;
import com.stock.premium.vo.ExchangeRateSimpleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 汇率管理控制器
 * 
 * @author system
 * @since 2024-01-01
 */
@Api(tags = "汇率管理")
@RestController
@RequestMapping("/api/exchange-rate")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @ApiOperation("获取最新汇率")
    @GetMapping("/latest")
    public Result<ExchangeRateSimpleVO> getLatestRate(
            @ApiParam("货币对，默认HKDCNY") @RequestParam(defaultValue = "HKDCNY") String currencyPair) {
        ExchangeRateSimpleVO latestRate = exchangeRateService.getLatestRate(currencyPair);
        return Result.success(latestRate);
    }

    @ApiOperation("手动更新汇率")
    @PostMapping("/update")
    public Result<Void> updateRate(
            @ApiParam("货币对") @RequestParam String currencyPair,
            @ApiParam("汇率值") @RequestParam BigDecimal rate) {
        exchangeRateService.updateRate(currencyPair, rate);
        return Result.success();
    }

    @ApiOperation("查询历史汇率")
    @GetMapping("/history")
    public Result<List<ExchangeRateSimpleVO>> getHistoryRates(@Valid ExchangeRateQueryDTO queryDTO) {
        List<ExchangeRateSimpleVO> historyRates = exchangeRateService.getHistoryRates(queryDTO);
        return Result.success(historyRates);
    }

    @ApiOperation("按日期范围查询汇率")
    @GetMapping("/range")
    public Result<List<ExchangeRateSimpleVO>> getRatesByDateRange(
            @ApiParam("货币对") @RequestParam(defaultValue = "HKDCNY") String currencyPair,
            @ApiParam("开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam("结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        // 如果没有提供日期范围，使用最近7天
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        List<ExchangeRateSimpleVO> rates = exchangeRateService.getRatesByDateRange(currencyPair, startDate, endDate);
        return Result.success(rates);
    }

    @ApiOperation("获取汇率统计信息")
    @GetMapping("/stats")
    public Result<ExchangeRateSimpleVO> getRateStats(
            @ApiParam("货币对") @RequestParam(defaultValue = "HKDCNY") String currencyPair,
            @ApiParam("统计日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tradeDate) {
        // 如果没有提供日期，使用今天
        if (tradeDate == null) {
            tradeDate = LocalDate.now();
        }
        ExchangeRateSimpleVO stats = exchangeRateService.getRateStats(currencyPair, tradeDate);
        return Result.success(stats);
    }

    @ApiOperation("删除指定日期的汇率记录")
    @DeleteMapping("/date/{tradeDate}")
    public Result<Void> deleteByDate(
            @ApiParam("交易日期") @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tradeDate,
            @ApiParam("货币对") @RequestParam(defaultValue = "HKDCNY") String currencyPair) {
        exchangeRateService.deleteByDate(currencyPair, tradeDate);
        return Result.success();
    }
}