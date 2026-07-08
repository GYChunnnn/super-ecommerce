package com.javastudy.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 3 文档配置
 * 访问: http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🛒 电商项目 API 文档")
                        .version("1.0.0")
                        .description("Spring Boot 电商学习项目，涵盖用户、商品、购物车、订单、支付、秒杀等模块")
                        .contact(new Contact()
                                .name("GYChunnnn")
                                .url("https://github.com/GYChunnnn/super-ecommerce")));
    }
}
