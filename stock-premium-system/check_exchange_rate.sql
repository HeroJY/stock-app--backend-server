-- 查询汇率数据脚本
USE stock_premium;

-- 1. 查看汇率表结构
DESCRIBE exchange_rate_record;

-- 2. 查询总记录数
SELECT COUNT(*) as '总记录数' FROM exchange_rate_record;

-- 3. 查询最新10条汇率记录
SELECT 
    id as '记录ID',
    currency_pair as '货币对',
    rate as '汇率',
    record_time as '记录时间',
    trade_date as '交易日期',
    data_source as '数据来源',
    created_time as '创建时间'
FROM exchange_rate_record 
ORDER BY record_time DESC 
LIMIT 10;

-- 4. 按货币对分组统计
SELECT 
    currency_pair as '货币对',
    COUNT(*) as '记录数量',
    MIN(rate) as '最低汇率',
    MAX(rate) as '最高汇率',
    AVG(rate) as '平均汇率',
    MAX(record_time) as '最新记录时间'
FROM exchange_rate_record 
GROUP BY currency_pair;

-- 5. 查询今日汇率记录
SELECT 
    currency_pair as '货币对',
    rate as '汇率',
    record_time as '记录时间',
    data_source as '数据来源'
FROM exchange_rate_record 
WHERE trade_date = CURDATE()
ORDER BY record_time DESC;

-- 6. 查询HKDCNY最新汇率
SELECT 
    rate as '港币兑人民币汇率',
    record_time as '记录时间',
    data_source as '数据来源'
FROM exchange_rate_record 
WHERE currency_pair = 'HKDCNY'
ORDER BY record_time DESC 
LIMIT 1;