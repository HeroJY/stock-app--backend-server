package com.stock.premium.controller;

import com.stock.premium.entity.DailyPremiumStats;
import com.stock.premium.entity.PremiumRateRecord;
import com.stock.premium.service.DailyStatsService;
import com.stock.premium.service.PremiumRateService;
import com.stock.premium.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表数据控制器
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Api(tags = "报表数据接口")
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private DailyStatsService dailyStatsService;

    @Autowired
    private PremiumRateService premiumRateService;

    @ApiOperation("获取股票溢价率概览报表")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverviewReport(
            @ApiParam("交易日期，格式：yyyy-MM-dd，不传则查询今日") @RequestParam(required = false) String tradeDate) {
        try {
            LocalDate date = tradeDate != null ? LocalDate.parse(tradeDate) : LocalDate.now();
            
            // 获取当日统计数据
            List<DailyPremiumStats> dailyStats = dailyStatsService.getStatsByDateRange(date, date);
            
            // 获取最新溢价率数据
            List<PremiumRateRecord> latestRates = premiumRateService.getLatestPremiumRatesByDate(date);
            
            Map<String, Object> report = new HashMap<>();
            report.put("trade_date", date);
            report.put("daily_stats", dailyStats);
            report.put("latest_rates", latestRates);
            report.put("stock_count", latestRates.size());
            
            return Result.success("查询成功", report);
        } catch (Exception e) {
            log.error("获取概览报表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取股票溢价率趋势报表")
    @GetMapping("/trend/{stockCode}")
    public Result<Map<String, Object>> getTrendReport(
            @ApiParam("股票代码") @PathVariable String stockCode,
            @ApiParam("查询天数，默认7天") @RequestParam(defaultValue = "7") Integer days) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);
            
            // 获取历史统计数据
            List<DailyPremiumStats> stats = dailyStatsService.getStatsByDateRange(startDate, endDate);
            
            // 过滤指定股票的数据
            List<DailyPremiumStats> stockStats = stats.stream()
                    .filter(s -> stockCode.equals(s.getStockCode()))
                    .toList();
            
            Map<String, Object> report = new HashMap<>();
            report.put("stock_code", stockCode);
            report.put("start_date", startDate);
            report.put("end_date", endDate);
            report.put("trend_data", stockStats);
            
            return Result.success("查询成功", report);
        } catch (Exception e) {
            log.error("获取趋势报表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取溢价率排行榜")
    @GetMapping("/ranking")
    public Result<Map<String, Object>> getRankingReport(
            @ApiParam("交易日期，格式：yyyy-MM-dd，不传则查询今日") @RequestParam(required = false) String tradeDate,
            @ApiParam("排序字段：premium_rate(溢价率)、max_rate(最高)、min_rate(最低)") @RequestParam(defaultValue = "premium_rate") String sortBy,
            @ApiParam("排序方向：asc(升序)、desc(降序)") @RequestParam(defaultValue = "desc") String sortOrder,
            @ApiParam("返回条数") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            LocalDate date = tradeDate != null ? LocalDate.parse(tradeDate) : LocalDate.now();
            
            List<PremiumRateRecord> records = premiumRateService.getLatestPremiumRatesByDate(date);
            
            // 根据排序字段和方向排序
            records.sort((r1, r2) -> {
                int result = r1.getPremiumRate().compareTo(r2.getPremiumRate());
                return "asc".equals(sortOrder) ? result : -result;
            });
            
            // 限制返回条数
            if (records.size() > limit) {
                records = records.subList(0, limit);
            }
            
            Map<String, Object> report = new HashMap<>();
            report.put("trade_date", date);
            report.put("sort_by", sortBy);
            report.put("sort_order", sortOrder);
            report.put("ranking_data", records);
            
            return Result.success("查询成功", report);
        } catch (Exception e) {
            log.error("获取排行榜失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取市场统计摘要")
    @GetMapping("/summary")
    public Result<Map<String, Object>> getMarketSummary(
            @ApiParam("交易日期，格式：yyyy-MM-dd，不传则查询今日") @RequestParam(required = false) String tradeDate) {
        try {
            LocalDate date = tradeDate != null ? LocalDate.parse(tradeDate) : LocalDate.now();
            
            List<PremiumRateRecord> records = premiumRateService.getLatestPremiumRatesByDate(date);
            
            if (records.isEmpty()) {
                return Result.success("暂无数据", new HashMap<>());
            }
            
            // 计算市场统计指标
            double avgPremium = records.stream()
                    .mapToDouble(r -> r.getPremiumRate().doubleValue())
                    .average()
                    .orElse(0.0);
            
            double maxPremium = records.stream()
                    .mapToDouble(r -> r.getPremiumRate().doubleValue())
                    .max()
                    .orElse(0.0);
            
            double minPremium = records.stream()
                    .mapToDouble(r -> r.getPremiumRate().doubleValue())
                    .min()
                    .orElse(0.0);
            
            long positiveCount = records.stream()
                    .mapToDouble(r -> r.getPremiumRate().doubleValue())
                    .filter(rate -> rate > 0)
                    .count();
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("trade_date", date);
            summary.put("total_stocks", records.size());
            summary.put("avg_premium_rate", avgPremium);
            summary.put("max_premium_rate", maxPremium);
            summary.put("min_premium_rate", minPremium);
            summary.put("positive_premium_count", positiveCount);
            summary.put("negative_premium_count", records.size() - positiveCount);
            
            return Result.success("查询成功", summary);
        } catch (Exception e) {
            log.error("获取市场摘要失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}