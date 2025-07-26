-- 数据库迁移脚本：删除stock_info表的stock_code字段

USE `stock_premium`;

-- 1. 删除与stock_code相关的索引
ALTER TABLE `stock_info` DROP INDEX `uk_stock_code_market`;

-- 2. 删除stock_code字段
ALTER TABLE `stock_info` DROP COLUMN `stock_code`;

-- 3. 添加新的唯一索引
ALTER TABLE `stock_info` ADD UNIQUE KEY `uk_a_h_stock_codes` (`a_stock_code`, `h_stock_code`);

-- 4. 清理stock_info表的所有数据
DELETE FROM `stock_info`;

-- 重置自增ID
ALTER TABLE `stock_info` AUTO_INCREMENT = 1;