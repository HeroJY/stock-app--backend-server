package com.stock.premium.controller;

import com.stock.premium.entity.StockInfo;
import com.stock.premium.service.StockInfoService;
import com.stock.premium.utils.Result;
import com.stock.premium.vo.StockDetailVO;
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

    @ApiOperation(value = "根据股票代码获取股票详细信息", 
                  notes = "获取股票的中文名称、最新A股价格、H股价格以及溢价率等详细信息")
    @GetMapping("/{stockCode}/detail")
    public Result<StockDetailVO> getStockDetail(
            @ApiParam(value = "股票代码", example = "600036", required = true) 
            @PathVariable String stockCode) {
        try {
            StockDetailVO stockDetail = stockInfoService.getStockDetail(stockCode);
            if (stockDetail != null) {
                return Result.success("查询成功", stockDetail);
            } else {
                return Result.notFound("未找到股票详细信息");
            }
        } catch (Exception e) {
            log.error("查询股票详细信息失败: stockCode={}", stockCode, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
