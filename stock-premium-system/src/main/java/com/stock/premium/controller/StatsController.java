package com.stock.premium.controller;

import com.stock.premium.entity.DailyPremiumStats;
import com.stock.premium.service.DailyStatsService;
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
 * 统计数据控制器
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Api(tags = "统计数据接口")
@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private DailyStatsService dailyStatsService;

    @ApiOperation("手动生成日统计数据")
    @PostMapping("/generate/{tradeDate}")
    public Result<String> generateDailyStats(
            @ApiParam("交易日期，格式：yyyy-MM-dd") @PathVariable String tradeDate) {
        try {
            LocalDate date = LocalDate.parse(tradeDate);
            dailyStatsService.generateAllStocksDailyStats(date);
            return Result.success("统计数据生成完成: " + tradeDate);
        } catch (Exception e) {
            log.error("生成统计数据失败", e);
            return Result.error("生成失败: " + e.getMessage());
        }
    }

    @ApiOperation("查询日期范围内的统计数据")
    @GetMapping("/range")
    public Result<List<DailyPremiumStats>> getStatsByDateRange(
            @ApiParam("开始日期") @RequestParam String startDate,
            @ApiParam("结束日期") @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            List<DailyPremiumStats> stats = dailyStatsService.getStatsByDateRange(start, end);
            return Result.success("查询成功", stats);
        } catch (Exception e) {
            log.error("查询统计数据失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("查询指定股票的历史统计数据")
    @GetMapping("/stock/{stockCode}")
    public Result<List<DailyPremiumStats>> getStatsByStockCode(
            @ApiParam("股票代码") @PathVariable String stockCode,
            @ApiParam("查询条数，默认30") @RequestParam(defaultValue = "30") Integer limit) {
        try {
            List<DailyPremiumStats> stats = dailyStatsService.getStatsByStockCode(stockCode, limit);
            return Result.success("查询成功", stats);
        } catch (Exception e) {
            log.error("查询股票统计数据失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("计算指定股票和日期的统计数据")
    @GetMapping("/calculate")
    public Result<DailyPremiumStats> calculateStats(
            @ApiParam("股票代码") @RequestParam String stockCode,
            @ApiParam("交易日期") @RequestParam String tradeDate) {
        try {
            LocalDate date = LocalDate.parse(tradeDate);
            DailyPremiumStats stats = dailyStatsService.calculateDailyStats(stockCode, date);
            
            if (stats != null) {
                return Result.success("计算成功", stats);
            } else {
                return Result.error("未找到相关数据或计算失败");
            }
        } catch (Exception e) {
            log.error("计算统计数据失败", e);
            return Result.error("计算失败: " + e.getMessage());
        }
    }
}