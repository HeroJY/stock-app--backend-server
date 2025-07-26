package com.stock.premium.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

/**
 * 数据库配置类
 * 
 * @author system
 * @since 2024-01-01
 */
public class DatabaseConfig {

    /**
     * 分页插件配置
     */
    public static MybatisPlusInterceptor createMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        return interceptor;
    }
}