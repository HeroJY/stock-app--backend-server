package com.stock.premium.service;

import com.stock.premium.dto.ExchangeRateQueryDTO;
import com.stock.premium.vo.ExchangeRateSimpleVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 汇率服务接口
 * 
 * @author system
 * @since 2024-01-01
 */
public interface ExchangeRateService {
    
    /**
     * 获取最新汇率
     */
    ExchangeRateSimpleVO getLatestRate(String currencyPair);
    
    /**
     * 手动更新汇率
     */
    void updateRate(String currencyPair, BigDecimal rate);
    
    
    /**
     * 查询历史汇率
     */
    List<ExchangeRateSimpleVO> getHistoryRates(ExchangeRateQueryDTO queryDTO);
    
    /**
     * 按日期范围查询汇率
     */
    List<ExchangeRateSimpleVO> getRatesByDateRange(String currencyPair, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取汇率统计信息
     */
    ExchangeRateSimpleVO getRateStats(String currencyPair, LocalDate tradeDate);
    
    /**
     * 删除指定日期的汇率记录
     */
    void deleteByDate(String currencyPair, LocalDate tradeDate);
    
}