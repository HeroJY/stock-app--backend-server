package com.stock.premium.controller;

import com.stock.premium.service.DataCollectionService;
import com.stock.premium.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据采集控制器
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Api(tags = "数据采集接口")
@RestController
@RequestMapping("/api/data-collection")
public class DataCollectionController {

    @Autowired
    private DataCollectionService dataCollectionService;

    @ApiOperation("手动触发股票数据采集")
    @PostMapping("/stock-data")
    public Result<String> collectStockData() {
        try {
            log.info("手动触发股票数据采集");
            
            // 调用数据采集服务进行股票数据采集
            dataCollectionService.collectAllStockData();
            
            log.info("股票数据采集任务执行完成");
            return Result.success("股票数据采集任务执行完成");
        } catch (Exception e) {
            log.error("股票数据采集失败", e);
            return Result.error("数据采集失败: " + e.getMessage());
        }
    }
}