package com.stock.premium.scheduled;

import com.stock.premium.entity.StockPriceRecord;
import com.stock.premium.service.TencentFinanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 测试股票价格定时任务
 * 每30秒获取一次中国神华(sh601088)的股票价格
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Component
public class TestStockPriceTask {

    @Autowired
    private TencentFinanceService tencentFinanceService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 每30秒执行一次，获取中国神华股票价格
     */
    @Scheduled(fixedRate = 30000) // 30秒 = 30000毫秒
    public void fetchChinaShenhuaStockPrice() {
        try {
            String currentTime = LocalDateTime.now().format(FORMATTER);
            log.info("=== [{}] 开始获取中国神华(sh601088)股票价格 ===", currentTime);
            
            // 获取中国神华股票数据
            StockPriceRecord stockData = tencentFinanceService.getStockPrice("601088", "A");
            
            if (stockData != null) {
                log.info("✅ 成功获取股票数据:");
                log.info("   股票代码: {}", stockData.getStockCode());
                log.info("   当前价格: {} 元", stockData.getCurrentPrice());
                log.info("   开盘价格: {} 元", stockData.getOpenPrice());
                log.info("   最高价格: {} 元", stockData.getHighPrice());
                log.info("   最低价格: {} 元", stockData.getLowPrice());
                log.info("   昨收价格: {} 元", stockData.getPreClosePrice());
                log.info("   涨跌幅度: {}%", stockData.getChangeRate());
                log.info("   数据时间: {}", stockData.getRecordTime().format(FORMATTER));
                
                // 判断涨跌情况
                if (stockData.getChangeRate().doubleValue() > 0) {
                    log.info("📈 股价上涨 +{}%", stockData.getChangeRate());
                } else if (stockData.getChangeRate().doubleValue() < 0) {
                    log.info("📉 股价下跌 {}%", stockData.getChangeRate());
                } else {
                    log.info("➡️ 股价平盘");
                }
            } else {
                log.warn("❌ 未能获取到股票数据，可能原因:");
                log.warn("   1. 市场未开市");
                log.warn("   2. 网络连接问题");
                log.warn("   3. API服务异常");
                
                // 检查市场状态
                boolean isMarketOpen = tencentFinanceService.isMarketOpen("A");
                log.info("   A股市场状态: {}", isMarketOpen ? "开市" : "休市");
            }
            
            log.info("=== [{}] 股票价格获取任务完成 ===\n", currentTime);
            
        } catch (Exception e) {
            log.error("❌ 获取股票价格失败", e);
        }
    }

    /**
     * 每分钟输出一次任务运行状态
     */
    @Scheduled(fixedRate = 60000) // 60秒
    public void printTaskStatus() {
        String currentTime = LocalDateTime.now().format(FORMATTER);
        boolean isMarketOpen = tencentFinanceService.isMarketOpen("A");
        
        log.info("🔄 [{}] 定时任务运行中 - A股市场: {}", 
            currentTime, isMarketOpen ? "开市中" : "休市中");
    }
}