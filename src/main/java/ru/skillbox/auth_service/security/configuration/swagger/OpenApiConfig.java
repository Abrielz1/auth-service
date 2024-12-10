package ru.skillbox.auth_service.security.configuration.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Info apiInfo = new Info()
                .title("MS-ACCOUNT API")
                .version("1.0.0")
                .description("API for managing accounts.")
                .contact(new Contact()
                        .name("TeamFlow_51")
                        .email("hello@skillbox.ru"));

        return new OpenAPI()
                .info(apiInfo);
    }
}