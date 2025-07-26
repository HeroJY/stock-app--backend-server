package com.stock.premium.controller;

import com.stock.premium.entity.StockInfo;
import com.stock.premium.service.StockInfoService;
import com.stock.premium.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 股票信息控制器
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Api(tags = "股票信息接口")
@RestController
@RequestMapping("/stock")
public class StockController {

    @Autowired
    private StockInfoService stockInfoService;

    @ApiOperation("获取所有启用的股票列表")
    @GetMapping("/list")
    public Result<List<StockInfo>> getStockList() {
        try {
            List<StockInfo> stocks = stockInfoService.getActiveStocks();
            return Result.success("查询成功", stocks);
        } catch (Exception e) {
            log.error("查询股票列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("根据股票代码查询股票信息")
    @GetMapping("/{stockCode}")
    public Result<StockInfo> getStockInfo(
            @ApiParam("股票代码") @PathVariable String stockCode) {
        try {
            StockInfo stock = stockInfoService.getByStockCode(stockCode);
            if (stock != null) {
                return Result.success("查询成功", stock);
            } else {
                return Result.notFound("未找到股票信息");
            }
        } catch (Exception e) {
            log.error("查询股票信息失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据股票名称搜索股票信息
     */
    @ApiOperation("根据股票名称搜索股票信息")
    @GetMapping("/search")
    public Result<List<StockInfo>> searchStockByName(
            @ApiParam("股票名称") @RequestParam String stockName) {
        try {
            List<StockInfo> stocks = stockInfoService.searchStockByName(stockName);
            return Result.success("搜索成功", stocks);
        } catch (Exception e) {
            log.error("搜索股票失败: {}", stockName, e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

}
