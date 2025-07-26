package com.stock.premium.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.premium.entity.DailyPremiumStats;
import com.stock.premium.entity.PremiumRateRecord;
import com.stock.premium.mapper.DailyPremiumStatsMapper;
import com.stock.premium.service.DailyStatsService;
import com.stock.premium.service.PremiumRateService;
import com.stock.premium.service.StockInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    private PremiumRateService premiumRateService;

    @Autowired
    private StockInfoService stockInfoService;

    @Override
    public void generateDailyStats(LocalDate tradeDate) {
        try {
            log.info("开始生成日统计数据: {}", tradeDate);
            
            // 获取所有A+H股票对
            List<String> stockCodes = stockInfoService.getAHStockPairs();
            
            int successCount = 0;
            for (String stockCode : stockCodes) {
                try {
                    DailyPremiumStats stats = calculateDailyStats(stockCode, tradeDate);
                    if (stats != null) {
                        // 检查是否已存在记录
                        LambdaQueryWrapper<DailyPremiumStats> wrapper = new LambdaQueryWrapper<>();
                        wrapper.eq(DailyPremiumStats::getStockCode, stockCode)
                               .eq(DailyPremiumStats::getTradeDate, tradeDate);
                        
                        DailyPremiumStats existing = this.getOne(wrapper);
                        if (existing != null) {
                            // 更新现有记录
                            stats.setId(existing.getId());
                            this.updateById(stats);
                        } else {
                            // 新增记录
                            this.save(stats);
                        }
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("生成股票统计数据失败: stockCode={}, date={}", stockCode, tradeDate, e);
                }
            }
            
            log.info("日统计数据生成完成: 日期={}, 成功处理{}个股票", tradeDate, successCount);
        } catch (Exception e) {
            log.error("生成日统计数据失败: {}", tradeDate, e);
        }
    }

    @Override
    public void generateAllStocksDailyStats(LocalDate tradeDate) {
        generateDailyStats(tradeDate);
    }

    @Override
    public DailyPremiumStats calculateDailyStats(String stockCode, LocalDate tradeDate) {
        try {
            // 获取指定股票和日期的所有溢价率记录
            List<PremiumRateRecord> records = premiumRateService.getPremiumRatesByStockAndDate(stockCode, tradeDate);
            
            if (records == null || records.isEmpty()) {
                log.warn("未找到溢价率记录: stockCode={}, date={}", stockCode, tradeDate);
                return null;
            }

            // 按时间排序
            records.sort((r1, r2) -> r1.getRecordTime().compareTo(r2.getRecordTime()));

            // 提取溢价率数据
            List<BigDecimal> premiumRates = records.stream()
                    .map(PremiumRateRecord::getPremiumRate)
                    .collect(Collectors.toList());

            // 计算统计指标
            DailyPremiumStats stats = new DailyPremiumStats();
            stats.setStockCode(stockCode);
            stats.setTradeDate(tradeDate);
            stats.setRecordCount(records.size());

            // 开盘和收盘溢价率
            stats.setOpenPremiumRate(records.get(0).getPremiumRate());
            stats.setClosePremiumRate(records.get(records.size() - 1).getPremiumRate());

            // 最高和最低溢价率
            stats.setMaxPremiumRate(Collections.max(premiumRates));
            stats.setMinPremiumRate(Collections.min(premiumRates));

            // 平均溢价率
            BigDecimal avgRate = premiumRates.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(premiumRates.size()), 4, RoundingMode.HALF_UP);
            stats.setAvgPremiumRate(avgRate);

            // 计算分位数
            stats.setPercentile95(calculatePercentile(premiumRates, 0.95));
            stats.setPercentile5(calculatePercentile(premiumRates, 0.05));

            log.debug("计算统计数据完成: stockCode={}, 记录数={}, 最高={}%, 最低={}%", 
                     stockCode, records.size(), stats.getMaxPremiumRate(), stats.getMinPremiumRate());

            return stats;
        } catch (Exception e) {
            log.error("计算日统计数据失败: stockCode={}, date={}", stockCode, tradeDate, e);
            return null;
        }
    }

    @Override
    public List<DailyPremiumStats> getStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        return baseMapper.selectByDateRange(startDate, endDate);
    }

    @Override
    public List<DailyPremiumStats> getStatsByStockCode(String stockCode, Integer limit) {
        return baseMapper.selectByStockCode(stockCode, limit != null ? limit : 30);
    }

    /**
     * 计算分位数
     * @param values 数值列表
     * @param percentile 分位数（0-1之间）
     * @return 分位数值
     */
    private BigDecimal calculatePercentile(List<BigDecimal> values, double percentile) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 排序
        List<BigDecimal> sortedValues = values.stream()
                .sorted()
                .collect(Collectors.toList());

        int size = sortedValues.size();
        if (size == 1) {
            return sortedValues.get(0);
        }

        // 计算分位数位置
        double position = percentile * (size - 1);
        int lowerIndex = (int) Math.floor(position);
        int upperIndex = (int) Math.ceil(position);

        if (lowerIndex == upperIndex) {
            return sortedValues.get(lowerIndex);
        }

        // 线性插值
        BigDecimal lowerValue = sortedValues.get(lowerIndex);
        BigDecimal upperValue = sortedValues.get(upperIndex);
        double weight = position - lowerIndex;

        return lowerValue.add(
                upperValue.subtract(lowerValue)
                        .multiply(new BigDecimal(weight))
        ).setScale(4, RoundingMode.HALF_UP);
    }
}