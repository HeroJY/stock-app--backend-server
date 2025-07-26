-- 创建数据库
CREATE DATABASE IF NOT EXISTS `stock_premium` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `stock_premium`;

-- 1. 股票基础信息表
CREATE TABLE `stock_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stock_code` varchar(20) NOT NULL COMMENT '股票代码',
  `stock_name` varchar(100) NOT NULL COMMENT '股票名称',
  `market_type` varchar(10) NOT NULL COMMENT '市场类型：A股/H股',
  `a_stock_code` varchar(20) DEFAULT NULL COMMENT 'A股代码',
  `h_stock_code` varchar(20) DEFAULT NULL COMMENT 'H股代码',
  `exchange` varchar(20) NOT NULL COMMENT '交易所：SH/SZ/HK',
  `industry` varchar(50) DEFAULT NULL COMMENT '所属行业',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-正常，0-停用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_code_market` (`stock_code`, `market_type`),
  KEY `idx_a_stock_code` (`a_stock_code`),
  KEY `idx_h_stock_code` (`h_stock_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='股票基础信息表';

-- 2. 实时价格记录表
CREATE TABLE `stock_price_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stock_code` varchar(20) NOT NULL COMMENT '股票代码',
  `market_type` varchar(10) NOT NULL COMMENT '市场类型：A股/H股',
  `current_price` decimal(10,3) NOT NULL COMMENT '当前价格',
  `open_price` decimal(10,3) DEFAULT NULL COMMENT '开盘价',
  `high_price` decimal(10,3) DEFAULT NULL COMMENT '最高价',
  `low_price` decimal(10,3) DEFAULT NULL COMMENT '最低价',
  `pre_close_price` decimal(10,3) DEFAULT NULL COMMENT '昨收价',
  `volume` bigint DEFAULT NULL COMMENT '成交量',
  `turnover` decimal(15,2) DEFAULT NULL COMMENT '成交额',
  `change_rate` decimal(6,3) DEFAULT NULL COMMENT '涨跌幅(%)',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `trade_date` date NOT NULL COMMENT '交易日期',
  `data_source` varchar(20) DEFAULT 'tencent' COMMENT '数据来源',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_stock_code_time` (`stock_code`, `record_time`),
  KEY `idx_trade_date` (`trade_date`),
  KEY `idx_market_type` (`market_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实时价格记录表';

-- 3. 汇率记录表
CREATE TABLE `exchange_rate_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `currency_pair` varchar(10) NOT NULL COMMENT '货币对：HKDCNY',
  `rate` decimal(8,6) NOT NULL COMMENT '汇率',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `trade_date` date NOT NULL COMMENT '交易日期',
  `data_source` varchar(20) DEFAULT 'tencent' COMMENT '数据来源',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_currency_time` (`currency_pair`, `record_time`),
  KEY `idx_trade_date` (`trade_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='汇率记录表';

-- 4. 溢价率记录表
CREATE TABLE `premium_rate_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stock_code` varchar(20) NOT NULL COMMENT '股票代码（A股代码）',
  `a_stock_price` decimal(10,3) NOT NULL COMMENT 'A股价格',
  `h_stock_price` decimal(10,3) NOT NULL COMMENT 'H股价格',
  `exchange_rate` decimal(8,6) NOT NULL COMMENT '汇率',
  `premium_rate` decimal(8,4) NOT NULL COMMENT '溢价率(%)',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `trade_date` date NOT NULL COMMENT '交易日期',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_stock_code_time` (`stock_code`, `record_time`),
  KEY `idx_trade_date` (`trade_date`),
  KEY `idx_premium_rate` (`premium_rate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='溢价率记录表';

-- 5. 日统计数据表
CREATE TABLE `daily_premium_stats` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stock_code` varchar(20) NOT NULL COMMENT '股票代码（A股代码）',
  `trade_date` date NOT NULL COMMENT '交易日期',
  `open_premium_rate` decimal(8,4) DEFAULT NULL COMMENT '开盘溢价率(%)',
  `close_premium_rate` decimal(8,4) DEFAULT NULL COMMENT '收盘溢价率(%)',
  `max_premium_rate` decimal(8,4) DEFAULT NULL COMMENT '最高溢价率(%)',
  `min_premium_rate` decimal(8,4) DEFAULT NULL COMMENT '最低溢价率(%)',
  `avg_premium_rate` decimal(8,4) DEFAULT NULL COMMENT '平均溢价率(%)',
  `percentile_95` decimal(8,4) DEFAULT NULL COMMENT '95%分位数',
  `percentile_5` decimal(8,4) DEFAULT NULL COMMENT '5%分位数',
  `record_count` int DEFAULT NULL COMMENT '记录数量',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_date` (`stock_code`, `trade_date`),
  KEY `idx_trade_date` (`trade_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日统计数据表';

-- 6. 系统配置表
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_desc` varchar(200) DEFAULT NULL COMMENT '配置描述',
  `config_type` varchar(20) DEFAULT 'STRING' COMMENT '配置类型',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入初始数据
-- 插入一些测试股票数据
INSERT INTO `stock_info` (`stock_code`, `stock_name`, `market_type`, `a_stock_code`, `h_stock_code`, `exchange`, `industry`, `status`) VALUES
('000001', '平安银行', 'A', '000001', '02318', 'SZ', '银行', 1),
('000002', '万科A', 'A', '000002', '02202', 'SZ', '房地产', 1),
('600036', '招商银行', 'A', '600036', '03968', 'SH', '银行', 1),
('02318', '平安银行', 'H', '000001', '02318', 'HK', '银行', 1),
('02202', '万科企业', 'H', '000002', '02202', 'HK', '房地产', 1),
('03968', '招商银行', 'H', '600036', '03968', 'HK', '银行', 1);

-- 插入系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`, `config_type`) VALUES
('data.collect.enabled', '1', '数据采集开关', 'BOOLEAN'),
('data.collect.interval', '30', '数据采集间隔(秒)', 'INTEGER'),
('trading.start.time', '09:30:00', '交易开始时间', 'TIME'),
('trading.end.time', '16:00:00', '交易结束时间', 'TIME'),
('hk.trading.end.time', '16:00:00', '港股交易结束时间', 'TIME'),
('default.exchange.rate', '0.9', '默认汇率(港币对人民币)', 'DECIMAL');