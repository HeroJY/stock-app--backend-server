package com.stock.premium.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.premium.entity.StockInfo;
import com.stock.premium.mapper.StockInfoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 股票基础信息查询测试
 * 
 * @author system
 * @since 2024-01-01
 */
@SpringBootTest
@ActiveProfiles("test")
class StockInfoQueryTest {

    @Autowired
    private StockInfoMapper stockInfoMapper;

    @Test
    void testQueryAllStockInfo_查询所有股票基础信息() {
        System.out.println("🔍 开始查询所有股票基础信息");

        // When - 查询所有未删除的股票信息
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .orderByDesc("created_time");
        
        List<StockInfo> stockList = stockInfoMapper.selectList(queryWrapper);

        // Then - 验证查询结果
        assertNotNull(stockList, "股票列表不应为空");
        
        System.out.println("✅ 查询到 " + stockList.size() + " 只股票:");
        System.out.println("┌─────┬──────────────────┬──────────┬──────────┬──────────┬──────────┬──────────────────────┐");
        System.out.println("│ ID  │ 股票名称         │ 市场类型 │ A股代码  │ H股代码  │ 所属行业 │ 创建时间             │");
        System.out.println("├─────┼──────────────────┼──────────┼──────────┼──────────┼──────────┼──────────────────────┤");
        
        for (StockInfo stock : stockList) {
            System.out.printf("│ %-3d │ %-16s │ %-8s │ %-8s │ %-8s │ %-8s │ %-20s │%n",
                stock.getId(),
                stock.getStockName(),
                stock.getMarketType(),
                stock.getAStockCode(),
                stock.getHStockCode(),
                stock.getIndustry(),
                stock.getCreatedTime()
            );
        }
        System.out.println("└─────┴──────────────────┴──────────┴──────────┴──────────┴──────────┴──────────────────────┘");
    }

    @Test
    void testQueryStockByName_按股票名称查询() {
        System.out.println("🔍 按股票名称查询 - 建设银行");

        // When - 按股票名称查询
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .like("stock_name", "建设银行");
        
        List<StockInfo> stockList = stockInfoMapper.selectList(queryWrapper);

        // Then - 验证查询结果
        assertNotNull(stockList, "查询结果不应为空");
        
        System.out.println("✅ 查询到 " + stockList.size() + " 条匹配记录:");
        for (StockInfo stock : stockList) {
            System.out.println("   股票名称: " + stock.getStockName());
            System.out.println("   A股代码: " + stock.getAStockCode());
            System.out.println("   H股代码: " + stock.getHStockCode());
            System.out.println("   所属行业: " + stock.getIndustry());
            System.out.println("   市场类型: " + stock.getMarketType());
            System.out.println("   交易所: " + stock.getExchange());
            System.out.println("   状态: " + (stock.getStatus() == 1 ? "正常" : "停用"));
            System.out.println("   创建时间: " + stock.getCreatedTime());
            System.out.println("   ─────────────────────────────────");
        }
    }

    @Test
    void testQueryStockByCode_按股票代码查询() {
        System.out.println("🔍 按股票代码查询");

        // When - 按A股代码查询建设银行
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .eq("a_stock_code", "601939");
        
        StockInfo stock = stockInfoMapper.selectOne(queryWrapper);

        // Then - 验证查询结果
        if (stock != null) {
            System.out.println("✅ 找到股票信息:");
            System.out.println("   记录ID: " + stock.getId());
            System.out.println("   股票名称: " + stock.getStockName());
            System.out.println("   A股代码: " + stock.getAStockCode());
            System.out.println("   H股代码: " + stock.getHStockCode());
            System.out.println("   所属行业: " + stock.getIndustry());
            
            assertEquals("中国建设银行", stock.getStockName(), "股票名称应为中国建设银行");
            assertEquals("601939", stock.getAStockCode(), "A股代码应为601939");
            assertEquals("00939", stock.getHStockCode(), "H股代码应为00939");
            assertEquals("银行", stock.getIndustry(), "所属行业应为银行");
        } else {
            System.out.println("❌ 未找到A股代码为601939的股票");
        }
    }

    @Test
    void testQueryStockByIndustry_按行业查询() {
        System.out.println("🔍 按行业查询 - 银行业");

        // When - 查询银行业股票
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .eq("industry", "银行")
                   .orderByAsc("a_stock_code");
        
        List<StockInfo> bankStocks = stockInfoMapper.selectList(queryWrapper);

        // Then - 验证查询结果
        assertNotNull(bankStocks, "银行股票列表不应为空");
        
        System.out.println("✅ 查询到 " + bankStocks.size() + " 只银行股票:");
        System.out.println("┌──────────────────┬──────────┬──────────┬──────────────────────┐");
        System.out.println("│ 股票名称         │ A股代码  │ H股代码  │ 创建时间             │");
        System.out.println("├──────────────────┼──────────┼──────────┼──────────────────────┤");
        
        for (StockInfo stock : bankStocks) {
            System.out.printf("│ %-16s │ %-8s │ %-8s │ %-20s │%n",
                stock.getStockName(),
                stock.getAStockCode(),
                stock.getHStockCode(),
                stock.getCreatedTime()
            );
        }
        System.out.println("└──────────────────┴──────────┴──────────┴──────────────────────┘");
        
        // 验证所有查询到的股票都是银行业
        for (StockInfo stock : bankStocks) {
            assertEquals("银行", stock.getIndustry(), "所有股票都应该属于银行业");
        }
    }

    @Test
    void testQueryStockByMarketType_按市场类型查询() {
        System.out.println("🔍 按市场类型查询 - AH股");

        // When - 查询AH股
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .eq("market_type", "AH")
                   .orderByDesc("created_time");
        
        List<StockInfo> ahStocks = stockInfoMapper.selectList(queryWrapper);

        // Then - 验证查询结果
        assertNotNull(ahStocks, "AH股列表不应为空");
        
        System.out.println("✅ 查询到 " + ahStocks.size() + " 只AH股:");
        for (StockInfo stock : ahStocks) {
            System.out.println("   " + stock.getStockName() + " (" + stock.getAStockCode() + "/" + stock.getHStockCode() + ")");
            
            // 验证AH股必须同时有A股和H股代码
            assertNotNull(stock.getAStockCode(), "AH股必须有A股代码");
            assertNotNull(stock.getHStockCode(), "AH股必须有H股代码");
            assertEquals("AH", stock.getMarketType(), "市场类型应为AH");
        }
    }

    @Test
    void testQueryStockWithPagination_分页查询股票信息() {
        System.out.println("🔍 分页查询股票信息");

        // When - 分页查询，每页2条记录
        Page<StockInfo> page = new Page<>(1, 2);
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .orderByDesc("created_time");
        
        Page<StockInfo> result = stockInfoMapper.selectPage(page, queryWrapper);

        // Then - 验证分页结果
        assertNotNull(result, "分页结果不应为空");
        
        System.out.println("✅ 分页查询结果:");
        System.out.println("   当前页: " + result.getCurrent());
        System.out.println("   每页大小: " + result.getSize());
        System.out.println("   总记录数: " + result.getTotal());
        System.out.println("   总页数: " + result.getPages());
        System.out.println("   当前页记录数: " + result.getRecords().size());
        
        System.out.println("   当前页数据:");
        for (StockInfo stock : result.getRecords()) {
            System.out.println("     " + stock.getStockName() + " (" + stock.getAStockCode() + ")");
        }
        
        // 验证分页参数
        assertEquals(1, result.getCurrent(), "当前页应为第1页");
        assertEquals(2, result.getSize(), "每页大小应为2");
        assertTrue(result.getRecords().size() <= 2, "当前页记录数不应超过2条");
    }

    @Test
    void testQueryStockCount_统计股票数量() {
        System.out.println("🔍 统计股票数量");

        // When - 统计各种类型的股票数量
        
        // 总股票数
        QueryWrapper<StockInfo> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("deleted", 0);
        Long totalCount = stockInfoMapper.selectCount(totalWrapper);
        
        // 银行股数量
        QueryWrapper<StockInfo> bankWrapper = new QueryWrapper<>();
        bankWrapper.eq("deleted", 0).eq("industry", "银行");
        Long bankCount = stockInfoMapper.selectCount(bankWrapper);
        
        // AH股数量
        QueryWrapper<StockInfo> ahWrapper = new QueryWrapper<>();
        ahWrapper.eq("deleted", 0).eq("market_type", "AH");
        Long ahCount = stockInfoMapper.selectCount(ahWrapper);
        
        // 正常状态股票数量
        QueryWrapper<StockInfo> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("deleted", 0).eq("status", 1);
        Long activeCount = stockInfoMapper.selectCount(activeWrapper);

        // Then - 验证统计结果
        System.out.println("✅ 股票数量统计:");
        System.out.println("   总股票数: " + totalCount);
        System.out.println("   银行股数: " + bankCount);
        System.out.println("   AH股数: " + ahCount);
        System.out.println("   正常状态股票数: " + activeCount);
        
        assertNotNull(totalCount, "总股票数不应为空");
        assertTrue(totalCount >= 0, "总股票数应大于等于0");
        assertTrue(bankCount <= totalCount, "银行股数不应超过总股票数");
        assertTrue(ahCount <= totalCount, "AH股数不应超过总股票数");
        assertTrue(activeCount <= totalCount, "正常状态股票数不应超过总股票数");
    }

    @Test
    void testQueryStockDetail_查询股票详细信息() {
        System.out.println("🔍 查询股票详细信息 - 建设银行");

        // When - 查询建设银行的详细信息
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .and(wrapper -> wrapper
                       .like("stock_name", "建设银行")
                       .or()
                       .eq("a_stock_code", "601939")
                       .or()
                       .eq("h_stock_code", "00939")
                   );
        
        List<StockInfo> stocks = stockInfoMapper.selectList(queryWrapper);

        // Then - 验证并显示详细信息
        assertNotNull(stocks, "查询结果不应为空");
        
        if (!stocks.isEmpty()) {
            StockInfo stock = stocks.get(0);
            System.out.println("✅ 建设银行详细信息:");
            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║            股票详细信息              ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║ 记录ID    : " + String.format("%-23s", stock.getId()) + "║");
            System.out.println("║ 股票名称  : " + String.format("%-23s", stock.getStockName()) + "║");
            System.out.println("║ 市场类型  : " + String.format("%-23s", stock.getMarketType()) + "║");
            System.out.println("║ A股代码   : " + String.format("%-23s", stock.getAStockCode()) + "║");
            System.out.println("║ H股代码   : " + String.format("%-23s", stock.getHStockCode()) + "║");
            System.out.println("║ 交易所    : " + String.format("%-23s", stock.getExchange()) + "║");
            System.out.println("║ 所属行业  : " + String.format("%-23s", stock.getIndustry()) + "║");
            System.out.println("║ 状态      : " + String.format("%-23s", stock.getStatus() == 1 ? "正常" : "停用") + "║");
            System.out.println("║ 创建时间  : " + String.format("%-23s", stock.getCreatedTime()) + "║");
            System.out.println("║ 更新时间  : " + String.format("%-23s", stock.getUpdatedTime()) + "║");
            System.out.println("╚══════════════════════════════════════╝");
            
            // 验证关键信息
            assertTrue(stock.getStockName().contains("建设银行"), "股票名称应包含建设银行");
            assertEquals("AH", stock.getMarketType(), "市场类型应为AH");
            assertEquals("银行", stock.getIndustry(), "所属行业应为银行");
            assertEquals(1, stock.getStatus(), "状态应为正常");
        } else {
            System.out.println("❌ 未找到建设银行的股票信息");
        }
    }
}