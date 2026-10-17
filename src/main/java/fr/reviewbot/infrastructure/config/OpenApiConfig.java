package fr.reviewbot.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reviewBotOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ReviewBot API")
                .description("Revue de code automatique de pull requests")
                .version("0.1.0"));
    }
}
