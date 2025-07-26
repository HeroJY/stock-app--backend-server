package com.stock.premium.service.impl;

import com.stock.premium.entity.StockInfo;
import com.stock.premium.entity.StockPriceRecord;
import com.stock.premium.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 数据采集服务实现类
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Service
public class DataCollectionServiceImpl implements DataCollectionService {

    @Autowired
    private StockInfoService stockInfoService;

    @Autowired
    private TencentFinanceService tencentFinanceService;

    @Autowired
    private PremiumRateService premiumRateService;

    @Override
    public void collectAllStockData() {
        try {
            log.info("开始采集所有股票数据");
            
            // 获取所有A+H股票对
            List<String> ahStockPairs = stockInfoService.getAHStockPairs();
            log.info("找到{}个A+H股票对", ahStockPairs.size());

            // 获取汇率
            BigDecimal exchangeRate = tencentFinanceService.getExchangeRate("HKDCNY");
            log.debug("当前汇率: {}", exchangeRate);

            int successCount = 0;
            for (String aStockCode : ahStockPairs) {
                try {
                    collectStockDataPair(aStockCode, exchangeRate);
                    successCount++;
                } catch (Exception e) {
                    log.error("采集股票数据失败: {}", aStockCode, e);
                }
            }

            log.info("数据采集完成，成功采集{}个股票对", successCount);
        } catch (Exception e) {
            log.error("批量采集股票数据失败", e);
        }
    }

    @Override
    public void collectStockData(String stockCode) {
        try {
            BigDecimal exchangeRate = tencentFinanceService.getExchangeRate("HKDCNY");
            collectStockDataPair(stockCode, exchangeRate);
        } catch (Exception e) {
            log.error("采集单个股票数据失败: {}", stockCode, e);
        }
    }

    @Override
    public void collectDataIfTradingTime() {
        // 检查A股或港股是否在交易时间
        boolean aMarketOpen = tencentFinanceService.isMarketOpen("A");
        boolean hMarketOpen = tencentFinanceService.isMarketOpen("H");

        if (aMarketOpen || hMarketOpen) {
            log.info("当前在交易时间内，开始采集数据 - A股开市: {}, 港股开市: {}", aMarketOpen, hMarketOpen);
            collectAllStockData();
        } else {
            log.debug("当前不在交易时间内，跳过数据采集");
        }
    }

    /**
     * 采集单个股票对的数据
     */
    private void collectStockDataPair(String aStockCode, BigDecimal exchangeRate) {
        try {
            // 获取A股信息
            StockInfo aStockInfo = stockInfoService.getByStockCode(aStockCode);
            if (aStockInfo == null) {
                log.warn("未找到A股信息: {}", aStockCode);
                return;
            }

            // 获取对应的H股信息
            StockInfo hStockInfo = stockInfoService.getHStockByACode(aStockCode);
            if (hStockInfo == null) {
                log.warn("未找到对应的H股信息: {}", aStockCode);
                return;
            }

            // 获取A股价格
            StockPriceRecord aStockPrice = tencentFinanceService.getStockPrice(aStockCode, "A");
            if (aStockPrice == null) {
                log.warn("获取A股价格失败: {}", aStockCode);
                return;
            }

            // 获取H股价格
            StockPriceRecord hStockPrice = tencentFinanceService.getStockPrice(hStockInfo.getHStockCode(), "H");
            if (hStockPrice == null) {
                log.warn("获取H股价格失败: {}", hStockInfo.getHStockCode());
                return;
            }

            // 计算并记录溢价率
            premiumRateService.recordPremiumRate(
                aStockCode,
                aStockPrice.getCurrentPrice(),
                hStockPrice.getCurrentPrice(),
                exchangeRate
            );

            log.debug("成功采集股票对数据: {} - A股价格: {}, H股价格: {}", 
                     aStockCode, aStockPrice.getCurrentPrice(), hStockPrice.getCurrentPrice());

        } catch (Exception e) {
            log.error("采集股票对数据失败: {}", aStockCode, e);
        }
    }
}