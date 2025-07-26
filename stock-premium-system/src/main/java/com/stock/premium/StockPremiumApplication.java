package com.stock.premium;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 股票溢价率监控系统启动类
 * 
 * @author system
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.stock.premium.mapper")
public class StockPremiumApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockPremiumApplication.class, args);
        System.out.println("=================================");
        System.out.println("股票溢价率监控系统启动成功！");
        System.out.println("API文档地址: http://localhost:8080/api/swagger-ui/");
        System.out.println("=================================");
    }
}