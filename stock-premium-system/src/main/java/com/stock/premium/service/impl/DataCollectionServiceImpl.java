package com.stock.premium.service.impl;

import com.stock.premium.entity.StockInfo;
import com.stock.premium.service.DataCollectionService;
import com.stock.premium.service.StockInfoService;
import com.stock.premium.service.TencentFinanceService;
import com.stock.premium.service.PremiumRateService;
import com.stock.premium.service.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
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
    
    @Autowired
    private ExchangeRateService exchangeRateService;

    @Override
    public void collectAllStockData() {
        log.info("开始采集所有股票数据");
        
        try {
            List<StockInfo> activeStocks = stockInfoService.getActiveStocks();
            log.info("共找到 {} 只活跃股票", activeStocks.size());
            
            for (StockInfo stock : activeStocks) {
                try {
                    collectStockData(stock.getAStockCode());
                } catch (Exception e) {
                    log.error("采集股票 {} 数据时发生错误", stock.getAStockCode(), e);
                }
            }
            
            log.info("完成所有股票数据采集");
        } catch (Exception e) {
            log.error("采集所有股票数据时发生错误", e);
        }
    }

    @Override
    public void collectStockData(String aStockCode) {
        log.debug("开始采集股票 {} 的数据", aStockCode);
        
        try {
            // 根据A股代码获取股票信息
            StockInfo stockInfo = stockInfoService.getByStockCode(aStockCode);
            if (stockInfo == null) {
                log.warn("未找到股票代码 {} 的信息", aStockCode);
                return;
            }
            
            String hStockCode = stockInfo.getHStockCode();
            if (hStockCode == null || hStockCode.trim().isEmpty()) {
                log.warn("股票 {} 没有对应的H股代码", aStockCode);
                return;
            }
            
            // 获取A股价格
            var aStockPriceRecord = tencentFinanceService.getStockPrice(aStockCode, "A");
            if (aStockPriceRecord == null || aStockPriceRecord.getCurrentPrice() == null) {
                log.warn("无法获取A股 {} 的价格", aStockCode);
                return;
            }
            BigDecimal aStockPrice = aStockPriceRecord.getCurrentPrice();
            
            // 获取H股价格
            var hStockPriceRecord = tencentFinanceService.getStockPrice(hStockCode, "H");
            if (hStockPriceRecord == null || hStockPriceRecord.getCurrentPrice() == null) {
                log.warn("无法获取H股 {} 的价格", hStockCode);
                return;
            }
            BigDecimal hStockPrice = hStockPriceRecord.getCurrentPrice();
            
            // 获取汇率 (港币对人民币)
            var exchangeRateVO = exchangeRateService.getLatestRate("HKDCNY");
            if (exchangeRateVO == null || exchangeRateVO.getRate() == null) {
                log.warn("无法获取最新汇率");
                return;
            }
            BigDecimal exchangeRate = exchangeRateVO.getRate();
            
            // 记录溢价率数据
            premiumRateService.recordPremiumRate(aStockCode, aStockPrice, hStockPrice, exchangeRate);
            
            log.debug("成功采集股票 {} 的数据", aStockCode);
            
        } catch (Exception e) {
            log.error("采集股票 {} 数据时发生错误", aStockCode, e);
        }
    }

    @Override
    public void collectDataIfTradingTime() {
        if (isTradingTime()) {
            log.info("当前为交易时间，开始采集数据");
            collectAllStockData();
        } else {
            log.debug("当前非交易时间，跳过数据采集");
        }
    }
    
    /**
     * 判断当前是否为交易时间
     * A股交易时间：9:30-11:30, 13:00-15:00
     * H股交易时间：9:30-12:00, 13:00-16:00
     */
    private boolean isTradingTime() {
        LocalTime now = LocalTime.now();
        
        // 上午交易时间：9:30-11:30
        boolean morningTrading = now.isAfter(LocalTime.of(9, 30)) && 
                                now.isBefore(LocalTime.of(11, 30));
        
        // 下午交易时间：13:00-15:00
        boolean afternoonTrading = now.isAfter(LocalTime.of(13, 0)) && 
                                  now.isBefore(LocalTime.of(15, 0));
        
        return morningTrading || afternoonTrading;
    }
}