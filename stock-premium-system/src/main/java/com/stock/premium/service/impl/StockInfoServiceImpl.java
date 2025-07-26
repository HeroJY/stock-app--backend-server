package com.stock.premium.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.premium.entity.StockInfo;
import com.stock.premium.mapper.StockInfoMapper;
import com.stock.premium.service.StockInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 股票基础信息服务实现类
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Service
public class StockInfoServiceImpl extends ServiceImpl<StockInfoMapper, StockInfo> implements StockInfoService {

    @Override
    public List<StockInfo> getActiveStocks() {
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                   .eq("deleted", 0)
                   .orderByAsc("a_stock_code");
        return list(queryWrapper);
    }

    @Override
    public StockInfo getByStockCode(String stockCode) {
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> 
            wrapper.eq("a_stock_code", stockCode)
                   .or()
                   .eq("h_stock_code", stockCode)
        ).eq("status", 1)
         .eq("deleted", 0);
        
        StockInfo result = getOne(queryWrapper);
        if (result != null) {
            log.debug("根据股票代码 {} 查询到股票信息: {}", stockCode, result.getStockName());
        } else {
            log.warn("未找到股票代码为 {} 的股票信息", stockCode);
        }
        return result;
    }

    @Override
    public List<StockInfo> searchStockByName(String stockName) {
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("stock_name", stockName)
                   .eq("status", 1)
                   .eq("deleted", 0)
                   .orderByAsc("a_stock_code");
        
        List<StockInfo> results = list(queryWrapper);
        log.debug("根据股票名称 '{}' 搜索到 {} 条记录", stockName, results.size());
        return results;
    }
}