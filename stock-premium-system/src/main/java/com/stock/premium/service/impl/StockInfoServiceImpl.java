package com.stock.premium.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.premium.entity.StockInfo;
import com.stock.premium.mapper.StockInfoMapper;
import com.stock.premium.service.StockInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        queryWrapper.eq("status", 1); // 启用状态
        queryWrapper.orderByAsc("stock_code");
        return list(queryWrapper);
    }

    @Override
    public StockInfo getHStockByACode(String aStockCode) {
        // 根据A股代码查询对应的H股信息
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("a_stock_code", aStockCode)
                   .eq("market_type", "H")
                   .eq("status", 1);
        return getOne(queryWrapper);
    }

    @Override
    public List<String> getAHStockPairs() {
        // 查询所有有A+H股票对的股票代码
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("a_stock_code")
                   .isNotNull("h_stock_code")
                   .eq("status", 1);
        
        List<StockInfo> stocks = list(queryWrapper);
        return stocks.stream()
                    .map(StockInfo::getAStockCode)
                    .distinct()
                    .collect(Collectors.toList());
    }

    @Override
    public StockInfo getByStockCode(String stockCode) {
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("stock_code", stockCode)
                   .eq("status", 1);
        return getOne(queryWrapper);
    }
}