package com.stock.premium.service.impl;

import com.stock.premium.entity.StockPriceRecord;
import com.stock.premium.mapper.StockPriceRecordMapper;
import com.stock.premium.service.StockPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 股票价格服务实现类
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Service
public class StockPriceServiceImpl implements StockPriceService {

    @Autowired
    private StockPriceRecordMapper stockPriceRecordMapper;

    @Override
    public boolean saveStockPriceRecord(StockPriceRecord record) {
        try {
            int result = stockPriceRecordMapper.insert(record);
            if (result > 0) {
                log.debug("成功保存股票价格记录: {}", record.getStockCode());
                return true;
            } else {
                log.warn("保存股票价格记录失败: {}", record.getStockCode());
                return false;
            }
        } catch (Exception e) {
            log.error("保存股票价格记录时发生错误: {}", record.getStockCode(), e);
            return false;
        }
    }

    @Override
    public List<StockPriceRecord> getByStockCodeAndDate(String stockCode, LocalDate tradeDate) {
        try {
            return stockPriceRecordMapper.selectByStockCodeAndDate(stockCode, tradeDate);
        } catch (Exception e) {
            log.error("查询股票价格记录时发生错误: stockCode={}, tradeDate={}", stockCode, tradeDate, e);
            return null;
        }
    }

    @Override
    public List<StockPriceRecord> getLatestByDate(LocalDate tradeDate) {
        try {
            return stockPriceRecordMapper.selectLatestByDate(tradeDate);
        } catch (Exception e) {
            log.error("查询最新股票价格记录时发生错误: tradeDate={}", tradeDate, e);
            return null;
        }
    }
}