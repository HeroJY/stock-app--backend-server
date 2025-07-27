package com.stock.premium.service;

/**
 * 数据采集服务接口
 * 
 * @author system
 * @since 2024-01-01
 */
public interface DataCollectionService {

    /**
     * 在交易时段采集所有股票数据并计算溢价率
     */
    void collectAllStockData();

}