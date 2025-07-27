package com.stock.premium.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger配置类
 * 
 * @author system
 * @since 2024-01-01
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.stock.premium.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("股票溢价率监控系统API")
                .description("微信小程序股票溢价率监控后端接口文档\n\n" +
                           "主要功能包括：\n" +
                           "• 股票基本信息管理\n" +
                           "• 腾讯财经API集成查询\n" +
                           "• 股票价格数据收集\n" +
                           "• 溢价率计算与监控\n" +
                           "• 数据统计与分析\n\n" +
                           "新增功能：\n" +
                           "• 通过股票名称调用腾讯财经API查询并持久化股票信息")
                .version("1.1.0")
                .contact(new Contact("系统管理员", "", "admin@example.com"))
                .license("MIT License")
                .licenseUrl("https://opensource.org/licenses/MIT")
                .build();
    }
}
