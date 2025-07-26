package com.stock.premium.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.premium.entity.ExchangeRateRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * 汇率记录Mapper接口
 * 
 * @author system
 * @since 2024-01-01
 */
@Mapper
public interface ExchangeRateRecordMapper extends BaseMapper<ExchangeRateRecord> {

    /**
     * 查询指定日期的最新汇率
     */
    ExchangeRateRecord selectLatestByDate(@Param("tradeDate") LocalDate tradeDate);

    /**
     * 查询最新的汇率记录
     */
    ExchangeRateRecord selectLatest();
}