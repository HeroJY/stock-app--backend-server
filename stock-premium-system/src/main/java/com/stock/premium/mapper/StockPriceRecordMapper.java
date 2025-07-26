package com.stock.premium.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.premium.entity.StockPriceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 股票价格记录Mapper接口
 * 
 * @author system
 * @since 2024-01-01
 */
@Mapper
public interface StockPriceRecordMapper extends BaseMapper<StockPriceRecord> {

    /**
     * 根据股票代码和日期查询价格记录
     */
    List<StockPriceRecord> selectByStockCodeAndDate(@Param("stockCode") String stockCode, 
                                                   @Param("tradeDate") LocalDate tradeDate);

    /**
     * 查询指定日期的最新价格记录
     */
    List<StockPriceRecord> selectLatestByDate(@Param("tradeDate") LocalDate tradeDate);
}