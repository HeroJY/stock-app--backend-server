package com.stock.premium.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯财经API工具类
 * 
 * @author system
 * @since 2024-01-01
 */
@Component
@Slf4j
public class TencentFinanceApiUtil {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 根据股票名称搜索股票基本信息
     */
    public Map<String, Object> searchStockByName(String stockName) {
        log.info("开始搜索股票: {}", stockName);
        
        try {
            // 腾讯财经搜索API
            String encodedName = URLEncoder.encode(stockName, StandardCharsets.UTF_8);
            String searchUrl = "https://qt.gtimg.cn/q=" + encodedName;
            
            log.info("调用腾讯财经API: {}", searchUrl);
            String response = restTemplate.getForObject(searchUrl, String.class);
            
            if (response == null || response.trim().isEmpty()) {
                log.warn("腾讯财经API返回空响应");
                return null;
            }
            
            log.info("腾讯财经API响应: {}", response);
            
            // 解析响应数据
            Map<String, Object> stockInfo = parseStockResponse(response, stockName);
            
            if (stockInfo != null) {
                log.info("成功解析股票信息: {}", stockInfo);
            } else {
                log.warn("未能解析到股票信息");
            }
            
            return stockInfo;
            
        } catch (Exception e) {
            log.error("搜索股票信息失败: {}", stockName, e);
            return null;
        }
    }

    /**
     * 解析腾讯财经API响应
     */
    private Map<String, Object> parseStockResponse(String response, String stockName) {
        try {
            // 腾讯财经API返回格式通常是: v_股票代码="数据";
            if (!response.contains("=")) {
                log.warn("响应格式不正确: {}", response);
                return null;
            }
            
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.contains(stockName) || line.contains("\"")) {
                    // 提取股票数据
                    int startIndex = line.indexOf("\"");
                    int endIndex = line.lastIndexOf("\"");
                    
                    if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                        String dataStr = line.substring(startIndex + 1, endIndex);
                        String[] fields = dataStr.split("~");
                        
                        if (fields.length >= 10) {
                            Map<String, Object> stockInfo = new HashMap<>();
                            stockInfo.put("stock_name", fields[1]); // 股票名称
                            stockInfo.put("stock_code", fields[2]); // 股票代码
                            stockInfo.put("current_price", fields[3]); // 当前价格
                            stockInfo.put("change_percent", fields[32]); // 涨跌幅
                            stockInfo.put("market_type", determineMarketType(fields[2])); // 市场类型
                            stockInfo.put("exchange", determineExchange(fields[2])); // 交易所
                            
                            return stockInfo;
                        }
                    }
                }
            }
            
            // 如果上面的解析失败，尝试简单解析
            return parseSimpleFormat(response, stockName);
            
        } catch (Exception e) {
            log.error("解析股票响应失败", e);
            return null;
        }
    }

    /**
     * 简单格式解析
     */
    private Map<String, Object> parseSimpleFormat(String response, String stockName) {
        try {
            // 创建基本的股票信息
            Map<String, Object> stockInfo = new HashMap<>();
            stockInfo.put("stock_name", stockName);
            
            // 尝试从响应中提取股票代码
            if (response.contains("sh") || response.contains("sz")) {
                String code = extractStockCode(response);
                if (code != null) {
                    stockInfo.put("stock_code", code);
                    stockInfo.put("market_type", determineMarketType(code));
                    stockInfo.put("exchange", determineExchange(code));
                }
            }
            
            return stockInfo.isEmpty() ? null : stockInfo;
            
        } catch (Exception e) {
            log.error("简单格式解析失败", e);
            return null;
        }
    }

    /**
     * 提取股票代码
     */
    private String extractStockCode(String response) {
        try {
            // 查找6位数字的股票代码
            String[] parts = response.split("[^0-9]");
            for (String part : parts) {
                if (part.length() == 6 && part.matches("\\d{6}")) {
                    return part;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 确定市场类型
     */
    private String determineMarketType(String stockCode) {
        if (stockCode == null || stockCode.length() < 6) {
            return "A";
        }
        
        String code = stockCode.replaceAll("[^0-9]", "");
        if (code.startsWith("60") || code.startsWith("00") || code.startsWith("30")) {
            return "A";
        } else if (code.startsWith("0") && code.length() == 5) {
            return "H";
        }
        
        return "A";
    }

    /**
     * 确定交易所
     */
    private String determineExchange(String stockCode) {
        if (stockCode == null || stockCode.length() < 6) {
            return "SH";
        }
        
        String code = stockCode.replaceAll("[^0-9]", "");
        if (code.startsWith("60")) {
            return "SH"; // 上海证券交易所
        } else if (code.startsWith("00") || code.startsWith("30")) {
            return "SZ"; // 深圳证券交易所
        } else if (code.startsWith("0") && code.length() == 5) {
            return "HK"; // 香港交易所
        }
        
        return "SH";
    }

    /**
     * 根据股票代码查询股票信息
     */
    public Map<String, Object> getStockInfoByCode(String stockCode) {
        log.info("根据股票代码查询股票信息: {}", stockCode);
        
        try {
            // 腾讯财经股票代码查询API
            String queryUrl = "https://qt.gtimg.cn/q=" + formatStockCode(stockCode);
            
            log.info("调用腾讯财经API: {}", queryUrl);
            String response = restTemplate.getForObject(queryUrl, String.class);
            
            if (response == null || response.trim().isEmpty()) {
                log.warn("腾讯财经API返回空响应，股票代码: {}", stockCode);
                return null;
            }
            
            log.info("腾讯财经API响应: {}", response);
            
            // 解析响应数据
            Map<String, Object> stockInfo = parseStockCodeResponse(response, stockCode);
            
            if (stockInfo != null) {
                log.info("成功解析股票代码 {} 的信息: {}", stockCode, stockInfo);
            } else {
                log.warn("未能解析股票代码 {} 的信息", stockCode);
            }
            
            return stockInfo;
            
        } catch (Exception e) {
            log.error("根据股票代码查询股票信息失败: {}", stockCode, e);
            return null;
        }
    }

    /**
     * 格式化股票代码为腾讯财经API格式
     */
    private String formatStockCode(String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            return stockCode;
        }
        
        String code = stockCode.trim();
        
        // 如果已经包含市场前缀，直接返回
        if (code.startsWith("sh") || code.startsWith("sz") || code.startsWith("hk")) {
            return code;
        }
        
        // 根据代码规则添加市场前缀
        if (code.startsWith("60")) {
            return "sh" + code; // 上海A股
        } else if (code.startsWith("00") || code.startsWith("30")) {
            return "sz" + code; // 深圳A股
        } else if (code.length() == 5 && code.startsWith("0")) {
            return "hk" + code; // 港股
        } else if (code.length() == 5) {
            return "hk" + code; // 默认港股
        }
        
        // 默认上海A股
        return "sh" + code;
    }

    /**
     * 解析股票代码查询的响应
     */
    private Map<String, Object> parseStockCodeResponse(String response, String stockCode) {
        try {
            // 腾讯财经API返回格式: v_股票代码="数据";
            if (!response.contains("=") || !response.contains("\"")) {
                log.warn("股票代码 {} 的响应格式不正确: {}", stockCode, response);
                return null;
            }
            
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.contains("\"")) {
                    // 提取股票数据
                    int startIndex = line.indexOf("\"");
                    int endIndex = line.lastIndexOf("\"");
                    
                    if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                        String dataStr = line.substring(startIndex + 1, endIndex);
                        String[] fields = dataStr.split("~");
                        
                        if (fields.length >= 10) {
                            Map<String, Object> stockInfo = new HashMap<>();
                            stockInfo.put("stock_name", fields[1]); // 股票名称
                            stockInfo.put("a_stock_code", extractPureCode(fields[2])); // A股代码
                            stockInfo.put("current_price", fields[3]); // 当前价格
                            stockInfo.put("change_percent", fields.length > 32 ? fields[32] : "0"); // 涨跌幅
                            
                            // 设置市场类型和交易所
                            String pureCode = extractPureCode(fields[2]);
                            stockInfo.put("market_type", determineMarketType(pureCode));
                            stockInfo.put("exchange", determineExchange(pureCode));
                            
                            // 尝试匹配H股代码
                            String hStockCode = getCorrespondingHStockCode(pureCode);
                            if (hStockCode != null && !hStockCode.trim().isEmpty()) {
                                stockInfo.put("h_stock_code", hStockCode);
                                stockInfo.put("market_type", "A+H");
                                stockInfo.put("exchange", "SH/HK");
                            } else {
                                stockInfo.put("h_stock_code", "");
                            }
                            
                            // 设置行业
                            stockInfo.put("industry", determineIndustry(fields[1]));
                            stockInfo.put("status", 1);
                            stockInfo.put("deleted", 0);
                            
                            return stockInfo;
                        }
                    }
                }
            }
            
            log.warn("无法解析股票代码 {} 的响应数据", stockCode);
            return null;
            
        } catch (Exception e) {
            log.error("解析股票代码 {} 的响应失败", stockCode, e);
            return null;
        }
    }

    /**
     * 提取纯股票代码（去除市场前缀）
     */
    private String extractPureCode(String fullCode) {
        if (fullCode == null || fullCode.trim().isEmpty()) {
            return fullCode;
        }
        
        String code = fullCode.trim();
        if (code.startsWith("sh") || code.startsWith("sz") || code.startsWith("hk")) {
            return code.substring(2);
        }
        
        return code;
    }

    /**
     * 获取股票详细信息（包含A+H股信息）
     */
    public Map<String, Object> getStockDetailInfo(String stockName) {
        log.info("获取股票详细信息: {}", stockName);
        
        try {
            // 调用腾讯财经API搜索股票信息
            Map<String, Object> aStockInfo = searchStockByName(stockName);
            
            if (aStockInfo == null) {
                log.warn("未从腾讯财经API找到股票信息: {}", stockName);
                return null;
            }
            
            // 构建完整的股票信息
            Map<String, Object> detailInfo = new HashMap<>();
            detailInfo.put("stock_name", stockName);
            
            // 获取A股代码
            String aStockCode = (String) aStockInfo.get("stock_code");
            if (aStockCode == null || aStockCode.trim().isEmpty()) {
                log.warn("API返回的股票代码为空: {}", stockName);
                return null;
            }
            
            detailInfo.put("a_stock_code", aStockCode);
            log.info("从API获取到A股代码: {}", aStockCode);
            
            // 尝试匹配对应的H股代码
            String hStockCode = getCorrespondingHStockCode(aStockCode);
            if (hStockCode != null && !hStockCode.trim().isEmpty()) {
                detailInfo.put("h_stock_code", hStockCode);
                detailInfo.put("market_type", "A+H");
                detailInfo.put("exchange", "SH/HK");
                log.info("匹配到H股代码: {}，设置为A+H股", hStockCode);
            } else {
                detailInfo.put("h_stock_code", "");
                detailInfo.put("market_type", "A");
                detailInfo.put("exchange", determineExchange(aStockCode));
                log.info("未匹配到H股代码，设置为A股");
            }
            
            detailInfo.put("industry", determineIndustry(stockName));
            detailInfo.put("status", 1);
            detailInfo.put("deleted", 0);
            
            log.info("构建的完整股票信息: {}", detailInfo);
            return detailInfo;
            
        } catch (Exception e) {
            log.error("获取股票详细信息失败: {}", stockName, e);
            return null;
        }
    }


    /**
     * 获取对应的H股代码
     */
    private String getCorrespondingHStockCode(String aStockCode) {
        if (aStockCode == null || aStockCode.trim().isEmpty()) {
            return null;
        }
        
        // 常见A+H股对应关系
        Map<String, String> ahMapping = new HashMap<>();
        ahMapping.put("601939", "00939"); // 中国建设银行
        ahMapping.put("600036", "03968"); // 招商银行
        ahMapping.put("601288", "01288"); // 中国农业银行
        ahMapping.put("601088", "01088"); // 中国神华
        ahMapping.put("601318", "02318"); // 中国平安
        ahMapping.put("600028", "00386"); // 中国石化
        ahMapping.put("601857", "00857"); // 中国石油
        ahMapping.put("601398", "01398"); // 工商银行
        ahMapping.put("601988", "03988"); // 中国银行
        ahMapping.put("601628", "02628"); // 中国人寿
        ahMapping.put("600919", "00753"); // 江苏银行
        ahMapping.put("601766", "06886"); // 中国中车
        ahMapping.put("601186", "03968"); // 中国铁建
        ahMapping.put("600585", "00753"); // 海螺水泥
        ahMapping.put("601328", "03328"); // 交通银行
        
        String hCode = ahMapping.get(aStockCode);
        log.info("A股代码 {} 对应的H股代码: {}", aStockCode, hCode);
        
        return hCode;
    }

    /**
     * 确定行业
     */
    private String determineIndustry(String stockName) {
        if (stockName.contains("银行")) {
            return "银行";
        } else if (stockName.contains("保险") || stockName.contains("平安")) {
            return "保险";
        } else if (stockName.contains("石油") || stockName.contains("石化")) {
            return "石油化工";
        } else if (stockName.contains("神华") || stockName.contains("煤炭")) {
            return "煤炭开采";
        } else if (stockName.contains("电力") || stockName.contains("能源")) {
            return "电力";
        } else if (stockName.contains("地产") || stockName.contains("房地产")) {
            return "房地产";
        }
        
        return "综合";
    }
}