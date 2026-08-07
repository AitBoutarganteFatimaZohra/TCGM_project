package com.tcgm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("TCGM - API de Gestion des Chantiers")
                .version("1.0.0")
                .description("API REST pour la gestion des chantiers de la filiale TCGM")
                .contact(new Contact()
                    .name("TCGM Development Team")
                    .email("dev@tcgm.com")
                    .url("https://www.tcgm.com"))
                .license(new License()
                    .name("Propriétaire")
                    .url("https://www.tcgm.com/license")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", createSecurityScheme()));
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("Entrez votre token JWT dans le format: Bearer {token}");
    }
}