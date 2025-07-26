package com.stock.premium.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.premium.entity.StockInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 股票基础信息Mapper接口
 * 
 * @author system
 * @since 2024-01-01
 */
@Mapper
public interface StockInfoMapper extends BaseMapper<StockInfo> {

    /**
     * 查询所有启用的股票信息
     */
    @Select("SELECT * FROM stock_info WHERE status = 1 AND deleted = 0")
    List<StockInfo> selectActiveStocks();

    /**
     * 根据A股代码查询对应的H股信息
     */
    @Select("SELECT * FROM stock_info WHERE a_stock_code = #{aStockCode} AND market_type = 'H' AND status = 1 AND deleted = 0")
    StockInfo selectHStockByACode(String aStockCode);

    /**
     * 查询所有A+H股票对
     */
    @Select("SELECT DISTINCT a_stock_code FROM stock_info WHERE a_stock_code IS NOT NULL AND h_stock_code IS NOT NULL AND status = 1 AND deleted = 0")
    List<String> selectAHStockPairs();
}