package org.example.servermanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServerManager API")
                        .version("1.0.0")
                        .description("API para gestión de chatbots con IA, verificación de pagos Yape/Plin y administración de empresas")
                        .contact(new Contact()
                                .name("ServerManager")
                                .email("soporte@servermanager.com"))
                        .license(new License()
                                .name("Privada")
                                .url("https://servermanager.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de desarrollo")
                ));
    }
}
