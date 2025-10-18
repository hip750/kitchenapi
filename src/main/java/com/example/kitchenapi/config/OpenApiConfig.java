package com.example.kitchenapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Kitchen APIのOpenAPI設定
 * JWT Bearer認証を使用したSwagger/OpenAPIドキュメントを構成します。
 */
@Configuration
public class OpenApiConfig {

    /**
     * API情報とセキュリティ設定を使用してOpenAPI Beanを構成します。
     * @return OpenAPI設定
     */
    @Bean
    public OpenAPI kitchenApi() {
        // Bearer JWT用のセキュリティスキームを定義
        SecurityScheme securityScheme = new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // セキュリティ要件を定義
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("Kitchen API")
                        .version("1.0")
                        .description("Recipe and Pantry Management REST API"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
