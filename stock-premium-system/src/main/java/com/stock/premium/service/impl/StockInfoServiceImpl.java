package com.stock.premium.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.premium.entity.PremiumRateRecord;
import com.stock.premium.entity.StockInfo;
import com.stock.premium.entity.StockPriceRecord;
import com.stock.premium.mapper.StockInfoMapper;
import com.stock.premium.service.PremiumRateService;
import com.stock.premium.service.StockInfoService;
import com.stock.premium.service.StockPriceService;
import com.stock.premium.utils.TencentFinanceApiUtil;
import com.stock.premium.vo.StockDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 股票基础信息服务实现类
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Service
public class StockInfoServiceImpl extends ServiceImpl<StockInfoMapper, StockInfo> implements StockInfoService {

    @Autowired
    private TencentFinanceApiUtil tencentFinanceApiUtil;
    
    @Autowired
    private StockPriceService stockPriceService;
    
    @Autowired
    private PremiumRateService premiumRateService;


    @Override
    public List<StockInfo> getActiveStocks() {
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                   .eq("deleted", 0)
                   .orderByAsc("a_stock_code");
        return list(queryWrapper);
    }

    @Override
    public StockInfo getByStockCode(String stockCode) {
        // 处理股票代码，去掉前缀（如sh、sz、hk等）
        final String cleanStockCode = stockCode.length() > 6 ? 
            stockCode.replaceAll("^[a-zA-Z]+", "") : stockCode;
        
        // 首先从数据库查询
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                   .eq("deleted", 0)
                   .and(wrapper -> 
                       wrapper.eq("a_stock_code", cleanStockCode)
                              .or()
                              .eq("h_stock_code", cleanStockCode)
                   );
        
        StockInfo result = getOne(queryWrapper);
        if (result != null) {
            log.debug("从数据库根据股票代码 {} 查询到股票信息: {}", stockCode, result.getStockName());
            return result;
        }
        
        // 数据库中没有找到，尝试调用腾讯财经API查询
        log.info("数据库中未找到股票代码 {}，尝试调用腾讯财经API查询", stockCode);
        try {
            Map<String, Object> apiResult = tencentFinanceApiUtil.getStockInfoByCode(stockCode);
            if (apiResult != null && !apiResult.isEmpty()) {
                // API查询成功，将结果转换为StockInfo对象并保存到数据库
                StockInfo stockInfo = convertApiResultToStockInfo(apiResult);
                if (stockInfo != null) {
                    // 保存到数据库
                    save(stockInfo);
                    log.info("通过腾讯财经API查询到股票代码 {} 的信息并保存到数据库: {}", stockCode, stockInfo.getStockName());
                    return stockInfo;
                }
            }
        } catch (Exception e) {
            log.error("调用腾讯财经API查询股票代码 {} 失败: {}", stockCode, e.getMessage());
        }
        
        // 数据库和API都没有找到
        log.warn("数据库和腾讯财经API都未找到股票代码 {} 的信息，请检查股票代码是否正确", stockCode);
        return null;
    }

    /**
     * 将腾讯财经API返回的结果转换为StockInfo对象
     */
    private StockInfo convertApiResultToStockInfo(Map<String, Object> apiResult) {
        try {
            StockInfo stockInfo = new StockInfo();
            
            // 设置基本信息
            stockInfo.setStockName((String) apiResult.get("stock_name"));
            stockInfo.setAStockCode((String) apiResult.get("a_stock_code"));
            stockInfo.setHStockCode((String) apiResult.get("h_stock_code"));
            stockInfo.setMarketType((String) apiResult.get("market_type"));
            stockInfo.setExchange((String) apiResult.get("exchange"));
            stockInfo.setIndustry((String) apiResult.get("industry"));
            
            // 设置默认状态
            stockInfo.setStatus(1); // 启用状态
            stockInfo.setDeleted(0); // 未删除
            stockInfo.setCreatedTime(LocalDateTime.now());
            stockInfo.setUpdatedTime(LocalDateTime.now());
            
            // 验证必要字段
            if (stockInfo.getStockName() == null || stockInfo.getStockName().trim().isEmpty()) {
                log.warn("API返回的股票名称为空，跳过转换");
                return null;
            }
            
            String aStockCode = stockInfo.getAStockCode();
            String hStockCode = stockInfo.getHStockCode();
            if ((aStockCode == null || aStockCode.trim().isEmpty()) && 
                (hStockCode == null || hStockCode.trim().isEmpty())) {
                log.warn("API返回的A股和H股代码都为空，跳过转换");
                return null;
            }
            
            return stockInfo;
            
        } catch (Exception e) {
            log.error("转换API结果为StockInfo对象失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public StockDetailVO getStockDetail(String stockCode) {
        try {
            // 1. 获取股票基本信息
            StockInfo stockInfo = getByStockCode(stockCode);
            if (stockInfo == null) {
                log.warn("未找到股票代码 {} 的基本信息", stockCode);
                return null;
            }

            StockDetailVO stockDetail = new StockDetailVO();
            stockDetail.setStockCode(stockCode);
            stockDetail.setStockName(stockInfo.getStockName());
            stockDetail.setAStockCode(stockInfo.getAStockCode());
            stockDetail.setHStockCode(stockInfo.getHStockCode());

            // 2. 获取最新的A股和H股价格
            LocalDate today = LocalDate.now();
            
            // 获取A股价格
            if (stockInfo.getAStockCode() != null && !stockInfo.getAStockCode().isEmpty()) {
                List<StockPriceRecord> aStockPrices = stockPriceService.getByStockCodeAndDate(stockInfo.getAStockCode(), today);
                if (aStockPrices != null && !aStockPrices.isEmpty()) {
                    // 获取最新的价格记录
                    StockPriceRecord latestAPrice = aStockPrices.stream()
                            .filter(record -> "A股".equals(record.getMarketType()))
                            .max((r1, r2) -> r1.getRecordTime().compareTo(r2.getRecordTime()))
                            .orElse(null);
                    if (latestAPrice != null) {
                        stockDetail.setAStockPrice(latestAPrice.getCurrentPrice());
                    }
                }
            }

            // 获取H股价格
            if (stockInfo.getHStockCode() != null && !stockInfo.getHStockCode().isEmpty()) {
                List<StockPriceRecord> hStockPrices = stockPriceService.getByStockCodeAndDate(stockInfo.getHStockCode(), today);
                if (hStockPrices != null && !hStockPrices.isEmpty()) {
                    // 获取最新的价格记录
                    StockPriceRecord latestHPrice = hStockPrices.stream()
                            .filter(record -> "H股".equals(record.getMarketType()))
                            .max((r1, r2) -> r1.getRecordTime().compareTo(r2.getRecordTime()))
                            .orElse(null);
                    if (latestHPrice != null) {
                        stockDetail.setHStockPrice(latestHPrice.getCurrentPrice());
                    }
                }
            }

            // 3. 获取最新的溢价率
            String queryStockCode = stockInfo.getAStockCode() != null ? stockInfo.getAStockCode() : stockCode;
            List<PremiumRateRecord> premiumRates = premiumRateService.getPremiumRatesByStockAndDate(queryStockCode, today);
            if (premiumRates != null && !premiumRates.isEmpty()) {
                // 获取最新的溢价率记录
                PremiumRateRecord latestPremiumRate = premiumRates.stream()
                        .max((r1, r2) -> r1.getRecordTime().compareTo(r2.getRecordTime()))
                        .orElse(null);
                if (latestPremiumRate != null) {
                    stockDetail.setPremiumRate(latestPremiumRate.getPremiumRate());
                    stockDetail.setExchangeRate(latestPremiumRate.getExchangeRate());
                    
                    // 如果价格信息缺失，从溢价率记录中补充
                    if (stockDetail.getAStockPrice() == null) {
                        stockDetail.setAStockPrice(latestPremiumRate.getAStockPrice());
                    }
                    if (stockDetail.getHStockPrice() == null) {
                        stockDetail.setHStockPrice(latestPremiumRate.getHStockPrice());
                    }
                }
            }

            log.debug("获取股票详细信息成功: {}", stockDetail.getStockName());
            return stockDetail;

        } catch (Exception e) {
            log.error("获取股票详细信息失败: stockCode={}", stockCode, e);
            return null;
        }
    }
}
