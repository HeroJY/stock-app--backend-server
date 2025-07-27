package com.stock.premium.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stock.premium.dto.ExchangeRateQueryDTO;
import com.stock.premium.entity.ExchangeRateRecord;
import com.stock.premium.mapper.ExchangeRateRecordMapper;
import com.stock.premium.vo.ExchangeRateSimpleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 汇率服务测试 - 真实数据库持久化测试
 * 
 * @author system
 * @since 2024-01-01
 */
@SpringBootTest
@ActiveProfiles("test")
class ExchangeRateServiceTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeRateRecordMapper exchangeRateRecordMapper;

    @BeforeEach
    void setUp() {
        System.out.println("🧹 开始清理测试数据");
        // 清理测试数据，只删除manual数据源的记录
        QueryWrapper<ExchangeRateRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("data_source", "manual");
        exchangeRateRecordMapper.delete(wrapper);
        System.out.println("🧹 清理测试数据完成");
    }

    @Test
    void testUpdateRate_手动更新汇率() {
        System.out.println("🎯 测试手动更新汇率");
        
        // Given
        String currencyPair = "HKDCNY";
        BigDecimal rate = new BigDecimal("0.912");
        
        // When
        exchangeRateService.updateRate(currencyPair, rate);
        
        // Then - 从数据库验证
        QueryWrapper<ExchangeRateRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("currency_pair", currencyPair)
               .eq("data_source", "manual")
               .orderByDesc("record_time")
               .last("LIMIT 1");
        
        ExchangeRateRecord record = exchangeRateRecordMapper.selectOne(wrapper);
        
        assertNotNull(record, "应该找到汇率记录");
        assertEquals(currencyPair, record.getCurrencyPair());
        assertEquals(0, rate.compareTo(record.getRate()));
        assertEquals("manual", record.getDataSource());
        assertEquals(LocalDate.now(), record.getTradeDate());
        
        System.out.println("✅ 手动更新汇率测试通过");
        System.out.println("   汇率: " + record.getRate());
        System.out.println("   记录时间: " + record.getRecordTime());
    }

    @Test
    void testGetLatestRate_获取最新汇率() {
        System.out.println("🎯 测试获取最新汇率");
        
        // Given - 先插入几条汇率记录
        String currencyPair = "HKDCNY";
        BigDecimal oldRate = new BigDecimal("0.910");
        BigDecimal newRate = new BigDecimal("0.912");
        
        // 插入旧汇率
        exchangeRateService.updateRate(currencyPair, oldRate);
        
        // 等待一秒确保时间差异
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 插入新汇率
        exchangeRateService.updateRate(currencyPair, newRate);
        
        // When
        ExchangeRateSimpleVO latestRate = exchangeRateService.getLatestRate(currencyPair);
        
        // Then
        assertNotNull(latestRate, "应该获取到最新汇率");
        assertEquals(currencyPair, latestRate.getCurrencyPair());
        assertEquals(0, newRate.compareTo(latestRate.getRate()), "应该是最新的汇率");
        
        System.out.println("✅ 获取最新汇率测试通过");
        System.out.println("   最新汇率: " + latestRate.getRate());
        System.out.println("   记录时间: " + latestRate.getRecordTime());
    }

    @Test
    void testGetHistoryRates_查询历史汇率() {
        System.out.println("🎯 测试查询历史汇率");
        
        // Given - 插入多条历史汇率
        String currencyPair = "HKDCNY";
        for (int i = 0; i < 5; i++) {
            BigDecimal rate = new BigDecimal("0.91" + i);
            exchangeRateService.updateRate(currencyPair, rate);
            
            try {
                Thread.sleep(100); // 确保时间差异
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // When
        ExchangeRateQueryDTO queryDTO = new ExchangeRateQueryDTO();
        queryDTO.setCurrencyPair(currencyPair);
        queryDTO.setPageSize(10);
        queryDTO.setPageNum(1);
        
        List<ExchangeRateSimpleVO> historyRates = exchangeRateService.getHistoryRates(queryDTO);
        
        // Then
        assertNotNull(historyRates, "历史汇率不应为空");
        assertEquals(5, historyRates.size(), "应该返回5条历史记录");
        
        System.out.println("✅ 查询历史汇率测试通过");
        System.out.println("   历史记录数: " + historyRates.size());
    }

    @Test
    void testGetRatesByDateRange_按日期范围查询() {
        System.out.println("🎯 测试按日期范围查询汇率");
        
        // Given
        String currencyPair = "HKDCNY";
        LocalDate today = LocalDate.now();
        
        // 插入今天的汇率
        exchangeRateService.updateRate(currencyPair, new BigDecimal("0.912"));
        
        // When
        List<ExchangeRateSimpleVO> rates = exchangeRateService.getRatesByDateRange(
            currencyPair, today, today);
        
        // Then
        assertNotNull(rates, "查询结果不应为空");
        assertTrue(rates.size() >= 1, "应该至少有1条今天的记录");
        
        System.out.println("✅ 按日期范围查询测试通过");
        System.out.println("   查询到记录数: " + rates.size());
    }

    @Test
    void testDeleteByDate_按日期删除汇率() {
        System.out.println("🎯 测试按日期删除汇率");
        
        // Given
        String currencyPair = "HKDCNY";
        LocalDate today = LocalDate.now();
        
        // 插入今天的汇率
        exchangeRateService.updateRate(currencyPair, new BigDecimal("0.912"));
        
        // 验证插入成功
        QueryWrapper<ExchangeRateRecord> beforeWrapper = new QueryWrapper<>();
        beforeWrapper.eq("currency_pair", currencyPair)
                    .eq("trade_date", today)
                    .eq("data_source", "manual");
        int beforeCount = Math.toIntExact(exchangeRateRecordMapper.selectCount(beforeWrapper));
        assertTrue(beforeCount > 0, "删除前应该有记录");
        
        // When
        exchangeRateService.deleteByDate(currencyPair, today);
        
        // Then
        QueryWrapper<ExchangeRateRecord> afterWrapper = new QueryWrapper<>();
        afterWrapper.eq("currency_pair", currencyPair)
                   .eq("trade_date", today)
                   .eq("data_source", "manual");
        int afterCount = Math.toIntExact(exchangeRateRecordMapper.selectCount(afterWrapper));
        
        assertEquals(0, afterCount, "删除后应该没有记录");
        
        System.out.println("✅ 按日期删除汇率测试通过");
        System.out.println("   删除前记录数: " + beforeCount);
        System.out.println("   删除后记录数: " + afterCount);
    }

    @Test
    void testRateCalculation_汇率计算验证() {
        System.out.println("🎯 测试汇率计算验证");
        
        // Given
        BigDecimal hkdAmount = new BigDecimal("1000.00");
        BigDecimal exchangeRate = new BigDecimal("0.912");
        
        // When
        BigDecimal cnyAmount = hkdAmount.multiply(exchangeRate);
        
        // Then
        BigDecimal expectedAmount = new BigDecimal("912.00");
        assertEquals(0, expectedAmount.compareTo(cnyAmount), 
                    "1000港币按0.912汇率应兑换912人民币");
        
        System.out.println("✅ 汇率计算验证通过");
        System.out.println("   港币金额: " + hkdAmount);
        System.out.println("   汇率: " + exchangeRate);
        System.out.println("   人民币金额: " + cnyAmount);
    }

    @Test
    void testGetRateStats_获取汇率统计() {
        System.out.println("🎯 测试获取汇率统计");
        
        // Given
        String currencyPair = "HKDCNY";
        LocalDate today = LocalDate.now();
        
        // 插入多个汇率用于统计
        BigDecimal[] rates = {
            new BigDecimal("0.910"), // 最低
            new BigDecimal("0.912"), // 中间
            new BigDecimal("0.915")  // 最高
        };
        
        for (BigDecimal rate : rates) {
            exchangeRateService.updateRate(currencyPair, rate);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // When
        ExchangeRateSimpleVO stats = exchangeRateService.getRateStats(currencyPair, today);
        
        // Then
        assertNotNull(stats, "统计结果不应为空");
        assertEquals(currencyPair, stats.getCurrencyPair());
        
        System.out.println("✅ 获取汇率统计测试通过");
        System.out.println("   统计日期: " + today);
        System.out.println("   货币对: " + stats.getCurrencyPair());
    }
}