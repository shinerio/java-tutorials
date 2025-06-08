package com.shinerio.tutorial.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI myOpenAPI() {

        // 定义测试环境服务器URL
        String devUrl = "http://localhost:5000";
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("本地服务器URL");

        // 定义生产环境服务器URL
        String prodUrl = "http://web.shinerio.site";
        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("正式环境的服务器URL");

        Contact contact = new Contact();
        contact.setEmail("jstxzhangrui@163.com");
        contact.setName("Rui Zhang");
        contact.setUrl("http://web.shinerio.site");

        Info info = new Info()
                .title("Java tutorial 演示 API")
                .version("1.0")
                .contact(contact)
                .description("对外API接口说明.");

        return new OpenAPI().info(info)
                .servers(List.of(devServer, prodServer));
    }
}