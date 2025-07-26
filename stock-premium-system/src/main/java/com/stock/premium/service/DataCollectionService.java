package com.stock.premium.service;

/**
 * 数据采集服务接口
 * 
 * @author system
 * @since 2024-01-01
 */
public interface DataCollectionService {

    /**
     * 采集所有股票数据并计算溢价率
     */
    void collectAllStockData();

    /**
     * 采集指定股票的数据
     * @param stockCode 股票代码
     */
    void collectStockData(String stockCode);

    /**
     * 检查并采集数据（仅在交易时间内执行）
     */
    void collectDataIfTradingTime();
}