package com.stock.premium.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.premium.entity.PremiumRateRecord;
import com.stock.premium.mapper.PremiumRateRecordMapper;
import com.stock.premium.service.PremiumRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 溢价率服务实现类
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Service
public class PremiumRateServiceImpl extends ServiceImpl<PremiumRateRecordMapper, PremiumRateRecord> implements PremiumRateService {

    @Override
    public BigDecimal calculatePremiumRate(BigDecimal aStockPrice, BigDecimal hStockPrice, BigDecimal exchangeRate) {
        if (aStockPrice == null || hStockPrice == null || exchangeRate == null ||
            aStockPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        try {
            // 溢价率公式：(H股价格*汇率 - A股价格) / A股价格 × 100%
            BigDecimal hStockPriceInCny = hStockPrice.multiply(exchangeRate);
            BigDecimal premiumRate = hStockPriceInCny.subtract(aStockPrice)
                    .divide(aStockPrice, 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            
            return premiumRate.setScale(4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("计算溢价率失败: aPrice={}, hPrice={}, rate={}", aStockPrice, hStockPrice, exchangeRate, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public PremiumRateRecord recordPremiumRate(String stockCode, BigDecimal aStockPrice, 
                                             BigDecimal hStockPrice, BigDecimal exchangeRate) {
        try {
            BigDecimal premiumRate = calculatePremiumRate(aStockPrice, hStockPrice, exchangeRate);
            
            PremiumRateRecord record = new PremiumRateRecord();
            record.setStockCode(stockCode);
            record.setAStockPrice(aStockPrice);
            record.setHStockPrice(hStockPrice);
            record.setExchangeRate(exchangeRate);
            record.setPremiumRate(premiumRate);
            record.setRecordTime(LocalDateTime.now());
            record.setTradeDate(LocalDate.now());
            
            this.save(record);
            log.debug("记录溢价率数据: stockCode={}, premiumRate={}%", stockCode, premiumRate);
            
            return record;
        } catch (Exception e) {
            log.error("记录溢价率数据失败: stockCode={}", stockCode, e);
            return null;
        }
    }

    @Override
    public List<PremiumRateRecord> getPremiumRatesByStockAndDate(String stockCode, LocalDate tradeDate) {
        return baseMapper.selectByStockAndDate(stockCode, tradeDate);
    }

    @Override
    public List<PremiumRateRecord> getLatestPremiumRatesByDate(LocalDate tradeDate) {
        return baseMapper.selectLatestByDate(tradeDate);
    }
}