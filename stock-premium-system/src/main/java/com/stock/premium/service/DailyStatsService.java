package com.stock.premium.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.premium.entity.DailyPremiumStats;

import java.time.LocalDate;
import java.util.List;

/**
 * 日统计数据服务接口
 * 
 * @author system
 * @since 2024-01-01
 */
public interface DailyStatsService extends IService<DailyPremiumStats> {

    /**
     * 生成指定日期的统计数据
     * @param tradeDate 交易日期
     */
    void generateDailyStats(LocalDate tradeDate);

    /**
     * 生成所有股票指定日期的统计数据
     * @param tradeDate 交易日期
     */
    void generateAllStocksDailyStats(LocalDate tradeDate);

    /**
     * 计算指定股票和日期的统计数据
     * @param stockCode 股票代码
     * @param tradeDate 交易日期
     * @return 统计数据
     */
    DailyPremiumStats calculateDailyStats(String stockCode, LocalDate tradeDate);

    /**
     * 查询指定日期范围的统计数据
     */
    List<DailyPremiumStats> getStatsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 查询指定股票的历史统计数据
     */
    List<DailyPremiumStats> getStatsByStockCode(String stockCode, Integer limit);
}