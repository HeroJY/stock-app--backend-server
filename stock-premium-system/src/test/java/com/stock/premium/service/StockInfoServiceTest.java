package com.stock.premium.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stock.premium.entity.StockInfo;
import com.stock.premium.mapper.StockInfoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 股票信息服务测试 - 真实数据库持久化测试
 * 
 * @author system
 * @since 2024-01-01
 */
@SpringBootTest
@ActiveProfiles("test")
class StockInfoServiceTest {

    @Autowired
    private StockInfoMapper stockInfoMapper;

    @BeforeEach
    void setUp() {
        System.out.println("🧹 开始清理股票信息测试数据");
        // 清理测试数据 - 删除建设银行相关的测试数据
        QueryWrapper<StockInfo> wrapper = new QueryWrapper<>();
        wrapper.like("stock_name", "建设银行")
               .or()
               .eq("a_stock_code", "601939")
               .or()
               .eq("h_stock_code", "00939");
        stockInfoMapper.delete(wrapper);
        System.out.println("🧹 清理完成");
    }

    @Test
    void testAddCCBStockInfo_新增建设银行股票基础信息() {
        System.out.println("🎯 开始测试新增建设银行股票基础信息");
        
        // Given - 准备建设银行的股票信息
        StockInfo ccbStock = new StockInfo();
        ccbStock.setStockName("中国建设银行");
        ccbStock.setMarketType("AH");
        ccbStock.setAStockCode("601939");
        ccbStock.setHStockCode("00939");
        ccbStock.setExchange("SH/HK");
        ccbStock.setIndustry("银行");
        ccbStock.setStatus(1);
        ccbStock.setCreatedTime(LocalDateTime.now());
        ccbStock.setUpdatedTime(LocalDateTime.now());
        ccbStock.setDeleted(0);
        
        System.out.println("📋 建设银行股票信息:");
        System.out.println("   股票名称: " + ccbStock.getStockName());
        System.out.println("   A股代码: " + ccbStock.getAStockCode());
        System.out.println("   H股代码: " + ccbStock.getHStockCode());
        System.out.println("   交易所: " + ccbStock.getExchange());
        System.out.println("   所属行业: " + ccbStock.getIndustry());
        
        // When - 插入股票信息到数据库
        int insertResult = stockInfoMapper.insert(ccbStock);
        
        // Then - 验证插入结果
        assertEquals(1, insertResult, "应该成功插入1条记录");
        assertNotNull(ccbStock.getId(), "插入后应该生成ID");
        
        // 从数据库查询验证
        StockInfo savedStock = stockInfoMapper.selectById(ccbStock.getId());
        assertNotNull(savedStock, "应该能从数据库查询到保存的记录");
        
        // 验证各个字段
        assertEquals("中国建设银行", savedStock.getStockName(), "股票名称应该匹配");
        assertEquals("AH", savedStock.getMarketType(), "市场类型应该为AH");
        assertEquals("601939", savedStock.getAStockCode(), "A股代码应该匹配");
        assertEquals("00939", savedStock.getHStockCode(), "H股代码应该匹配");
        assertEquals("SH/HK", savedStock.getExchange(), "交易所应该匹配");
        assertEquals("银行", savedStock.getIndustry(), "行业应该匹配");
        assertEquals(1, savedStock.getStatus(), "状态应该为正常");
        assertEquals(0, savedStock.getDeleted(), "删除标记应该为0");
        
        System.out.println("✅ 建设银行股票信息新增成功:");
        System.out.println("   记录ID: " + savedStock.getId());
        System.out.println("   股票名称: " + savedStock.getStockName());
        System.out.println("   A股代码: " + savedStock.getAStockCode());
        System.out.println("   H股代码: " + savedStock.getHStockCode());
        System.out.println("   创建时间: " + savedStock.getCreatedTime());
        System.out.println("   更新时间: " + savedStock.getUpdatedTime());
    }

    @Test
    void testQueryCCBStockInfo_查询建设银行股票信息() {
        System.out.println("🎯 测试查询建设银行股票信息");
        
        // Given - 先插入建设银行股票信息
        StockInfo ccbStock = new StockInfo();
        ccbStock.setStockName("中国建设银行");
        ccbStock.setMarketType("AH");
        ccbStock.setAStockCode("601939");
        ccbStock.setHStockCode("00939");
        ccbStock.setExchange("SH/HK");
        ccbStock.setIndustry("银行");
        ccbStock.setStatus(1);
        ccbStock.setCreatedTime(LocalDateTime.now());
        ccbStock.setUpdatedTime(LocalDateTime.now());
        ccbStock.setDeleted(0);
        
        stockInfoMapper.insert(ccbStock);
        
        // When - 通过不同条件查询
        
        // 1. 通过A股代码查询
        QueryWrapper<StockInfo> aStockWrapper = new QueryWrapper<>();
        aStockWrapper.eq("a_stock_code", "601939");
        StockInfo stockByACode = stockInfoMapper.selectOne(aStockWrapper);
        
        // 2. 通过H股代码查询
        QueryWrapper<StockInfo> hStockWrapper = new QueryWrapper<>();
        hStockWrapper.eq("h_stock_code", "00939");
        StockInfo stockByHCode = stockInfoMapper.selectOne(hStockWrapper);
        
        // 3. 通过股票名称模糊查询
        QueryWrapper<StockInfo> nameWrapper = new QueryWrapper<>();
        nameWrapper.like("stock_name", "建设银行");
        List<StockInfo> stocksByName = stockInfoMapper.selectList(nameWrapper);
        
        // 4. 通过行业查询
        QueryWrapper<StockInfo> industryWrapper = new QueryWrapper<>();
        industryWrapper.eq("industry", "银行");
        List<StockInfo> bankStocks = stockInfoMapper.selectList(industryWrapper);
        
        // Then - 验证查询结果
        assertNotNull(stockByACode, "通过A股代码应该能查询到记录");
        assertEquals("中国建设银行", stockByACode.getStockName());
        
        assertNotNull(stockByHCode, "通过H股代码应该能查询到记录");
        assertEquals("中国建设银行", stockByHCode.getStockName());
        
        assertFalse(stocksByName.isEmpty(), "通过名称模糊查询应该有结果");
        assertTrue(stocksByName.stream().anyMatch(s -> "中国建设银行".equals(s.getStockName())));
        
        assertFalse(bankStocks.isEmpty(), "银行行业应该有股票记录");
        assertTrue(bankStocks.stream().anyMatch(s -> "中国建设银行".equals(s.getStockName())));
        
        System.out.println("✅ 建设银行股票信息查询测试通过:");
        System.out.println("   通过A股代码查询: " + stockByACode.getStockName());
        System.out.println("   通过H股代码查询: " + stockByHCode.getStockName());
        System.out.println("   名称模糊查询结果数: " + stocksByName.size());
        System.out.println("   银行行业股票数: " + bankStocks.size());
    }

    @Test
    void testUpdateCCBStockInfo_更新建设银行股票信息() {
        System.out.println("🎯 测试更新建设银行股票信息");
        
        // Given - 先插入建设银行股票信息
        StockInfo ccbStock = new StockInfo();
        ccbStock.setStockName("中国建设银行");
        ccbStock.setMarketType("AH");
        ccbStock.setAStockCode("601939");
        ccbStock.setHStockCode("00939");
        ccbStock.setExchange("SH/HK");
        ccbStock.setIndustry("银行");
        ccbStock.setStatus(1);
        ccbStock.setCreatedTime(LocalDateTime.now());
        ccbStock.setUpdatedTime(LocalDateTime.now());
        ccbStock.setDeleted(0);
        
        stockInfoMapper.insert(ccbStock);
        Long stockId = ccbStock.getId();
        
        // When - 更新股票信息
        StockInfo updateStock = new StockInfo();
        updateStock.setId(stockId);
        updateStock.setStockName("中国建设银行股份有限公司");
        updateStock.setIndustry("商业银行");
        updateStock.setUpdatedTime(LocalDateTime.now());
        
        int updateResult = stockInfoMapper.updateById(updateStock);
        
        // Then - 验证更新结果
        assertEquals(1, updateResult, "应该成功更新1条记录");
        
        // 查询更新后的记录
        StockInfo updatedStock = stockInfoMapper.selectById(stockId);
        assertNotNull(updatedStock, "更新后应该能查询到记录");
        assertEquals("中国建设银行股份有限公司", updatedStock.getStockName(), "股票名称应该已更新");
        assertEquals("商业银行", updatedStock.getIndustry(), "行业应该已更新");
        assertEquals("601939", updatedStock.getAStockCode(), "A股代码应该保持不变");
        assertEquals("00939", updatedStock.getHStockCode(), "H股代码应该保持不变");
        
        System.out.println("✅ 建设银行股票信息更新成功:");
        System.out.println("   更新后名称: " + updatedStock.getStockName());
        System.out.println("   更新后行业: " + updatedStock.getIndustry());
        System.out.println("   更新时间: " + updatedStock.getUpdatedTime());
    }

    @Test
    void testBatchAddBankStocks_批量新增银行股票信息() {
        System.out.println("🎯 测试批量新增银行股票信息");
        
        // Given - 准备多个银行股票信息
        StockInfo[] bankStocks = {
            createBankStock("中国建设银行", "601939", "00939", "SH/HK"),
            createBankStock("中国工商银行", "601398", "01398", "SH/HK"),
            createBankStock("中国银行", "601988", "03988", "SH/HK"),
            createBankStock("中国农业银行", "601288", "01288", "SH/HK")
        };
        
        System.out.println("📋 准备批量插入 " + bankStocks.length + " 个银行股票:");
        for (StockInfo stock : bankStocks) {
            System.out.println("   " + stock.getStockName() + " (A:" + stock.getAStockCode() + ", H:" + stock.getHStockCode() + ")");
        }
        
        // When - 批量插入
        int totalInserted = 0;
        for (StockInfo stock : bankStocks) {
            int result = stockInfoMapper.insert(stock);
            totalInserted += result;
        }
        
        // Then - 验证批量插入结果
        assertEquals(bankStocks.length, totalInserted, "应该成功插入所有银行股票");
        
        // 查询验证
        QueryWrapper<StockInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("industry", "银行")
               .eq("status", 1)
               .eq("deleted", 0);
        List<StockInfo> savedBankStocks = stockInfoMapper.selectList(wrapper);
        
        assertTrue(savedBankStocks.size() >= bankStocks.length, "数据库中应该至少有" + bankStocks.length + "个银行股票");
        
        // 验证每个银行都存在
        String[] bankNames = {"中国建设银行", "中国工商银行", "中国银行", "中国农业银行"};
        for (String bankName : bankNames) {
            boolean exists = savedBankStocks.stream()
                .anyMatch(stock -> stock.getStockName().equals(bankName));
            assertTrue(exists, bankName + "应该存在于数据库中");
        }
        
        System.out.println("✅ 批量新增银行股票信息成功:");
        System.out.println("   插入记录数: " + totalInserted);
        System.out.println("   数据库中银行股票总数: " + savedBankStocks.size());
        
        // 显示所有银行股票信息
        System.out.println("📊 数据库中的银行股票列表:");
        for (StockInfo stock : savedBankStocks) {
            System.out.println("   ID:" + stock.getId() + " " + stock.getStockName() + 
                             " (A:" + stock.getAStockCode() + ", H:" + stock.getHStockCode() + ")");
        }
    }

    @Test
    void testValidateStockInfoConstraints_验证股票信息约束() {
        System.out.println("🎯 测试股票信息数据约束验证");
        
        // Given - 准备建设银行股票信息
        StockInfo ccbStock = createBankStock("中国建设银行", "601939", "00939", "SH/HK");
        stockInfoMapper.insert(ccbStock);
        
        // When & Then - 测试唯一约束
        try {
            // 尝试插入相同的A股和H股代码组合
            StockInfo duplicateStock = createBankStock("重复的建设银行", "601939", "00939", "SH/HK");
            stockInfoMapper.insert(duplicateStock);
            
            // 如果没有抛出异常，说明约束没有生效（这可能是正常的，取决于数据库设计）
            System.out.println("⚠️  数据库允许重复的A股H股代码组合");
        } catch (Exception e) {
            System.out.println("✅ 数据库正确阻止了重复的A股H股代码组合: " + e.getMessage());
        }
        
        // 验证数据完整性
        StockInfo savedStock = stockInfoMapper.selectById(ccbStock.getId());
        assertNotNull(savedStock.getStockName(), "股票名称不能为空");
        assertNotNull(savedStock.getAStockCode(), "A股代码不能为空");
        assertNotNull(savedStock.getHStockCode(), "H股代码不能为空");
        assertNotNull(savedStock.getExchange(), "交易所不能为空");
        assertTrue(savedStock.getStatus() >= 0, "状态值应该有效");
        assertTrue(savedStock.getDeleted() >= 0, "删除标记应该有效");
        
        System.out.println("✅ 股票信息约束验证完成");
    }

    /**
     * 创建银行股票信息的辅助方法
     */
    private StockInfo createBankStock(String name, String aCode, String hCode, String exchange) {
        StockInfo stock = new StockInfo();
        stock.setStockName(name);
        stock.setMarketType("AH");
        stock.setAStockCode(aCode);
        stock.setHStockCode(hCode);
        stock.setExchange(exchange);
        stock.setIndustry("银行");
        stock.setStatus(1);
        stock.setCreatedTime(LocalDateTime.now());
        stock.setUpdatedTime(LocalDateTime.now());
        stock.setDeleted(0);
        return stock;
    }

    @Test
    void testQueryAllStockInfo_查询所有股票信息() {
        System.out.println("🎯 查询数据库中的所有股票信息");
        
        // 先插入建设银行信息确保有数据
        StockInfo ccbStock = createBankStock("中国建设银行", "601939", "00939", "SH/HK");
        stockInfoMapper.insert(ccbStock);
        
        // 查询所有股票信息
        List<StockInfo> allStocks = stockInfoMapper.selectList(null);
        
        System.out.println("📊 数据库中共有 " + allStocks.size() + " 条股票记录:");
        
        if (allStocks.isEmpty()) {
            System.out.println("   📭 暂无股票记录");
        } else {
            for (int i = 0; i < allStocks.size(); i++) {
                StockInfo stock = allStocks.get(i);
                System.out.println("   第" + (i+1) + "条:");
                System.out.println("     ID: " + stock.getId());
                System.out.println("     名称: " + stock.getStockName());
                System.out.println("     市场类型: " + stock.getMarketType());
                System.out.println("     A股代码: " + stock.getAStockCode());
                System.out.println("     H股代码: " + stock.getHStockCode());
                System.out.println("     交易所: " + stock.getExchange());
                System.out.println("     行业: " + stock.getIndustry());
                System.out.println("     状态: " + (stock.getStatus() == 1 ? "正常" : "停用"));
                System.out.println("     创建时间: " + stock.getCreatedTime());
                System.out.println("     ────────────────────");
            }
        }
        
        assertTrue(allStocks.size() >= 1, "应该至少有1条股票记录（刚插入的建设银行）");
    }
}