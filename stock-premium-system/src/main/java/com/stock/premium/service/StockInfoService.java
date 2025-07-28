package com.stock.premium.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.premium.entity.StockInfo;
import com.stock.premium.vo.StockDetailVO;

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
     * 根据股票代码查询股票信息
     */
    StockInfo getByStockCode(String stockCode);
    
    /**
     * 根据股票代码获取股票详细信息
     * @param stockCode 股票代码
     * @return 股票详细信息
     */
    StockDetailVO getStockDetail(String stockCode);
}
