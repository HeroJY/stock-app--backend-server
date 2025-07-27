package com.stock.premium.service;

import com.stock.premium.entity.StockPriceRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * 股票价格服务接口
 * 
 * @author system
 * @since 2024-01-01
 */
public interface StockPriceService {

    /**
     * 保存股票价格记录
     * @param record 股票价格记录
     * @return 保存结果
     */
    boolean saveStockPriceRecord(StockPriceRecord record);

    /**
     * 根据股票代码和日期查询价格记录
     * @param stockCode 股票代码
     * @param tradeDate 交易日期
     * @return 价格记录列表
     */
    List<StockPriceRecord> getByStockCodeAndDate(String stockCode, LocalDate tradeDate);

    /**
     * 获取指定日期的最新价格记录
     * @param tradeDate 交易日期
     * @return 价格记录列表
     */
    List<StockPriceRecord> getLatestByDate(LocalDate tradeDate);
}