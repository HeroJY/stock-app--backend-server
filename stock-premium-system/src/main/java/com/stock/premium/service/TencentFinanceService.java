package com.stock.premium.service;

import com.stock.premium.entity.StockPriceRecord;

import java.util.List;

/**
 * 腾讯财经API服务接口
 * 
 * @author system
 * @since 2024-01-01
 */
public interface TencentFinanceService {

    /**
     * 获取股票实时价格
     * @param stockCode 股票代码
     * @param marketType 市场类型 A/H
     * @return 股票价格记录
     */
    StockPriceRecord getStockPrice(String stockCode, String marketType);

    /**
     * 批量获取股票价格
     * @param stockCodes 股票代码列表
     * @return 股票价格记录列表
     */
    List<StockPriceRecord> getBatchStockPrices(List<String> stockCodes);


    /**
     * 检查市场是否开市
     * @param marketType 市场类型
     * @return 是否开市
     */
    boolean isMarketOpen(String marketType);
}