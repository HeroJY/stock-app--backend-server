package com.stock.premium.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.premium.entity.DailyPremiumStats;
import com.stock.premium.entity.StockInfo;
import com.stock.premium.mapper.DailyPremiumStatsMapper;
import com.stock.premium.service.DailyStatsService;
import com.stock.premium.service.StockInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 日统计数据服务实现类
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Service
public class DailyStatsServiceImpl extends ServiceImpl<DailyPremiumStatsMapper, DailyPremiumStats> implements DailyStatsService {

    @Autowired
    private StockInfoService stockInfoService;

    @Override
    public void generateDailyStats(LocalDate tradeDate) {
        log.info("开始生成日期 {} 的统计数据", tradeDate);
        
        List<StockInfo> ahStocks = stockInfoService.getActiveStocks();
        
        for (StockInfo stock : ahStocks) {
            try {
                DailyPremiumStats stats = calculateDailyStats(stock.getAStockCode(), tradeDate);
                if (stats != null) {
                    // 检查是否已存在该日期的统计数据
                    QueryWrapper<DailyPremiumStats> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("stock_code", stock.getAStockCode())
                               .eq("trade_date", tradeDate);
                    
                    DailyPremiumStats existing = getOne(queryWrapper);
                    if (existing != null) {
                        // 更新现有记录
                        stats.setId(existing.getId());
                        updateById(stats);
                        log.debug("更新股票 {} 日期 {} 的统计数据", stock.getAStockCode(), tradeDate);
                    } else {
                        // 插入新记录
                        save(stats);
                        log.debug("新增股票 {} 日期 {} 的统计数据", stock.getAStockCode(), tradeDate);
                    }
                }
            } catch (Exception e) {
                log.error("生成股票 {} 日期 {} 的统计数据时发生错误", stock.getAStockCode(), tradeDate, e);
            }
        }
        
        log.info("完成日期 {} 的统计数据生成", tradeDate);
    }

    @Override
    public void generateAllStocksDailyStats(LocalDate tradeDate) {
        generateDailyStats(tradeDate);
    }

    @Override
    public DailyPremiumStats calculateDailyStats(String stockCode, LocalDate tradeDate) {
        try {
            // 获取股票信息
            StockInfo stockInfo = stockInfoService.getByStockCode(stockCode);
            if (stockInfo == null) {
                log.warn("未找到股票代码 {} 的信息", stockCode);
                return null;
            }

            // 这里应该根据实际业务逻辑计算统计数据
            // 示例：计算溢价率统计
            DailyPremiumStats stats = new DailyPremiumStats();
            stats.setStockCode(stockCode);
            stats.setTradeDate(tradeDate);
            
            // TODO: 实现具体的统计计算逻辑
            // 例如：计算平均溢价率、最高溢价率、最低溢价率等
            
            return stats;
            
        } catch (Exception e) {
            log.error("计算股票 {} 日期 {} 的统计数据时发生错误", stockCode, tradeDate, e);
            return null;
        }
    }

    @Override
    public List<DailyPremiumStats> getStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<DailyPremiumStats> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("trade_date", startDate, endDate)
                   .orderByDesc("trade_date")
                   .orderByAsc("stock_code");
        
        List<DailyPremiumStats> results = list(queryWrapper);
        log.debug("查询日期范围 {} 到 {} 的统计数据，共 {} 条记录", startDate, endDate, results.size());
        return results;
    }

    @Override
    public List<DailyPremiumStats> getStatsByStockCode(String stockCode, Integer limit) {
        QueryWrapper<DailyPremiumStats> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("stock_code", stockCode)
                   .orderByDesc("trade_date");
        
        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }
        
        List<DailyPremiumStats> results = list(queryWrapper);
        log.debug("查询股票 {} 的历史统计数据，共 {} 条记录", stockCode, results.size());
        return results;
    }
}