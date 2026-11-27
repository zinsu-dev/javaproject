package com.campuscourier.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Titles the Swagger UI page (http://localhost:8080/swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campusCourierApi() {
        return new OpenAPI().info(new Info()
                .title("Campus Courier API")
                .version("1.0.0")
                .description("Register parcels, assign dispatch, calculate fees and track deliveries."));
    }
}
