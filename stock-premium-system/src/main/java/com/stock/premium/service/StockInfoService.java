package com.stock.premium.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.premium.entity.StockInfo;

import java.util.List;

/**
 * 股票基础信息服务接口
 * 
 * @author system
 * @since 2024-01-01
 */
public interface StockInfoService extends IService<StockInfo> {

    /**
     * 查询所有启用的股票信息
     */
    List<StockInfo> getActiveStocks();

    /**
     * 根据A股代码查询对应的H股信息
     */
    StockInfo getHStockByACode(String aStockCode);

    /**
     * 查询所有A+H股票对
     */
    List<String> getAHStockPairs();

    /**
     * 根据股票代码查询股票信息
     */
    StockInfo getByStockCode(String stockCode);
}