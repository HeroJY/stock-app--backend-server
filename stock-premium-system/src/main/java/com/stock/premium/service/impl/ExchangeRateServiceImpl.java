package com.stock.premium.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stock.premium.dto.ExchangeRateQueryDTO;
import com.stock.premium.entity.ExchangeRateRecord;
import com.stock.premium.mapper.ExchangeRateRecordMapper;
import com.stock.premium.service.ExchangeRateService;
import com.stock.premium.service.TencentFinanceService;
import com.stock.premium.vo.ExchangeRateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 汇率服务实现类
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateRecordMapper exchangeRateRecordMapper;
    private final TencentFinanceService tencentFinanceService;

    @Override
    public ExchangeRateVO getLatestRate(String currencyPair) {
        ExchangeRateRecord record = exchangeRateRecordMapper.selectLatest();
        if (record == null) {
            // 如果没有记录，尝试从外部API获取
            return refreshRate(currencyPair);
        }
        return convertToVO(record);
    }

    @Override
    @Transactional
    public void updateRate(String currencyPair, BigDecimal rate) {
        ExchangeRateRecord record = new ExchangeRateRecord();
        record.setCurrencyPair(currencyPair);
        record.setRate(rate);
        record.setRecordTime(LocalDateTime.now());
        record.setTradeDate(LocalDate.now());
        record.setDataSource("manual");
        
        exchangeRateRecordMapper.insert(record);
        log.info("手动更新汇率: {} = {}", currencyPair, rate);
    }

    @Override
    @Transactional
    public void batchImport(List<ExchangeRateRecord> records) {
        for (ExchangeRateRecord record : records) {
            if (record.getRecordTime() == null) {
                record.setRecordTime(LocalDateTime.now());
            }
            if (record.getTradeDate() == null) {
                record.setTradeDate(LocalDate.now());
            }
            if (record.getDataSource() == null) {
                record.setDataSource("import");
            }
            exchangeRateRecordMapper.insert(record);
        }
        log.info("批量导入汇率数据: {} 条", records.size());
    }

    @Override
    public List<ExchangeRateVO> getHistoryRates(ExchangeRateQueryDTO queryDTO) {
        QueryWrapper<ExchangeRateRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("currency_pair", queryDTO.getCurrencyPair());
        
        if (queryDTO.getStartDate() != null) {
            wrapper.ge("trade_date", queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le("trade_date", queryDTO.getEndDate());
        }
        
        wrapper.orderByDesc("record_time");
        wrapper.last("LIMIT " + queryDTO.getPageSize() + " OFFSET " + 
                    (queryDTO.getPageNum() - 1) * queryDTO.getPageSize());
        
        List<ExchangeRateRecord> records = exchangeRateRecordMapper.selectList(wrapper);
        return records.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<ExchangeRateVO> getRatesByDateRange(String currencyPair, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<ExchangeRateRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("currency_pair", currencyPair)
               .ge("trade_date", startDate)
               .le("trade_date", endDate)
               .orderByAsc("record_time");
        
        List<ExchangeRateRecord> records = exchangeRateRecordMapper.selectList(wrapper);
        return records.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public ExchangeRateVO getRateStats(String currencyPair, LocalDate tradeDate) {
        QueryWrapper<ExchangeRateRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("currency_pair", currencyPair)
               .eq("trade_date", tradeDate)
               .orderByAsc("record_time");
        
        List<ExchangeRateRecord> records = exchangeRateRecordMapper.selectList(wrapper);
        if (records.isEmpty()) {
            return null;
        }
        
        ExchangeRateVO stats = new ExchangeRateVO();
        stats.setCurrencyPair(currencyPair);
        stats.setTradeDate(tradeDate);
        
        // 计算统计数据
        BigDecimal maxRate = records.stream()
                .map(ExchangeRateRecord::getRate)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal minRate = records.stream()
                .map(ExchangeRateRecord::getRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal avgRate = records.stream()
                .map(ExchangeRateRecord::getRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(records.size()), 6, java.math.RoundingMode.HALF_UP);
        
        stats.setMaxRate(maxRate);
        stats.setMinRate(minRate);
        stats.setAvgRate(avgRate);
        stats.setOpenRate(records.get(0).getRate());
        stats.setCloseRate(records.get(records.size() - 1).getRate());
        
        return stats;
    }

    @Override
    @Transactional
    public void deleteByDate(String currencyPair, LocalDate tradeDate) {
        QueryWrapper<ExchangeRateRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("currency_pair", currencyPair)
               .eq("trade_date", tradeDate);
        
        int deleted = exchangeRateRecordMapper.delete(wrapper);
        log.info("删除汇率记录: {} 条, 货币对: {}, 日期: {}", deleted, currencyPair, tradeDate);
    }

    @Override
    @Transactional
    public ExchangeRateVO refreshRate(String currencyPair) {
        try {
            BigDecimal rate = tencentFinanceService.getExchangeRate(currencyPair);
            
            ExchangeRateRecord record = new ExchangeRateRecord();
            record.setCurrencyPair(currencyPair);
            record.setRate(rate);
            record.setRecordTime(LocalDateTime.now());
            record.setTradeDate(LocalDate.now());
            record.setDataSource("tencent");
            
            exchangeRateRecordMapper.insert(record);
            log.info("刷新汇率成功: {} = {}", currencyPair, rate);
            
            return convertToVO(record);
        } catch (Exception e) {
            log.error("刷新汇率失败: {}", e.getMessage(), e);
            throw new RuntimeException("刷新汇率失败: " + e.getMessage());
        }
    }

    /**
     * 转换为VO对象
     */
    private ExchangeRateVO convertToVO(ExchangeRateRecord record) {
        ExchangeRateVO vo = new ExchangeRateVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}