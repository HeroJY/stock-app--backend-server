package com.stock.premium.controller;

import com.stock.premium.entity.StockInfo;
import com.stock.premium.entity.StockPriceRecord;
import com.stock.premium.service.StockInfoService;
import com.stock.premium.service.TencentFinanceService;
import com.stock.premium.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Api(tags = "系统测试接口")
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private StockInfoService stockInfoService;
    
    @Autowired
    private TencentFinanceService tencentFinanceService;

    @ApiOperation("系统健康检查")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("application", "股票溢价率监控系统");
        data.put("version", "1.0.0");
        
        log.info("系统健康检查通过");
        return Result.success("系统运行正常", data);
    }

    @ApiOperation("数据库连接测试")
    @GetMapping("/database")
    public Result<Map<String, Object>> testDatabase() {
        try {
            List<StockInfo> stocks = stockInfoService.getActiveStocks();
            
            Map<String, Object> data = new HashMap<>();
            data.put("database_status", "连接正常");
            data.put("stock_count", stocks.size());
            data.put("stocks", stocks);
            
            log.info("数据库连接测试成功，查询到{}只股票", stocks.size());
            return Result.success("数据库连接正常", data);
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            return Result.error("数据库连接失败: " + e.getMessage());
        }
    }

    @ApiOperation("查询A+H股票对")
    @GetMapping("/ah-pairs")
    public Result<List<String>> getAHPairs() {
        try {
            List<String> pairs = stockInfoService.getAHStockPairs();
            log.info("查询到{}个A+H股票对", pairs.size());
            return Result.success("查询成功", pairs);
        } catch (Exception e) {
            log.error("查询A+H股票对失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("测试腾讯财经API - 获取sh601088股票数据")
    @GetMapping("/tencent-api/sh601088")
    public Result<StockPriceRecord> testTencentApiSh601088() {
        try {
            log.info("开始测试腾讯财经API获取sh601088股票数据");
            
            // 获取sh601088（中国神华）的股票数据
            StockPriceRecord stockData = tencentFinanceService.getStockPrice("601088", "A");
            
            if (stockData != null) {
                log.info("成功获取sh601088股票数据: 当前价={}, 涨跌幅={}%", 
                    stockData.getCurrentPrice(), stockData.getChangeRate());
                return Result.success("成功获取sh601088股票数据", stockData);
            } else {
                log.warn("未能获取到sh601088股票数据");
                return Result.error("未能获取到股票数据，可能是市场未开市或网络问题");
            }
        } catch (Exception e) {
            log.error("测试腾讯财经API失败", e);
            return Result.error("API调用失败: " + e.getMessage());
        }
    }

    @ApiOperation("测试腾讯财经API - 检查市场状态")
    @GetMapping("/tencent-api/market-status")
    public Result<Map<String, Object>> testMarketStatus() {
        try {
            Map<String, Object> data = new HashMap<>();
            
            boolean aMarketOpen = tencentFinanceService.isMarketOpen("A");
            boolean hMarketOpen = tencentFinanceService.isMarketOpen("H");
            
            data.put("a_market_open", aMarketOpen);
            data.put("h_market_open", hMarketOpen);
            data.put("check_time", LocalDateTime.now());
            
            log.info("市场状态检查: A股={}, 港股={}", aMarketOpen, hMarketOpen);
            return Result.success("市场状态检查完成", data);
        } catch (Exception e) {
            log.error("检查市场状态失败", e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }
}