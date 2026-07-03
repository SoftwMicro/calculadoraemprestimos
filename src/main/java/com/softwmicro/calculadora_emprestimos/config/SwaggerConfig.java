package com.softwmicro.calculadora_emprestimos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Calculadora de Empréstimos API").version("1.0").description("API para cálculo de empréstimos e geração de grid."));
    }
}

