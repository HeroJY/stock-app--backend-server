package com.stock.premium.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stock.premium.dto.ExchangeRateQueryDTO;
import com.stock.premium.entity.ExchangeRateRecord;
import com.stock.premium.mapper.ExchangeRateRecordMapper;
import com.stock.premium.service.ExchangeRateService;
import com.stock.premium.vo.ExchangeRateSimpleVO;
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

    @Override
    public ExchangeRateSimpleVO getLatestRate(String currencyPair) {
        ExchangeRateRecord record = exchangeRateRecordMapper.selectLatest();
        if (record == null) {
            // 如果没有记录，抛出异常提示需要手动输入汇率信息
            throw new RuntimeException("暂无汇率数据，请先手动输入汇率信息");
        }
        return convertToSimpleVO(record);
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
        record.setCreatedTime(LocalDateTime.now());  // 设置创建时间戳
        
        exchangeRateRecordMapper.insert(record);
        log.info("手动更新汇率: {} = {}", currencyPair, rate);
    }


    @Override
    public List<ExchangeRateSimpleVO> getHistoryRates(ExchangeRateQueryDTO queryDTO) {
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
        if (records.isEmpty()) {
            throw new RuntimeException("没查询到汇率设置，请手动设置汇率信息");
        }
        return records.stream().map(this::convertToSimpleVO).collect(Collectors.toList());
    }

    @Override
    public List<ExchangeRateSimpleVO> getRatesByDateRange(String currencyPair, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<ExchangeRateRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("currency_pair", currencyPair)
               .ge("trade_date", startDate)
               .le("trade_date", endDate)
               .orderByAsc("record_time");
        
        List<ExchangeRateRecord> records = exchangeRateRecordMapper.selectList(wrapper);
        if (records.isEmpty()) {
            throw new RuntimeException("没查询到汇率设置，请手动设置汇率信息");
        }
        return records.stream().map(this::convertToSimpleVO).collect(Collectors.toList());
    }

    @Override
    public ExchangeRateSimpleVO getRateStats(String currencyPair, LocalDate tradeDate) {
        QueryWrapper<ExchangeRateRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("currency_pair", currencyPair)
               .eq("trade_date", tradeDate)
               .orderByAsc("record_time");
        
        List<ExchangeRateRecord> records = exchangeRateRecordMapper.selectList(wrapper);
        if (records.isEmpty()) {
            throw new RuntimeException("没查询到汇率设置，请手动设置汇率信息");
        }
        
        // 对于统计信息，返回最新的一条记录作为代表
        ExchangeRateRecord latestRecord = records.get(records.size() - 1);
        return convertToSimpleVO(latestRecord);
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

    /**
     * 转换为简单VO对象（只包含汇率信息）
     */
    private ExchangeRateSimpleVO convertToSimpleVO(ExchangeRateRecord record) {
        ExchangeRateSimpleVO vo = new ExchangeRateSimpleVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}