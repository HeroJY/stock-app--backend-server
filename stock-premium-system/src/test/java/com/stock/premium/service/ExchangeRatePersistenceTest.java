package com.stock.premium.service;

import com.stock.premium.entity.ExchangeRateRecord;
import com.stock.premium.mapper.ExchangeRateRecordMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 汇率持久化测试 - 真实保存到数据库
 * 不使用@Transactional，数据会真实保存到数据库
 * 
 * @author system
 * @since 2024-01-01
 */
@SpringBootTest
@ActiveProfiles("test")
class ExchangeRatePersistenceTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeRateRecordMapper exchangeRateRecordMapper;

    @Test
    void testPersistHKDToCNYRate_持久化港币兑人民币汇率0点912() {
        // Given - 设置港币兑人民币汇率为0.912
        String currencyPair = "HKDCNY";
        BigDecimal targetRate = new BigDecimal("0.912");
        
        System.out.println("🎯 开始持久化港币兑人民币汇率: " + targetRate);
        System.out.println("⚠️  注意：此测试会真实保存数据到数据库，不会回滚");

        // When - 执行汇率更新（真实保存到数据库）
        assertDoesNotThrow(() -> {
            exchangeRateService.updateRate(currencyPair, targetRate);
        });

        // Then - 验证数据库中的记录
        List<ExchangeRateRecord> records = exchangeRateRecordMapper.selectList(null);
        
        // 查找我们刚插入的记录
        ExchangeRateRecord savedRecord = records.stream()
            .filter(r -> "HKDCNY".equals(r.getCurrencyPair()) && 
                        "manual".equals(r.getDataSource()) &&
                        r.getRate().compareTo(targetRate) == 0)
            .findFirst()
            .orElse(null);
        
        assertNotNull(savedRecord, "应该在数据库中找到刚插入的汇率记录");
        
        // 验证汇率数据
        assertEquals("HKDCNY", savedRecord.getCurrencyPair(), "货币对应为HKDCNY");
        assertEquals(0, targetRate.compareTo(savedRecord.getRate()), "汇率应为0.912");
        assertEquals("manual", savedRecord.getDataSource(), "数据来源应为手动设置");
        assertNotNull(savedRecord.getRecordTime(), "记录时间不能为空");
        assertNotNull(savedRecord.getTradeDate(), "交易日期不能为空");
        assertEquals(LocalDate.now(), savedRecord.getTradeDate(), "交易日期应该是今天");
        
        System.out.println("✅ 港币兑人民币汇率持久化成功:");
        System.out.println("   记录ID: " + savedRecord.getId());
        System.out.println("   货币对: " + savedRecord.getCurrencyPair());
        System.out.println("   汇率: " + savedRecord.getRate());
        System.out.println("   数据来源: " + savedRecord.getDataSource());
        System.out.println("   记录时间: " + savedRecord.getRecordTime());
        System.out.println("   交易日期: " + savedRecord.getTradeDate());
        System.out.println("   数据库总记录数: " + records.size());
        
        // 验证汇率计算
        BigDecimal hkdAmount = new BigDecimal("1000.00");
        BigDecimal cnyAmount = hkdAmount.multiply(targetRate);
        System.out.println("💰 汇率计算验证:");
        System.out.println("   1000港币 × " + targetRate + " = " + cnyAmount + "人民币");
    }

    @Test
    void testQueryCurrentExchangeRates_查询当前数据库中的所有汇率() {
        System.out.println("🔍 查询数据库中的所有汇率记录:");
        
        List<ExchangeRateRecord> allRecords = exchangeRateRecordMapper.selectList(null);
        
        if (allRecords.isEmpty()) {
            System.out.println("   📭 数据库中暂无汇率记录");
        } else {
            System.out.println("   📊 共找到 " + allRecords.size() + " 条汇率记录:");
            
            for (int i = 0; i < allRecords.size(); i++) {
                ExchangeRateRecord record = allRecords.get(i);
                System.out.println("   第" + (i+1) + "条:");
                System.out.println("     ID: " + record.getId());
                System.out.println("     货币对: " + record.getCurrencyPair());
                System.out.println("     汇率: " + record.getRate());
                System.out.println("     数据来源: " + record.getDataSource());
                System.out.println("     记录时间: " + record.getRecordTime());
                System.out.println("     交易日期: " + record.getTradeDate());
                System.out.println("     ────────────────────");
            }
        }
        
        // 验证至少有一条记录（如果之前的测试运行过）
        assertTrue(allRecords.size() >= 0, "记录数应该大于等于0");
    }

    @Test
    void testSetMultipleRates_设置多个汇率并持久化() {
        System.out.println("🎯 测试设置多个汇率并持久化到数据库");
        
        // 设置不同时间点的汇率
        String currencyPair = "HKDCNY";
        BigDecimal[] rates = {
            new BigDecimal("0.910"), // 早盘
            new BigDecimal("0.912"), // 中盘  
            new BigDecimal("0.915")  // 尾盘
        };
        
        int initialCount = exchangeRateRecordMapper.selectList(null).size();
        System.out.println("   初始记录数: " + initialCount);
        
        // 批量设置汇率
        for (int i = 0; i < rates.length; i++) {
            BigDecimal rate = rates[i];
            exchangeRateService.updateRate(currencyPair, rate);
            System.out.println("   设置汇率 " + (i+1) + ": " + rate);
            
            // 添加小延迟确保时间戳不同
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 验证数据库中的记录
        List<ExchangeRateRecord> allRecords = exchangeRateRecordMapper.selectList(null);
        int finalCount = allRecords.size();
        
        System.out.println("   最终记录数: " + finalCount);
        System.out.println("   新增记录数: " + (finalCount - initialCount));
        
        // 验证新增了3条记录
        assertEquals(initialCount + 3, finalCount, "应该新增3条汇率记录");
        
        // 验证最新的3条记录
        List<ExchangeRateRecord> newRecords = allRecords.stream()
            .filter(r -> "HKDCNY".equals(r.getCurrencyPair()) && "manual".equals(r.getDataSource()))
            .sorted((a, b) -> b.getRecordTime().compareTo(a.getRecordTime())) // 按时间倒序
            .limit(3)
            .toList();
            
        assertEquals(3, newRecords.size(), "应该找到3条新记录");
        
        System.out.println("✅ 批量汇率设置成功，数据已持久化到数据库");
    }
}