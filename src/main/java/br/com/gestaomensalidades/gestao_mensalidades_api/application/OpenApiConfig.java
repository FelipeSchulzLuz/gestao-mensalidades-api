package br.com.gestaomensalidades.gestao_mensalidades_api.application;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuração do OpenAPI/Swagger para documentação da API.
 * 
 * <p>Ativa apenas no profile 'dev' para evitar exposição em produção.
 * 
 * <p>Suporta autenticação híbrida:
 * <ul>
 *   <li>JWT Bearer Token (autenticação própria)</li>
 *   <li>Firebase ID Token (social login)</li>
 * </ul>
 * 
 * <p>Acesso à documentação:
 * <ul>
 *   <li>Swagger UI: <a href="http://localhost:8080/api/swagger-ui.html">http://localhost:8080/api/swagger-ui.html</a></li>
 *   <li>OpenAPI JSON: <a href="http://localhost:8080/api/v3/api-docs">http://localhost:8080/api/v3/api-docs</a></li>
 * </ul>
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 2.0
 */
@Configuration
@Profile("dev")
@OpenAPIDefinition(
        info = @Info(
                title = "Gestão de Mensalidades API",
                version = "2.0.0",
                description = "API REST para gestão de mensalidades com integração ao Stripe e autenticação híbrida (JWT + Firebase)",
                contact = @Contact(
                        name = "Equipe de Desenvolvimento",
                        url = "https://github.com/FelipeSchulzLuz/gestao-mensalidades-api",
                        email = "contato@gestaomensalidades.com.br"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080/api",
                        description = "Servidor de Desenvolvimento"
                )
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Autenticação via JWT Bearer Token ou Firebase ID Token. " +
                      "Adicione o token no formato: Bearer {token}"
)
public class OpenApiConfig {
    // Configuração aplicada via anotações
}
