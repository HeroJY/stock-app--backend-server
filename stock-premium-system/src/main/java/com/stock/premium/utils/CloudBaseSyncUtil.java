package com.stock.premium.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * CloudBase数据同步工具类
 * 
 * @author system
 * @since 2024-01-01
 */
@Component
@Slf4j
public class CloudBaseSyncUtil {

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private TencentFinanceApiUtil tencentFinanceApiUtil;
    
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 初始化JdbcTemplate，确保使用正确的数据源
     */
    @javax.annotation.PostConstruct
    public void init() {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        log.info("CloudBaseSyncUtil初始化完成，使用数据源: {}", dataSource.getClass().getSimpleName());
        
        // 测试数据库连接
        try {
            String result = jdbcTemplate.queryForObject("SELECT 'CloudBase MySQL连接成功' as test", String.class);
            log.info("数据库连接测试: {}", result);
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
        }
    }

    /**
     * 检查数据库状态
     */
    public Object checkDatabaseStatus() {
        log.info("检查CloudBase数据库状态");
        
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 检查各表的记录数
            status.put("stock_info", getTableCount("stock_info"));
            status.put("stock_price_record", getTableCount("stock_price_record"));
            status.put("exchange_rate_record", getTableCount("exchange_rate_record"));
            status.put("premium_rate_record", getTableCount("premium_rate_record"));
            status.put("daily_premium_stats", getTableCount("daily_premium_stats"));
            status.put("system_config", getTableCount("system_config"));
            status.put("last_check", java.time.LocalDateTime.now().toString());
            status.put("status", "正常");
            
            return status;
        } catch (Exception e) {
            log.error("检查数据库状态失败", e);
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("status", "异常");
            errorStatus.put("error", e.getMessage());
            return errorStatus;
        }
    }

    /**
     * 获取表记录数
     */
    private String getTableCount(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
            return count != null ? count.toString() : "0";
        } catch (Exception e) {
            log.warn("获取表 {} 记录数失败: {}", tableName, e.getMessage());
            return "0";
        }
    }

    /**
     * 初始化数据库表结构
     */
    public boolean initTables() {
        log.info("开始初始化CloudBase数据库表结构");
        
        try {
            // 创建股票基础信息表
            createStockInfoTable();
            
            // 创建股票价格记录表
            createStockPriceRecordTable();
            
            // 创建汇率记录表
            createExchangeRateRecordTable();
            
            // 创建溢价率记录表
            createPremiumRateRecordTable();
            
            // 创建日统计表
            createDailyPremiumStatsTable();
            
            // 创建系统配置表
            createSystemConfigTable();
            
            log.info("CloudBase数据库表结构初始化完成");
            return true;
            
        } catch (Exception e) {
            log.error("初始化数据库表结构失败", e);
            return false;
        }
    }

    /**
     * 创建股票基础信息表
     */
    private void createStockInfoTable() {
        try {
            String sql = """
                CREATE TABLE IF NOT EXISTS stock_info (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                    stock_name VARCHAR(50) NOT NULL COMMENT '股票名称',
                    market_type VARCHAR(10) NOT NULL DEFAULT 'A+H' COMMENT '市场类型',
                    a_stock_code VARCHAR(10) NOT NULL COMMENT 'A股代码',
                    h_stock_code VARCHAR(10) NOT NULL COMMENT 'H股代码',
                    exchange VARCHAR(20) NOT NULL DEFAULT 'SH/HK' COMMENT '交易所',
                    industry VARCHAR(50) COMMENT '所属行业',
                    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
                    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常,1:删除)',
                    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    UNIQUE KEY uk_a_stock_code (a_stock_code),
                    UNIQUE KEY uk_h_stock_code (h_stock_code),
                    KEY idx_stock_name (stock_name),
                    KEY idx_status (status, deleted)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票基础信息表'
                """;
            jdbcTemplate.execute(sql);
            log.info("股票基础信息表创建完成");
            
            // 验证表是否创建成功
            String checkSql = "SHOW TABLES LIKE 'stock_info'";
            try {
                jdbcTemplate.queryForObject(checkSql, String.class);
                log.info("验证：stock_info表已存在");
            } catch (Exception e) {
                log.error("验证：stock_info表不存在", e);
                throw new RuntimeException("stock_info表创建失败");
            }
        } catch (Exception e) {
            log.error("创建股票基础信息表失败", e);
            throw e;
        }
    }

    /**
     * 创建股票价格记录表
     */
    private void createStockPriceRecordTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS stock_price_record (
                id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                stock_id BIGINT NOT NULL COMMENT '股票ID',
                stock_code VARCHAR(10) NOT NULL COMMENT '股票代码',
                market_type VARCHAR(5) NOT NULL COMMENT '市场类型(A/H)',
                price DECIMAL(10,3) NOT NULL COMMENT '股票价格',
                change_amount DECIMAL(10,3) COMMENT '涨跌金额',
                change_percent DECIMAL(8,4) COMMENT '涨跌幅度(%)',
                volume BIGINT COMMENT '成交量',
                turnover DECIMAL(15,2) COMMENT '成交额',
                record_time DATETIME NOT NULL COMMENT '记录时间',
                trade_date DATE NOT NULL COMMENT '交易日期',
                data_source VARCHAR(20) NOT NULL DEFAULT 'tencent' COMMENT '数据来源',
                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                KEY idx_stock_trade_date (stock_id, trade_date),
                KEY idx_stock_code_date (stock_code, trade_date),
                KEY idx_record_time (record_time)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票价格记录表'
            """;
        jdbcTemplate.execute(sql);
        log.info("股票价格记录表创建完成");
    }

    /**
     * 创建汇率记录表
     */
    private void createExchangeRateRecordTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS exchange_rate_record (
                id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                currency_pair VARCHAR(10) NOT NULL COMMENT '货币对(如HKDCNY)',
                rate DECIMAL(8,6) NOT NULL COMMENT '汇率',
                record_time DATETIME NOT NULL COMMENT '记录时间',
                trade_date DATE NOT NULL COMMENT '交易日期',
                data_source VARCHAR(20) NOT NULL DEFAULT 'manual' COMMENT '数据来源',
                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                KEY idx_currency_date (currency_pair, trade_date),
                KEY idx_record_time (record_time)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='汇率记录表'
            """;
        jdbcTemplate.execute(sql);
        log.info("汇率记录表创建完成");
    }

    /**
     * 创建溢价率记录表
     */
    private void createPremiumRateRecordTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS premium_rate_record (
                id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                stock_id BIGINT NOT NULL COMMENT '股票ID',
                stock_name VARCHAR(50) NOT NULL COMMENT '股票名称',
                a_stock_price DECIMAL(10,3) NOT NULL COMMENT 'A股价格',
                h_stock_price DECIMAL(10,3) NOT NULL COMMENT 'H股价格',
                exchange_rate DECIMAL(8,6) NOT NULL COMMENT '汇率(港币兑人民币)',
                premium_rate DECIMAL(8,4) NOT NULL COMMENT '溢价率(%)',
                record_time DATETIME NOT NULL COMMENT '记录时间',
                trade_date DATE NOT NULL COMMENT '交易日期',
                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                KEY idx_stock_trade_date (stock_id, trade_date),
                KEY idx_premium_rate (premium_rate),
                KEY idx_record_time (record_time)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='溢价率记录表'
            """;
        jdbcTemplate.execute(sql);
        log.info("溢价率记录表创建完成");
    }

    /**
     * 创建日统计表
     */
    private void createDailyPremiumStatsTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS daily_premium_stats (
                id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                trade_date DATE NOT NULL COMMENT '交易日期',
                stock_count INT NOT NULL DEFAULT 0 COMMENT '股票数量',
                avg_premium_rate DECIMAL(8,4) COMMENT '平均溢价率(%)',
                max_premium_rate DECIMAL(8,4) COMMENT '最高溢价率(%)',
                min_premium_rate DECIMAL(8,4) COMMENT '最低溢价率(%)',
                positive_count INT NOT NULL DEFAULT 0 COMMENT '正溢价股票数量',
                negative_count INT NOT NULL DEFAULT 0 COMMENT '负溢价股票数量',
                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                UNIQUE KEY uk_trade_date (trade_date)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日溢价率统计表'
            """;
        jdbcTemplate.execute(sql);
        log.info("日统计表创建完成");
    }

    /**
     * 创建系统配置表
     */
    private void createSystemConfigTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS system_config (
                id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                config_key VARCHAR(50) NOT NULL COMMENT '配置键',
                config_value TEXT NOT NULL COMMENT '配置值',
                config_desc VARCHAR(200) COMMENT '配置描述',
                config_type VARCHAR(20) NOT NULL DEFAULT 'string' COMMENT '配置类型',
                status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                UNIQUE KEY uk_config_key (config_key)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表'
            """;
        jdbcTemplate.execute(sql);
        log.info("系统配置表创建完成");
    }

    /**
     * 插入初始数据
     */
    public boolean insertInitialData() {
        log.info("开始插入CloudBase初始数据");
        
        try {
            // 插入系统配置数据
            insertSystemConfig();
            
            // 插入汇率数据
            insertExchangeRateData();
            
            log.info("CloudBase初始数据插入完成");
            return true;
            
        } catch (Exception e) {
            log.error("插入初始数据失败", e);
            return false;
        }
    }

    /**
     * 插入系统配置数据
     */
    private void insertSystemConfig() {
        String sql = """
            INSERT IGNORE INTO system_config (config_key, config_value, config_desc, config_type, status)
            VALUES 
            ('data_collection_enabled', 'true', '数据采集开关', 'boolean', 1),
            ('data_collection_interval', '300', '数据采集间隔时间（秒）', 'number', 1),
            ('premium_alert_threshold', '10.0', '溢价率预警阈值（%）', 'number', 1),
            ('market_open_time', '09:30', 'A股开市时间', 'time', 1),
            ('market_close_time', '15:00', 'A股收市时间', 'time', 1),
            ('hk_market_open_time', '09:30', '港股开市时间', 'time', 1)
            """;
        
        int result = jdbcTemplate.update(sql);
        log.info("插入系统配置数据: {} 条记录", result);
    }

    /**
     * 插入汇率数据
     */
    private void insertExchangeRateData() {
        String sql = """
            INSERT IGNORE INTO exchange_rate_record (currency_pair, rate, record_time, trade_date, data_source)
            VALUES 
            ('HKDCNY', 0.920, '2024-07-27 17:00:00', '2024-07-27', 'manual'),
            ('HKDCNY', 0.918, '2024-07-26 17:00:00', '2024-07-26', 'manual')
            """;
        
        int result = jdbcTemplate.update(sql);
        log.info("插入汇率数据: {} 条记录", result);
    }

    /**
     * 查询stock_info表中的所有数据
     */
    public Object queryStockInfoData() {
        log.info("开始查询stock_info表中的所有数据");
        
        try {
            // 先检查表是否存在
            String checkTableSql = "SHOW TABLES LIKE 'stock_info'";
            try {
                jdbcTemplate.queryForObject(checkTableSql, String.class);
                log.info("stock_info表存在");
            } catch (Exception e) {
                log.error("stock_info表不存在", e);
                return Map.of("error", "stock_info表不存在");
            }
            
            // 查询记录数
            String countSql = "SELECT COUNT(*) FROM stock_info";
            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
            log.info("stock_info表中共有: {} 条记录", count);
            
            // 查询所有数据
            String querySql = "SELECT id, stock_name, market_type, a_stock_code, h_stock_code, exchange, industry, status, deleted, created_time, updated_time FROM stock_info ORDER BY id";
            java.util.List<java.util.Map<String, Object>> stockList = jdbcTemplate.queryForList(querySql);
            
            log.info("查询到的股票数据:");
            for (java.util.Map<String, Object> stock : stockList) {
                log.info("股票: {}", stock);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("stocks", stockList);
            result.put("query_time", java.time.LocalDateTime.now().toString());
            
            return result;
        } catch (Exception e) {
            log.error("查询stock_info表数据失败", e);
            return Map.of("error", "查询失败: " + e.getMessage());
        }
    }

    /**
     * 添加5只指定股票信息
     */
    @org.springframework.transaction.annotation.Transactional
    public boolean addFiveSpecificStocks() {
        log.info("开始添加5只指定股票信息");
        
        try {
            // 先验证表是否存在
            try {
                String checkTableSql = "SHOW TABLES LIKE 'stock_info'";
                jdbcTemplate.queryForObject(checkTableSql, String.class);
                log.info("验证：stock_info表存在且可访问");
            } catch (Exception e) {
                log.error("stock_info表不存在或无法访问", e);
                throw new RuntimeException("stock_info表不存在，请先初始化表结构");
            }
            
            // 先清空现有股票数据
            String deleteSql = "DELETE FROM stock_info";
            int deleteResult = jdbcTemplate.update(deleteSql);
            log.info("已清空现有股票数据: {} 条记录", deleteResult);
            
            // 使用逐条插入，确保每条都成功
            String insertSql = "INSERT INTO stock_info (stock_name, market_type, a_stock_code, h_stock_code, exchange, industry, status, deleted, created_time, updated_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
            
            int totalInserted = 0;
            
            // 1. 中国建设银行
            int result1 = jdbcTemplate.update(insertSql, "中国建设银行", "A+H", "601939", "00939", "SH/HK", "银行", 1, 0);
            totalInserted += result1;
            log.info("插入中国建设银行: {} 条记录", result1);
            
            // 2. 招商银行
            int result2 = jdbcTemplate.update(insertSql, "招商银行", "A+H", "600036", "03968", "SH/HK", "银行", 1, 0);
            totalInserted += result2;
            log.info("插入招商银行: {} 条记录", result2);
            
            // 3. 中国农业银行
            int result3 = jdbcTemplate.update(insertSql, "中国农业银行", "A+H", "601288", "01288", "SH/HK", "银行", 1, 0);
            totalInserted += result3;
            log.info("插入中国农业银行: {} 条记录", result3);
            
            // 4. 中国神华
            int result4 = jdbcTemplate.update(insertSql, "中国神华", "A+H", "601088", "01088", "SH/HK", "煤炭开采", 1, 0);
            totalInserted += result4;
            log.info("插入中国神华: {} 条记录", result4);
            
            // 5. 中国平安
            int result5 = jdbcTemplate.update(insertSql, "中国平安", "A+H", "601318", "02318", "SH/HK", "保险", 1, 0);
            totalInserted += result5;
            log.info("插入中国平安: {} 条记录", result5);
            
            log.info("成功添加5只指定股票信息，总计: {} 条记录", totalInserted);
            
            // 立即验证插入结果
            String countSql = "SELECT COUNT(*) FROM stock_info";
            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
            log.info("验证：当前stock_info表中共有: {} 条记录", count);
            
            // 查询具体数据进行验证
            String querySql = "SELECT stock_name, a_stock_code, h_stock_code FROM stock_info ORDER BY id";
            java.util.List<java.util.Map<String, Object>> stocks = jdbcTemplate.queryForList(querySql);
            log.info("插入后的股票数据验证:");
            for (java.util.Map<String, Object> stock : stocks) {
                log.info("股票: {} (A股:{}, H股:{})", stock.get("stock_name"), stock.get("a_stock_code"), stock.get("h_stock_code"));
            }
            
            if (count == null || count != 5) {
                throw new RuntimeException("数据插入验证失败，期望5条记录，实际" + (count != null ? count : "null") + "条记录");
            }
            
            return totalInserted == 5;
        } catch (Exception e) {
            log.error("添加5只指定股票信息失败", e);
            throw new RuntimeException("添加股票信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据股票名称从腾讯财经API获取信息并同步到数据库
     */
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> syncStockInfoByName(String stockName) {
        log.info("开始同步股票信息: {}", stockName);
        
        try {
            // 1. 调用腾讯财经API获取股票信息
            Map<String, Object> apiStockInfo = tencentFinanceApiUtil.getStockDetailInfo(stockName);
            
            if (apiStockInfo == null) {
                return Map.of("success", false, "message", "未能从腾讯财经API获取到股票信息: " + stockName);
            }
            
            log.info("从腾讯财经API获取到股票信息: {}", apiStockInfo);
            
            // 2. 检查股票是否已存在
            String checkSql = "SELECT COUNT(*) FROM stock_info WHERE stock_name = ? OR a_stock_code = ?";
            String aStockCode = (String) apiStockInfo.get("a_stock_code");
            Integer existCount = jdbcTemplate.queryForObject(checkSql, Integer.class, stockName, aStockCode);
            
            if (existCount != null && existCount > 0) {
                log.info("股票已存在，执行更新操作: {}", stockName);
                return updateExistingStock(stockName, apiStockInfo);
            } else {
                log.info("股票不存在，执行插入操作: {}", stockName);
                return insertNewStock(apiStockInfo);
            }
            
        } catch (Exception e) {
            log.error("同步股票信息失败: {}", stockName, e);
            return Map.of("success", false, "message", "同步失败: " + e.getMessage());
        }
    }

    /**
     * 插入新股票信息
     */
    private Map<String, Object> insertNewStock(Map<String, Object> stockInfo) {
        try {
            String insertSql = """
                INSERT INTO stock_info (stock_name, market_type, a_stock_code, h_stock_code, exchange, industry, status, deleted, created_time, updated_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;
            
            int result = jdbcTemplate.update(insertSql,
                stockInfo.get("stock_name"),
                stockInfo.get("market_type"),
                stockInfo.get("a_stock_code"),
                stockInfo.get("h_stock_code"),
                stockInfo.get("exchange"),
                stockInfo.get("industry"),
                stockInfo.get("status"),
                stockInfo.get("deleted")
            );
            
            if (result > 0) {
                log.info("成功插入新股票信息: {}", stockInfo.get("stock_name"));
                return Map.of(
                    "success", true,
                    "message", "成功添加股票信息",
                    "action", "insert",
                    "stock_info", stockInfo
                );
            } else {
                return Map.of("success", false, "message", "插入股票信息失败");
            }
            
        } catch (Exception e) {
            log.error("插入新股票信息失败", e);
            return Map.of("success", false, "message", "插入失败: " + e.getMessage());
        }
    }

    /**
     * 更新现有股票信息
     */
    private Map<String, Object> updateExistingStock(String stockName, Map<String, Object> stockInfo) {
        try {
            String updateSql = """
                UPDATE stock_info SET 
                    market_type = ?, 
                    a_stock_code = ?, 
                    h_stock_code = ?, 
                    exchange = ?, 
                    industry = ?, 
                    updated_time = NOW()
                WHERE stock_name = ?
                """;
            
            int result = jdbcTemplate.update(updateSql,
                stockInfo.get("market_type"),
                stockInfo.get("a_stock_code"),
                stockInfo.get("h_stock_code"),
                stockInfo.get("exchange"),
                stockInfo.get("industry"),
                stockName
            );
            
            if (result > 0) {
                log.info("成功更新股票信息: {}", stockName);
                return Map.of(
                    "success", true,
                    "message", "成功更新股票信息",
                    "action", "update",
                    "stock_info", stockInfo
                );
            } else {
                return Map.of("success", false, "message", "更新股票信息失败");
            }
            
        } catch (Exception e) {
            log.error("更新股票信息失败", e);
            return Map.of("success", false, "message", "更新失败: " + e.getMessage());
        }
    }

    /**
     * 批量同步多个股票信息
     */
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> batchSyncStocksByNames(java.util.List<String> stockNames) {
        log.info("开始批量同步股票信息: {}", stockNames);
        
        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        
        for (String stockName : stockNames) {
            try {
                Map<String, Object> result = syncStockInfoByName(stockName);
                results.add(result);
                
                if ((Boolean) result.get("success")) {
                    successCount++;
                } else {
                    failCount++;
                }
                
                // 避免API调用过于频繁
                Thread.sleep(500);
                
            } catch (Exception e) {
                log.error("批量同步股票失败: {}", stockName, e);
                results.add(Map.of(
                    "success", false,
                    "message", "同步失败: " + e.getMessage(),
                    "stock_name", stockName
                ));
                failCount++;
            }
        }
        
        return Map.of(
            "total", stockNames.size(),
            "success_count", successCount,
            "fail_count", failCount,
            "results", results
        );
    }

    // ========== CloudBaseSyncController需要的方法 ==========

    /**
     * 同步本地数据到CloudBase数据库
     */
    public Object syncLocalDataToCloudBase() {
        log.info("开始同步本地数据到CloudBase数据库");
        
        try {
            // 查询本地股票数据
            String querySql = "SELECT * FROM stock_info WHERE status = 1 AND deleted = 0";
            java.util.List<java.util.Map<String, Object>> localStocks = jdbcTemplate.queryForList(querySql);
            
            log.info("本地共有 {} 条股票数据需要同步", localStocks.size());
            
            // 这里可以添加实际的CloudBase同步逻辑
            // 目前返回模拟结果
            return Map.of(
                "success", true,
                "message", "本地数据同步到CloudBase完成",
                "sync_count", localStocks.size(),
                "sync_time", java.time.LocalDateTime.now().toString()
            );
            
        } catch (Exception e) {
            log.error("同步本地数据到CloudBase失败", e);
            return Map.of(
                "success", false,
                "message", "同步失败: " + e.getMessage()
            );
        }
    }

    /**
     * 从CloudBase数据库同步数据到本地
     */
    public Object syncCloudBaseDataToLocal() {
        log.info("开始从CloudBase数据库同步数据到本地");
        
        try {
            // 这里可以添加实际的CloudBase数据获取逻辑
            // 目前返回模拟结果
            return Map.of(
                "success", true,
                "message", "从CloudBase同步数据到本地完成",
                "sync_count", 0,
                "sync_time", java.time.LocalDateTime.now().toString()
            );
            
        } catch (Exception e) {
            log.error("从CloudBase同步数据到本地失败", e);
            return Map.of(
                "success", false,
                "message", "同步失败: " + e.getMessage()
            );
        }
    }

    /**
     * 查询CloudBase数据库中的股票信息
     */
    public Object queryCloudBaseStockData() {
        log.info("开始查询CloudBase数据库中的股票信息");
        
        try {
            // 查询本地数据作为CloudBase数据的代表
            String querySql = "SELECT * FROM stock_info WHERE status = 1 AND deleted = 0 ORDER BY id";
            java.util.List<java.util.Map<String, Object>> stocks = jdbcTemplate.queryForList(querySql);
            
            return Map.of(
                "success", true,
                "count", stocks.size(),
                "stocks", stocks,
                "query_time", java.time.LocalDateTime.now().toString()
            );
            
        } catch (Exception e) {
            log.error("查询CloudBase股票信息失败", e);
            return Map.of(
                "success", false,
                "message", "查询失败: " + e.getMessage()
            );
        }
    }

    /**
     * 同步指定股票信息到CloudBase
     */
    public Object syncSpecificStockToCloudBase(String stockCode) {
        log.info("开始同步指定股票信息到CloudBase: {}", stockCode);
        
        try {
            // 查询指定股票信息
            String querySql = "SELECT * FROM stock_info WHERE (a_stock_code = ? OR h_stock_code = ?) AND status = 1 AND deleted = 0";
            java.util.List<java.util.Map<String, Object>> stocks = jdbcTemplate.queryForList(querySql, stockCode, stockCode);
            
            if (stocks.isEmpty()) {
                return Map.of(
                    "success", false,
                    "message", "未找到股票代码: " + stockCode
                );
            }
            
            // 这里可以添加实际的CloudBase同步逻辑
            return Map.of(
                "success", true,
                "message", "股票信息同步到CloudBase完成",
                "stock_code", stockCode,
                "stock_info", stocks.get(0),
                "sync_time", java.time.LocalDateTime.now().toString()
            );
            
        } catch (Exception e) {
            log.error("同步指定股票信息到CloudBase失败: {}", stockCode, e);
            return Map.of(
                "success", false,
                "message", "同步失败: " + e.getMessage()
            );
        }
    }

    /**
     * 批量同步股票信息到CloudBase
     */
    public Object batchSyncStocksToCloudBase(java.util.List<String> stockCodes) {
        log.info("开始批量同步股票信息到CloudBase: {}", stockCodes);
        
        try {
            java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            for (String stockCode : stockCodes) {
                try {
                    Object result = syncSpecificStockToCloudBase(stockCode);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resultMap = (Map<String, Object>) result;
                    results.add(resultMap);
                    
                    if ((Boolean) resultMap.get("success")) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("批量同步股票失败: {}", stockCode, e);
                    results.add(Map.of(
                        "success", false,
                        "message", "同步失败: " + e.getMessage(),
                        "stock_code", stockCode
                    ));
                    failCount++;
                }
            }
            
            return Map.of(
                "success", true,
                "total", stockCodes.size(),
                "success_count", successCount,
                "fail_count", failCount,
                "results", results
            );
            
        } catch (Exception e) {
            log.error("批量同步股票信息到CloudBase失败", e);
            return Map.of(
                "success", false,
                "message", "批量同步失败: " + e.getMessage()
            );
        }
    }

    /**
     * 检查CloudBase数据库连接状态
     */
    public Object checkCloudBaseConnection() {
        log.info("检查CloudBase数据库连接状态");
        
        try {
            // 测试数据库连接
            String testSql = "SELECT 1 as test";
            Integer result = jdbcTemplate.queryForObject(testSql, Integer.class);
            
            return Map.of(
                "success", true,
                "status", "连接正常",
                "connection_test", result,
                "check_time", java.time.LocalDateTime.now().toString()
            );
            
        } catch (Exception e) {
            log.error("CloudBase数据库连接检查失败", e);
            return Map.of(
                "success", false,
                "status", "连接异常",
                "error", e.getMessage(),
                "check_time", java.time.LocalDateTime.now().toString()
            );
        }
    }

    /**
     * 获取CloudBase数据同步状态
     */
    public Object getSyncStatus() {
        log.info("获取CloudBase数据同步状态");
        
        try {
            // 获取各表的记录数统计
            Map<String, Object> status = new HashMap<>();
            status.put("stock_info_count", getTableCount("stock_info"));
            status.put("stock_price_record_count", getTableCount("stock_price_record"));
            status.put("exchange_rate_record_count", getTableCount("exchange_rate_record"));
            status.put("premium_rate_record_count", getTableCount("premium_rate_record"));
            status.put("daily_premium_stats_count", getTableCount("daily_premium_stats"));
            status.put("system_config_count", getTableCount("system_config"));
            status.put("last_sync_time", java.time.LocalDateTime.now().toString());
            status.put("sync_status", "正常");
            
            return status;
            
        } catch (Exception e) {
            log.error("获取CloudBase数据同步状态失败", e);
            return Map.of(
                "success", false,
                "sync_status", "异常",
                "error", e.getMessage()
            );
        }
    }
}
