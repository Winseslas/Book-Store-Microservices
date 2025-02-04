package com.winseslas.microservices.bookStore.NotificationManager.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuration class for Swagger and OpenAPI documentation in the NotificationManager application.
 * This class integrates Swagger UI and OpenAPI 3.0 for generating API documentation.
 * It includes configurations for API metadata, server information, and bearer authentication support.
 */
@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

    /**
     * Configures the OpenAPI documentation with server details and API metadata.
     *
     * @return an {@link OpenAPI} bean containing API information and server configuration.
     */
    @Bean
    public OpenAPI openAPI() {
        return configureOpenAPI();
    }

    /**
     * Configures OpenAPI metadata and server information.
     * It sets the API title, description, version, and adds a development server URL.
     *
     * @return an {@link OpenAPI} object containing API metadata and server configuration.
     */
    private OpenAPI configureOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:9091");
        server.setDescription("Development server for the Notification Manager Service API.");

        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Notification Manager Service API")
                .description("API for managing notifications and related data in the BookStore system.")
                .version("1.0.0")
                .license(mitLicense);

        SecurityScheme bearerAuth = bearerAuthScheme();

        return new OpenAPI()
                .info(info)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .schemaRequirement("bearerAuth", bearerAuth)
                .servers(List.of(server));
    }

    /**
     * Configures the security scheme for Bearer Token (JWT) authentication.
     * This configuration enables Swagger UI to include a security scheme for bearer authentication.
     *
     * @return a {@link SecurityScheme} object for JWT bearer authentication.
     */
    private SecurityScheme bearerAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
    }
}
