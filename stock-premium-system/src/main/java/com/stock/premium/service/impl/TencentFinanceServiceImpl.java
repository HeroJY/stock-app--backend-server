package com.stock.premium.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.stock.premium.entity.StockPriceRecord;
import com.stock.premium.service.TencentFinanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯财经API服务实现类
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Service
public class TencentFinanceServiceImpl implements TencentFinanceService {

    @Value("${tencent.finance.api.base-url:https://qt.gtimg.cn/q=}")
    private String baseUrl;

    @Value("${tencent.finance.api.timeout:5000}")
    private int timeout;

    @Override
    public StockPriceRecord getStockPrice(String stockCode, String marketType) {
        try {
            String fullCode = buildFullStockCode(stockCode, marketType);
            String url = baseUrl + fullCode;
            
            log.debug("请求腾讯财经API: {}", url);
            String response = HttpUtil.get(url, timeout);
            
            if (StrUtil.isBlank(response)) {
                log.warn("腾讯财经API返回空数据: {}", stockCode);
                return null;
            }

            return parseStockData(response, stockCode, marketType);
        } catch (Exception e) {
            log.error("获取股票价格失败: stockCode={}, marketType={}", stockCode, marketType, e);
            return null;
        }
    }

    @Override
    public List<StockPriceRecord> getBatchStockPrices(List<String> stockCodes) {
        List<StockPriceRecord> results = new ArrayList<>();
        
        if (stockCodes == null || stockCodes.isEmpty()) {
            return results;
        }

        try {
            // 构建批量请求的股票代码
            StringBuilder codeBuilder = new StringBuilder();
            for (String code : stockCodes) {
                if (codeBuilder.length() > 0) {
                    codeBuilder.append(",");
                }
                // 根据代码判断市场类型
                String marketType = determineMarketType(code);
                codeBuilder.append(buildFullStockCode(code, marketType));
            }

            String url = baseUrl + codeBuilder.toString();
            log.debug("批量请求腾讯财经API: {}", url);
            
            String response = HttpUtil.get(url, timeout);
            if (StrUtil.isBlank(response)) {
                log.warn("腾讯财经API批量请求返回空数据");
                return results;
            }

            // 解析批量响应数据
            String[] lines = response.split("\n");
            for (int i = 0; i < lines.length && i < stockCodes.size(); i++) {
                String stockCode = stockCodes.get(i);
                String marketType = determineMarketType(stockCode);
                StockPriceRecord record = parseStockData(lines[i], stockCode, marketType);
                if (record != null) {
                    results.add(record);
                }
            }
        } catch (Exception e) {
            log.error("批量获取股票价格失败", e);
        }

        return results;
    }

    @Override
    public BigDecimal getExchangeRate(String currencyPair) {
        try {
            // 腾讯财经汇率API格式: r_hkdcny (港币对人民币)
            String code = "r_" + currencyPair.toLowerCase();
            String url = baseUrl + code;
            
            log.debug("请求汇率API: {}", url);
            String response = HttpUtil.get(url, timeout);
            
            if (StrUtil.isBlank(response)) {
                log.warn("获取汇率失败，使用默认汇率: {}", currencyPair);
                return new BigDecimal("0.9"); // 默认汇率
            }

            return parseExchangeRate(response);
        } catch (Exception e) {
            log.error("获取汇率失败: {}", currencyPair, e);
            return new BigDecimal("0.9"); // 默认汇率
        }
    }

    @Override
    public boolean isMarketOpen(String marketType) {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        
        // 检查是否为工作日
        int dayOfWeek = today.getDayOfWeek().getValue();
        if (dayOfWeek > 5) { // 周六日
            return false;
        }

        if ("A".equals(marketType)) {
            // A股交易时间: 9:30-11:30, 13:00-15:00
            return (now.isAfter(LocalTime.of(9, 30)) && now.isBefore(LocalTime.of(11, 30))) ||
                   (now.isAfter(LocalTime.of(13, 0)) && now.isBefore(LocalTime.of(15, 0)));
        } else if ("H".equals(marketType)) {
            // 港股交易时间: 9:30-12:00, 13:00-16:00
            return (now.isAfter(LocalTime.of(9, 30)) && now.isBefore(LocalTime.of(12, 0))) ||
                   (now.isAfter(LocalTime.of(13, 0)) && now.isBefore(LocalTime.of(16, 0)));
        }
        
        return false;
    }

    /**
     * 构建完整的股票代码
     */
    private String buildFullStockCode(String stockCode, String marketType) {
        if ("A".equals(marketType)) {
            // A股代码格式
            if (stockCode.startsWith("6")) {
                return "sh" + stockCode; // 上海
            } else {
                return "sz" + stockCode; // 深圳
            }
        } else if ("H".equals(marketType)) {
            // 港股代码格式
            return "hk" + stockCode;
        }
        return stockCode;
    }

    /**
     * 根据股票代码判断市场类型
     */
    private String determineMarketType(String stockCode) {
        if (stockCode.length() == 6 && stockCode.matches("\\d+")) {
            return "A"; // A股
        } else if (stockCode.length() == 5 && stockCode.matches("\\d+")) {
            return "H"; // 港股
        }
        return "A"; // 默认A股
    }

    /**
     * 解析股票数据
     */
    private StockPriceRecord parseStockData(String data, String stockCode, String marketType) {
        try {
            if (StrUtil.isBlank(data) || !data.contains("=")) {
                return null;
            }

            // 腾讯财经API返回格式: v_sh000001="51~平安银行~000001~12.34~12.30~12.35~..."
            String[] parts = data.split("=");
            if (parts.length < 2) {
                return null;
            }

            String content = parts[1].replace("\"", "").replace(";", "");
            String[] fields = content.split("~");
            
            if (fields.length < 10) {
                log.warn("股票数据格式不正确: {}", data);
                return null;
            }

            StockPriceRecord record = new StockPriceRecord();
            record.setStockCode(stockCode);
            record.setMarketType(marketType);
            record.setCurrentPrice(new BigDecimal(fields[3])); // 当前价
            record.setPreClosePrice(new BigDecimal(fields[4])); // 昨收价
            record.setOpenPrice(new BigDecimal(fields[5])); // 开盘价
            record.setHighPrice(new BigDecimal(fields[33])); // 最高价
            record.setLowPrice(new BigDecimal(fields[34])); // 最低价
            
            // 计算涨跌幅
            if (record.getPreClosePrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal changeRate = record.getCurrentPrice()
                    .subtract(record.getPreClosePrice())
                    .divide(record.getPreClosePrice(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                record.setChangeRate(changeRate);
            }

            record.setRecordTime(LocalDateTime.now());
            record.setTradeDate(LocalDate.now());
            record.setDataSource("tencent");

            return record;
        } catch (Exception e) {
            log.error("解析股票数据失败: {}", data, e);
            return null;
        }
    }

    /**
     * 解析汇率数据
     */
    private BigDecimal parseExchangeRate(String data) {
        try {
            // 汇率数据格式: r_hkdcny="0.9123"
            String[] parts = data.split("=");
            if (parts.length < 2) {
                return new BigDecimal("0.9");
            }

            String rateStr = parts[1].replace("\"", "").replace(";", "");
            return new BigDecimal(rateStr);
        } catch (Exception e) {
            log.error("解析汇率数据失败: {}", data, e);
            return new BigDecimal("0.9");
        }
    }
}