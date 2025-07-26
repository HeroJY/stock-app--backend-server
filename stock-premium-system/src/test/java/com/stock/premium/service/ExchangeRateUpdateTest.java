package com.stock.premium.service;

import com.stock.premium.entity.ExchangeRateRecord;
import com.stock.premium.mapper.ExchangeRateRecordMapper;
import com.stock.premium.vo.ExchangeRateVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 汇率更新专项测试 - 真实数据库连接测试
 * 
 * @author system
 * @since 2024-01-01
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExchangeRateUpdateTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeRateRecordMapper exchangeRateRecordMapper;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        exchangeRateRecordMapper.delete(null);
        System.out.println("🧹 清理测试数据完成");
    }

    @AfterEach
    void tearDown() {
        // 测试后清理
        exchangeRateRecordMapper.delete(null);
        System.out.println("🧹 测试后清理完成");
    }

    @Test
    void testSetHKDToCNYRate_设置港币兑人民币汇率0点912() {
        // Given - 设置港币兑人民币汇率为0.912
        String currencyPair = "HKDCNY";
        BigDecimal targetRate = new BigDecimal("0.912");
        
        System.out.println("🎯 开始测试设置港币兑人民币汇率: " + targetRate);

        // When - 执行汇率更新
        assertDoesNotThrow(() -> {
            exchangeRateService.updateRate(currencyPair, targetRate);
        });

        // Then - 从数据库验证汇率设置
        List<ExchangeRateRecord> records = exchangeRateRecordMapper.selectList(null);
        assertEquals(1, records.size(), "应该插入1条汇率记录");
        
        ExchangeRateRecord savedRecord = records.get(0);
        
        // 验证汇率数据
        assertEquals("HKDCNY", savedRecord.getCurrencyPair(), "货币对应为HKDCNY");
        assertEquals(0, targetRate.compareTo(savedRecord.getRate()), "汇率应为0.912");
        assertEquals("manual", savedRecord.getDataSource(), "数据来源应为手动设置");
        assertNotNull(savedRecord.getRecordTime(), "记录时间不能为空");
        assertNotNull(savedRecord.getTradeDate(), "交易日期不能为空");
        
        // 验证时间设置合理性
        assertTrue(savedRecord.getRecordTime().isBefore(LocalDateTime.now().plusMinutes(1)), 
                  "记录时间应该是当前时间");
        assertEquals(LocalDate.now(), savedRecord.getTradeDate(), 
                    "交易日期应该是今天");
        
        System.out.println("✅ 港币兑人民币汇率设置成功:");
        System.out.println("   记录ID: " + savedRecord.getId());
        System.out.println("   货币对: " + savedRecord.getCurrencyPair());
        System.out.println("   汇率: " + savedRecord.getRate());
        System.out.println("   数据来源: " + savedRecord.getDataSource());
        System.out.println("   记录时间: " + savedRecord.getRecordTime());
        System.out.println("   交易日期: " + savedRecord.getTradeDate());
    }

    @Test
    void testBatchSetMultipleRates_批量设置不同时间点的汇率() {
        // Given - 模拟一天内不同时间点的汇率变化
        String currencyPair = "HKDCNY";
        BigDecimal[] rates = {
            new BigDecimal("0.910"), // 开盘
            new BigDecimal("0.912"), // 中午
            new BigDecimal("0.911")  // 收盘
        };
        
        System.out.println("🎯 开始批量设置汇率:");

        // When - 批量设置汇率
        for (int i = 0; i < rates.length; i++) {
            BigDecimal rate = rates[i];
            assertDoesNotThrow(() -> {
                exchangeRateService.updateRate(currencyPair, rate);
            });
            System.out.println("   第" + (i+1) + "次设置: " + rate);
        }

        // Then - 验证所有汇率都被正确设置
        List<ExchangeRateRecord> records = exchangeRateRecordMapper.selectList(null);
        assertEquals(3, records.size(), "应该插入3条汇率记录");
        
        // 验证每条记录
        for (int i = 0; i < records.size(); i++) {
            ExchangeRateRecord record = records.get(i);
            assertEquals("HKDCNY", record.getCurrencyPair());
            assertEquals("manual", record.getDataSource());
            assertNotNull(record.getRate());
        }
        
        System.out.println("✅ 批量汇率设置成功，共插入 " + records.size() + " 条记录");
    }

    @Test
    void testGetLatestRateAfterUpdate_更新后获取最新汇率() {
        // Given - 先设置汇率，然后获取
        String currencyPair = "HKDCNY";
        BigDecimal setRate = new BigDecimal("0.912");
        
        System.out.println("🎯 测试设置汇率后获取最新汇率");

        // When - 先设置汇率，再获取最新汇率
        exchangeRateService.updateRate(currencyPair, setRate);
        ExchangeRateVO latestRate = exchangeRateService.getLatestRate(currencyPair);

        // Then - 验证获取的汇率正确
        assertNotNull(latestRate, "应该能获取到最新汇率");
        assertEquals(currencyPair, latestRate.getCurrencyPair(), "货币对应该匹配");
        assertEquals(0, setRate.compareTo(latestRate.getRate()), "汇率应该是刚设置的0.912");
        assertEquals("manual", latestRate.getDataSource(), "数据来源应该是手动设置");
        
        System.out.println("✅ 汇率设置和获取验证成功:");
        System.out.println("   设置汇率: " + setRate);
        System.out.println("   获取汇率: " + latestRate.getRate());
        System.out.println("   数据来源: " + latestRate.getDataSource());
        System.out.println("   记录时间: " + latestRate.getRecordTime());
    }

    @Test
    void testVerifyRateCalculation_验证汇率计算精度() {
        // Given - 测试汇率计算的精度
        BigDecimal hkdAmount = new BigDecimal("1000.00"); // 1000港币
        BigDecimal exchangeRate = new BigDecimal("0.912"); // 汇率0.912
        
        System.out.println("🎯 验证汇率计算精度");

        // When - 计算兑换后的人民币金额
        BigDecimal cnyAmount = hkdAmount.multiply(exchangeRate);
        
        // Then - 验证计算结果
        BigDecimal expectedAmount = new BigDecimal("912.00");
        assertEquals(0, expectedAmount.compareTo(cnyAmount), 
                    "1000港币按0.912汇率应兑换912人民币");
        
        System.out.println("✅ 汇率计算验证:");
        System.out.println("   港币金额: " + hkdAmount);
        System.out.println("   汇率: " + exchangeRate);
        System.out.println("   人民币金额: " + cnyAmount);
        System.out.println("   计算公式: " + hkdAmount + " × " + exchangeRate + " = " + cnyAmount);
    }

    @Test
    void testRateValidation_汇率数据验证() {
        // Given - 测试各种汇率值的有效性
        String currencyPair = "HKDCNY";
        BigDecimal validRate = new BigDecimal("0.912");
        
        System.out.println("🎯 验证汇率数据有效性");

        // When & Then - 验证正常汇率设置
        assertDoesNotThrow(() -> {
            exchangeRateService.updateRate(currencyPair, validRate);
        }, "正常汇率0.912应该设置成功");

        // 验证数据库中的记录
        List<ExchangeRateRecord> records = exchangeRateRecordMapper.selectList(null);
        assertEquals(1, records.size(), "应该有1条记录");
        
        ExchangeRateRecord record = records.get(0);
        assertEquals(0, validRate.compareTo(record.getRate()), "数据库中的汇率应该是0.912");

        // 验证汇率范围合理性（港币兑人民币通常在0.8-1.2之间）
        assertTrue(validRate.compareTo(new BigDecimal("0.5")) > 0, "汇率应该大于0.5");
        assertTrue(validRate.compareTo(new BigDecimal("1.5")) < 0, "汇率应该小于1.5");
        
        System.out.println("✅ 汇率数据验证通过:");
        System.out.println("   设置汇率: " + validRate);
        System.out.println("   数据库汇率: " + record.getRate());
        System.out.println("   汇率范围: 0.5 < " + validRate + " < 1.5 ✓");
    }

    @Test
    void testDatabasePersistence_数据库持久化验证() {
        // Given
        String currencyPair = "HKDCNY";
        BigDecimal rate1 = new BigDecimal("0.912");
        BigDecimal rate2 = new BigDecimal("0.915");
        
        System.out.println("🎯 验证数据库持久化");

        // When - 设置两个不同的汇率
        exchangeRateService.updateRate(currencyPair, rate1);
        exchangeRateService.updateRate(currencyPair, rate2);

        // Then - 验证数据库中有两条记录
        List<ExchangeRateRecord> records = exchangeRateRecordMapper.selectList(null);
        assertEquals(2, records.size(), "应该有2条汇率记录");
        
        // 验证记录内容
        boolean hasRate1 = records.stream().anyMatch(r -> r.getRate().compareTo(rate1) == 0);
        boolean hasRate2 = records.stream().anyMatch(r -> r.getRate().compareTo(rate2) == 0);
        
        assertTrue(hasRate1, "应该包含汇率0.912的记录");
        assertTrue(hasRate2, "应该包含汇率0.915的记录");
        
        System.out.println("✅ 数据库持久化验证成功:");
        System.out.println("   总记录数: " + records.size());
        System.out.println("   汇率1: " + rate1 + " ✓");
        System.out.println("   汇率2: " + rate2 + " ✓");
    }

    @Test
    void testGetLatestRate_获取最新汇率() {
        // Given - 设置多个汇率，最后一个应该是最新的
        String currencyPair = "HKDCNY";
        BigDecimal oldRate = new BigDecimal("0.910");
        BigDecimal newRate = new BigDecimal("0.912");
        
        System.out.println("🎯 测试获取最新汇率功能");

        // When
        exchangeRateService.updateRate(currencyPair, oldRate);
        try {
            Thread.sleep(1000); // 确保时间差异
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        exchangeRateService.updateRate(currencyPair, newRate);
        
        ExchangeRateVO latestRate = exchangeRateService.getLatestRate(currencyPair);

        // Then
        assertNotNull(latestRate, "应该能获取到最新汇率");
        assertEquals(0, newRate.compareTo(latestRate.getRate()), "最新汇率应该是0.912");
        
        System.out.println("✅ 获取最新汇率成功:");
        System.out.println("   旧汇率: " + oldRate);
        System.out.println("   新汇率: " + newRate);
        System.out.println("   获取到的最新汇率: " + latestRate.getRate());
    }
}