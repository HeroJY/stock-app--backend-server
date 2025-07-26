package com.stock.premium.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.premium.entity.PremiumRateRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 溢价率服务接口
 * 
 * @author system
 * @since 2024-01-01
 */
public interface PremiumRateService extends IService<PremiumRateRecord> {

    /**
     * 计算溢价率
     * @param aStockPrice A股价格
     * @param hStockPrice H股价格
     * @param exchangeRate 汇率
     * @return 溢价率
     */
    BigDecimal calculatePremiumRate(BigDecimal aStockPrice, BigDecimal hStockPrice, BigDecimal exchangeRate);

    /**
     * 记录溢价率数据
     * @param stockCode 股票代码
     * @param aStockPrice A股价格
     * @param hStockPrice H股价格
     * @param exchangeRate 汇率
     * @return 溢价率记录
     */
    PremiumRateRecord recordPremiumRate(String stockCode, BigDecimal aStockPrice, 
                                       BigDecimal hStockPrice, BigDecimal exchangeRate);

    /**
     * 获取指定日期和股票的溢价率记录
     */
    List<PremiumRateRecord> getPremiumRatesByStockAndDate(String stockCode, LocalDate tradeDate);

    /**
     * 获取指定日期所有股票的最新溢价率
     */
    List<PremiumRateRecord> getLatestPremiumRatesByDate(LocalDate tradeDate);
}