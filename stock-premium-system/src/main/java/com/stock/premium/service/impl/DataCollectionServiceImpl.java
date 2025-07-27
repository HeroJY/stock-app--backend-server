package com.stock.premium.service.impl;

import com.stock.premium.entity.StockInfo;
import com.stock.premium.service.DataCollectionService;
import com.stock.premium.service.StockInfoService;
import com.stock.premium.service.TencentFinanceService;
import com.stock.premium.service.PremiumRateService;
import com.stock.premium.service.ExchangeRateService;
import com.stock.premium.service.StockPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    
    @Autowired
    private StockPriceService stockPriceService;

    @Override
    public void collectAllStockData() {
        log.info("开始采集所有股票数据");
        
        // 检查是否为交易时间
        // if (!isTradingTime()) {
        //     log.info("当前不是交易时间，跳过数据采集");
        //     return;
        // }
        
        try {
            List<StockInfo> activeStocks = stockInfoService.getActiveStocks();
            log.info("共找到 {} 只活跃股票", activeStocks.size());
            
            for (StockInfo stock : activeStocks) {
                try {
                    collectSingleStockData(stock.getAStockCode());
                } catch (Exception e) {
                    log.error("采集股票 {} 数据时发生错误", stock.getAStockCode(), e);
                }
            }
            
            log.info("完成所有股票数据采集");
        } catch (Exception e) {
            log.error("采集所有股票数据时发生错误", e);
        }
    }

    /**
     * 采集单个股票的数据（私有方法）
     * 1. 采集股票价格信息并存储到stock_price_record表中
     * 2. 计算HA溢价率并将信息存储到premium_rate_record表中
     */
    private void collectSingleStockData(String aStockCode) {
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
            
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = LocalDate.now();
            
            // 1. 获取并保存A股价格数据
            var aStockPriceRecord = tencentFinanceService.getStockPrice(aStockCode, "A");
            if (aStockPriceRecord == null || aStockPriceRecord.getCurrentPrice() == null) {
                log.warn("无法获取A股 {} 的价格", aStockCode);
                return;
            }
            
            // 设置A股价格记录的时间信息
            aStockPriceRecord.setRecordTime(now);
            aStockPriceRecord.setTradeDate(today);
            
            // 保存A股价格记录到数据库
            boolean aSaved = stockPriceService.saveStockPriceRecord(aStockPriceRecord);
            if (!aSaved) {
                log.warn("保存A股 {} 价格记录失败", aStockCode);
            }
            
            BigDecimal aStockPrice = aStockPriceRecord.getCurrentPrice();
            
            // 2. 获取并保存H股价格数据
            var hStockPriceRecord = tencentFinanceService.getStockPrice(hStockCode, "H");
            if (hStockPriceRecord == null || hStockPriceRecord.getCurrentPrice() == null) {
                log.warn("无法获取H股 {} 的价格", hStockCode);
                return;
            }
            
            // 设置H股价格记录的时间信息
            hStockPriceRecord.setRecordTime(now);
            hStockPriceRecord.setTradeDate(today);
            
            // 保存H股价格记录到数据库
            boolean hSaved = stockPriceService.saveStockPriceRecord(hStockPriceRecord);
            if (!hSaved) {
                log.warn("保存H股 {} 价格记录失败", hStockCode);
            }
            
            BigDecimal hStockPrice = hStockPriceRecord.getCurrentPrice();
            
            // 3. 获取汇率 (港币对人民币)
            var exchangeRateVO = exchangeRateService.getLatestRate("HKDCNY");
            if (exchangeRateVO == null || exchangeRateVO.getRate() == null) {
                log.warn("无法获取最新汇率");
                return;
            }
            BigDecimal exchangeRate = exchangeRateVO.getRate();
            
            // 4. 计算并记录溢价率数据到premium_rate_record表
            premiumRateService.recordPremiumRate(aStockCode, aStockPrice, hStockPrice, exchangeRate);
            
            log.debug("成功采集并保存股票 {} 的数据 - A股价格: {}, H股价格: {}, 汇率: {}", 
                     aStockCode, aStockPrice, hStockPrice, exchangeRate);
            
        } catch (Exception e) {
            log.error("采集股票 {} 数据时发生错误", aStockCode, e);
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