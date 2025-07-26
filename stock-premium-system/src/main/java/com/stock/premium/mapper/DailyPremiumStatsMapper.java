package com.stock.premium.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.premium.entity.DailyPremiumStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 日统计数据Mapper接口
 * 
 * @author system
 * @since 2024-01-01
 */
@Mapper
public interface DailyPremiumStatsMapper extends BaseMapper<DailyPremiumStats> {

    /**
     * 查询指定日期范围的统计数据
     */
    @Select("SELECT * FROM daily_premium_stats WHERE trade_date BETWEEN #{startDate} AND #{endDate} ORDER BY trade_date DESC, stock_code")
    List<DailyPremiumStats> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询指定股票的历史统计数据
     */
    @Select("SELECT * FROM daily_premium_stats WHERE stock_code = #{stockCode} ORDER BY trade_date DESC LIMIT #{limit}")
    List<DailyPremiumStats> selectByStockCode(@Param("stockCode") String stockCode, @Param("limit") Integer limit);
}