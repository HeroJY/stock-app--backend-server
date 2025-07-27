package com.stock.premium.controller;

import com.stock.premium.entity.PremiumRateRecord;
import com.stock.premium.service.PremiumRateService;
import com.stock.premium.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 溢价率数据控制器
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Api(tags = "溢价率数据接口")
@RestController
@RequestMapping("/premium")
public class PremiumRateController {

    @Autowired
    private PremiumRateService premiumRateService;

    @ApiOperation("查询指定股票和日期的溢价率记录")
    @GetMapping("/stock/{stockCode}")
    public Result<List<PremiumRateRecord>> getPremiumRatesByStock(
            @ApiParam("股票代码") @PathVariable String stockCode,
            @ApiParam("交易日期，格式：yyyy-MM-dd，不传则查询今日") @RequestParam(required = false) String tradeDate) {
        try {
            LocalDate date = tradeDate != null ? LocalDate.parse(tradeDate) : LocalDate.now();
            List<PremiumRateRecord> records = premiumRateService.getPremiumRatesByStockAndDate(stockCode, date);
            return Result.success("查询成功", records);
        } catch (Exception e) {
            log.error("查询溢价率记录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("查询指定日期所有股票的最新溢价率")
    @GetMapping("/latest")
    public Result<List<PremiumRateRecord>> getLatestPremiumRates(
            @ApiParam("交易日期，格式：yyyy-MM-dd，不传则查询今日") @RequestParam(required = false) String tradeDate) {
        try {
            LocalDate date = tradeDate != null ? LocalDate.parse(tradeDate) : LocalDate.now();
            List<PremiumRateRecord> records = premiumRateService.getLatestPremiumRatesByDate(date);
            return Result.success("查询成功", records);
        } catch (Exception e) {
            log.error("查询最新溢价率失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("查询溢价率历史数据（分页）")
    @GetMapping("/history")
    public Result<List<PremiumRateRecord>> getPremiumRateHistory(
            @ApiParam(value = "股票代码", required = true) @RequestParam String stockCode,
            @ApiParam("开始日期") @RequestParam(required = false) String startDate,
            @ApiParam("结束日期") @RequestParam(required = false) String endDate,
            @ApiParam("页码，从1开始") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") Integer size) {
        try {
            // 验证股票代码不能为空
            if (stockCode == null || stockCode.trim().isEmpty()) {
                return Result.error("股票代码不能为空");
            }
            
            // 这里可以根据需要实现分页查询逻辑
            // 暂时返回今日数据
            LocalDate date = LocalDate.now();
            List<PremiumRateRecord> records = premiumRateService.getPremiumRatesByStockAndDate(stockCode, date);
            
            return Result.success("查询成功", records);
        } catch (Exception e) {
            log.error("查询溢价率历史数据失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}