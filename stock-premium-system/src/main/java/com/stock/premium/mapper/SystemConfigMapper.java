package com.stock.premium.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.premium.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统配置Mapper接口
 * 
 * @author system
 * @since 2024-01-01
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键查询配置值
     */
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 更新配置值
     */
    int updateByConfigKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
}