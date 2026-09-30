package br.com.voupassar.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI da TASK 3.2 (primeira API real).
 *
 * <p>Contrato público em {@code /v3/api-docs} + Swagger UI em
 * {@code /swagger-ui.html} (rotas de documentação liberadas no
 * SecurityConfig; sem PII, só schemas). A URL base por ambiente segue em
 * {@code js/config.js} no frontend (docs/architecture.md §2).
 */
@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "VouPassar API",
            version = "0.1.0",
            description =
                "Plataforma de preparação para o IFRN (provas reais → questões → diagnóstico → roteiro). "
                    + "Evidência antes de opinião: o que a fonte não comprova sai como DESCONHECIDO/NÃO CONFIRMADO.",
            contact = @Contact(name = "VouPassar")))
public class OpenApiConfig {}
