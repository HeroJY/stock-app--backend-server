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

    @ApiOperation("获取所有A+H股票对")
    @GetMapping("/ah-pairs")
    public Result<List<String>> getAHStockPairs() {
        try {
            List<String> pairs = stockInfoService.getAHStockPairs();
            return Result.success("查询成功", pairs);
        } catch (Exception e) {
            log.error("查询A+H股票对失败", e);
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

    @ApiOperation("根据A股代码查询对应的H股信息")
    @GetMapping("/h-stock/{aStockCode}")
    public Result<StockInfo> getHStockByACode(
            @ApiParam("A股代码") @PathVariable String aStockCode) {
        try {
            StockInfo hStock = stockInfoService.getHStockByACode(aStockCode);
            if (hStock != null) {
                return Result.success("查询成功", hStock);
            } else {
                return Result.notFound("未找到对应的H股信息");
            }
        } catch (Exception e) {
            log.error("查询H股信息失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}