package com.stock.premium.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.premium.entity.PremiumRateRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 溢价率记录Mapper接口
 * 
 * @author system
 * @since 2024-01-01
 */
@Mapper
public interface PremiumRateRecordMapper extends BaseMapper<PremiumRateRecord> {

    /**
     * 查询指定日期和股票的溢价率记录
     */
    @Select("SELECT * FROM premium_rate_record WHERE stock_code = #{stockCode} AND trade_date = #{tradeDate} ORDER BY record_time")
    List<PremiumRateRecord> selectByStockAndDate(@Param("stockCode") String stockCode, @Param("tradeDate") LocalDate tradeDate);

    /**
     * 查询指定日期所有股票的最新溢价率
     */
    @Select("SELECT * FROM premium_rate_record WHERE trade_date = #{tradeDate} AND record_time = (SELECT MAX(record_time) FROM premium_rate_record WHERE stock_code = premium_rate_record.stock_code AND trade_date = #{tradeDate})")
    List<PremiumRateRecord> selectLatestByDate(@Param("tradeDate") LocalDate tradeDate);
}