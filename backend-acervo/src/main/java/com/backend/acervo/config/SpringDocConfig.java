package com.backend.acervo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    OpenAPI acervoOpenAPI() {
    return new OpenAPI()
            .info(new Info()
                .title("Sistema Acervo API")
                .description("API para gerenciamento do acervo digital da FATEC")
                .version("v1.0")
                .contact(new Contact()
                    .name("Equipe de Desenvolvimento")
                    .email("suporte@fatec.sp.gov.br"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")));
}
}