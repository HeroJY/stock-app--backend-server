package com.stock.premium.controller;

import com.stock.premium.utils.CloudBaseSyncUtil;
import com.stock.premium.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * CloudBase数据同步控制器
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/cloudbase")
public class CloudBaseSyncController {

    @Autowired
    private CloudBaseSyncUtil cloudBaseSyncUtil;

    @PostMapping("/sync-to-cloudbase")
    public Result<Object> syncToCloudBase() {
        try {
            log.info("开始同步本地数据到CloudBase数据库");
            Object result = cloudBaseSyncUtil.syncLocalDataToCloudBase();
            return Result.success("数据同步完成", result);
        } catch (Exception e) {
            log.error("同步数据到CloudBase异常", e);
            return Result.error("数据同步失败: " + e.getMessage());
        }
    }

    @PostMapping("/sync-from-cloudbase")
    public Result<Object> syncFromCloudBase() {
        try {
            log.info("开始从CloudBase数据库同步数据到本地");
            Object result = cloudBaseSyncUtil.syncCloudBaseDataToLocal();
            return Result.success("数据同步完成", result);
        } catch (Exception e) {
            log.error("从CloudBase同步数据异常", e);
            return Result.error("数据同步失败: " + e.getMessage());
        }
    }

    @GetMapping("/query-cloudbase-stocks")
    public Result<Object> queryCloudBaseStocks() {
        try {
            log.info("开始查询CloudBase数据库中的股票信息");
            Object data = cloudBaseSyncUtil.queryCloudBaseStockData();
            return Result.success("查询成功", data);
        } catch (Exception e) {
            log.error("查询CloudBase股票信息异常", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/sync-stock-to-cloudbase")
    public Result<Object> syncStockToCloudBase(@RequestParam String stockCode) {
        try {
            log.info("开始同步指定股票信息到CloudBase: {}", stockCode);
            
            if (stockCode == null || stockCode.trim().isEmpty()) {
                return Result.error("股票代码不能为空");
            }
            
            Object result = cloudBaseSyncUtil.syncSpecificStockToCloudBase(stockCode.trim());
            return Result.success("股票信息同步完成", result);
            
        } catch (Exception e) {
            log.error("同步股票信息到CloudBase异常: {}", stockCode, e);
            return Result.error("同步失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch-sync-to-cloudbase")
    public Result<Object> batchSyncToCloudBase(@RequestBody java.util.List<String> stockCodes) {
        try {
            log.info("开始批量同步股票信息到CloudBase: {}", stockCodes);
            
            if (stockCodes == null || stockCodes.isEmpty()) {
                return Result.error("股票代码列表不能为空");
            }
            
            // 过滤空值
            java.util.List<String> validCodes = stockCodes.stream()
                    .filter(code -> code != null && !code.trim().isEmpty())
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toList());
            
            if (validCodes.isEmpty()) {
                return Result.error("没有有效的股票代码");
            }
            
            Object result = cloudBaseSyncUtil.batchSyncStocksToCloudBase(validCodes);
            return Result.success("批量同步完成", result);
            
        } catch (Exception e) {
            log.error("批量同步股票信息到CloudBase异常", e);
            return Result.error("批量同步失败: " + e.getMessage());
        }
    }

    @GetMapping("/check-connection")
    public Result<Object> checkConnection() {
        try {
            log.info("检查CloudBase数据库连接状态");
            Object status = cloudBaseSyncUtil.checkCloudBaseConnection();
            return Result.success("连接检查完成", status);
        } catch (Exception e) {
            log.error("检查CloudBase连接状态失败", e);
            return Result.error("连接检查失败: " + e.getMessage());
        }
    }

    @GetMapping("/sync-status")
    public Result<Object> getSyncStatus() {
        try {
            log.info("获取CloudBase数据同步状态");
            Object status = cloudBaseSyncUtil.getSyncStatus();
            return Result.success("状态获取成功", status);
        } catch (Exception e) {
            log.error("获取同步状态失败", e);
            return Result.error("状态获取失败: " + e.getMessage());
        }
    }
}