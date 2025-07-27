package com.stock.premium.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.premium.entity.StockInfo;
import com.stock.premium.mapper.StockInfoMapper;
import com.stock.premium.service.StockInfoService;
import com.stock.premium.utils.TencentFinanceApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


}
