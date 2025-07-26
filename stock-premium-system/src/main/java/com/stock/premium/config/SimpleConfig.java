package com.stock.premium.config;

/**
 * 简化配置类 - 不依赖Spring注解
 * 
 * @author system
 * @since 2024-01-01
 */
public class SimpleConfig {
    
    /**
     * 应用配置信息
     */
    public static class AppConfig {
        public static final String APP_NAME = "股票溢价率监控系统";
        public static final String APP_VERSION = "1.0.0";
        public static final String API_BASE_PATH = "/api";
    }
    
    /**
     * 数据库配置信息
     */
    public static class DatabaseConfig {
        public static final String H2_URL = "jdbc:h2:mem:testdb";
        public static final String H2_USERNAME = "sa";
        public static final String H2_PASSWORD = "";
    }
}