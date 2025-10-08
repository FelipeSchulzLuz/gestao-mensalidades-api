# Swagger/OpenAPI - Documentação da API

## 📋 Visão Geral

O projeto agora conta com documentação interativa da API usando **Swagger UI** (via `springdoc-openapi`). A documentação está disponível apenas no **profile `dev`** para evitar exposição em produção.

## 🔧 Implementação

### 1. Dependência (build.gradle)

```gradle
// OpenAPI / Swagger UI (springdoc) - versão compatível com Spring Boot 3.5.x
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.2'
```

**⚠️ Importante**: A versão `2.8.2` é compatível com Spring Boot 3.5.x e Spring Framework 6.2.x. Versões anteriores (como 2.2.0) causam erro `NoSuchMethodError` devido a incompatibilidade de API.

### 2. Configuração de Segurança (SecurityConfig.java)

Os endpoints do Swagger foram liberados para acesso público:

```java
.requestMatchers("/api/swagger-ui.html").permitAll()
.requestMatchers("/api/swagger-ui/**").permitAll()
.requestMatchers("/api/v3/api-docs/**").permitAll()
.requestMatchers("/swagger-ui.html").permitAll()
.requestMatchers("/swagger-ui/**").permitAll()
.requestMatchers("/v3/api-docs/**").permitAll()
```

### 3. Classe de Configuração (OpenApiConfig.java)

```java
@Configuration
@Profile("dev")  // ← Ativa apenas em dev
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
```

**Recursos configurados**:
- ✅ Metadados da API (título, versão, descrição)
- ✅ Informações de contato e licença
- ✅ Servidor de desenvolvimento
- ✅ Esquema de autenticação Bearer (JWT/Firebase)
- ✅ Ativo apenas no profile `dev`

### 4. application.yml

```yaml
# OpenAPI/Swagger Configuration (Dev)
springdoc:
  enabled: true
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
    tagsSorter: alpha
    displayRequestDuration: true
    defaultModelsExpandDepth: 3
    defaultModelExpandDepth: 3
    tryItOutEnabled: true
  show-actuator: false

---
# Production Profile
springdoc:
  enabled: false  # ← Desabilitado em produção
```

**Configurações aplicadas**:
- ✅ Ordenação de operações por método HTTP
- ✅ Ordenação de tags alfabeticamente
- ✅ Exibição de duração das requisições
- ✅ Expansão de modelos habilitada
- ✅ Botão "Try it out" habilitado
- ✅ Endpoints do Actuator ocultados

## 🚀 Como Usar

### Acessar a Documentação

Com a aplicação rodando em **dev profile**:

1. **Swagger UI (Interface Interativa)**:
   ```
   http://localhost:8080/api/swagger-ui.html
   ```
   ou
   ```
   http://localhost:8080/api/swagger-ui/index.html
   ```

2. **OpenAPI JSON (Especificação)**:
   ```
   http://localhost:8080/api/v3/api-docs
   ```

### Testar Endpoints com Autenticação

1. Abra o Swagger UI
2. Clique no botão **"Authorize"** (cadeado verde)
3. Insira o token no formato: `Bearer {seu-token-aqui}`
4. Clique em **"Authorize"**
5. Agora você pode testar os endpoints protegidos

**Exemplo de token**:
```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Rodar Localmente (Dev + H2)

Para testar o Swagger sem Postgres:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev --spring.flyway.enabled=false --spring.datasource.url=jdbc:h2:mem:testdb --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.username=sa --spring.datasource.password='
```

### Rodar com Docker Compose

```bash
# Certifique-se de usar o profile dev
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

O arquivo `docker-compose.dev.yml` já configura `SPRING_PROFILES_ACTIVE=dev`.

## 📊 Endpoints Documentados

A documentação inclui automaticamente:

- ✅ Todos os controllers REST
- ✅ Parâmetros de entrada (path, query, body)
- ✅ Respostas esperadas (status codes e schemas)
- ✅ Modelos de dados (DTOs)
- ✅ Esquemas de autenticação
- ✅ Exemplos de requisição/resposta

**Grupos de endpoints**:
- `auth-controller`: Autenticação JWT (login, register)
- `firebase-auth-controller`: Autenticação Firebase
- `user-example-controller`: Gerenciamento de usuários

## 🔒 Segurança

### Proteção em Produção

O Swagger é **automaticamente desabilitado** no profile `prod`:

```yaml
spring:
  config:
    activate:
      on-profile: prod

springdoc:
  enabled: false  # ← Swagger desabilitado
```

### Endpoints Liberados

Os seguintes endpoints estão **públicos** (não requerem autenticação):

- `/api/swagger-ui.html`
- `/api/swagger-ui/**`
- `/api/v3/api-docs/**`

**⚠️ Atenção**: Esses endpoints são liberados apenas em dev. Em produção, o springdoc está desabilitado, então não há risco de exposição.

## 🐛 Troubleshooting

### Erro: NoSuchMethodError: ControllerAdviceBean.<init>

**Causa**: Versão incompatível do springdoc com Spring Boot/Framework.

**Solução**: Usar `springdoc-openapi-starter-webmvc-ui:2.8.2` (ou superior) com Spring Boot 3.5.x.

### Swagger UI não carrega

1. Verifique se o profile `dev` está ativo:
   ```bash
   curl http://localhost:8080/api/actuator/health
   ```
   
2. Confirme que `springdoc.enabled=true` no application.yml (dev section)

3. Verifique os logs para erros de inicialização

### Endpoints não aparecem na documentação

1. Verifique se o controller está anotado com `@RestController`
2. Confirme que os métodos têm `@RequestMapping`, `@GetMapping`, etc.
3. Verifique se o package do controller está sendo escaneado pelo Spring

### Token de autenticação não funciona

1. Certifique-se de incluir o prefixo `Bearer ` (com espaço)
2. Verifique se o token é válido e não expirou
3. Confirme que o usuário tem as permissões necessárias

## 📚 Recursos Adicionais

- [Documentação springdoc-openapi](https://springdoc.org/)
- [OpenAPI Specification 3.1](https://spec.openapis.org/oas/v3.1.0)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)

## ✅ Checklist de Validação

- [x] Dependência `springdoc-openapi-starter-webmvc-ui:2.8.2` adicionada
- [x] OpenApiConfig criado e anotado com `@Profile("dev")`
- [x] Endpoints do Swagger liberados no SecurityConfig
- [x] `springdoc.enabled=true` em dev e `false` em prod
- [x] Aplicação inicia sem erros
- [x] OpenAPI JSON acessível em `/api/v3/api-docs`
- [x] Swagger UI acessível em `/api/swagger-ui.html`
- [x] Autenticação Bearer configurada corretamente
- [x] Todos os endpoints aparecem na documentação

## 🎯 Conclusão

A implementação do Swagger está **completa e funcionando corretamente**:

- ✅ Versão compatível do springdoc (2.8.2)
- ✅ Configuração de segurança ajustada
- ✅ Metadados da API configurados
- ✅ Autenticação Bearer documentada
- ✅ Desabilitado em produção
- ✅ Endpoints testados e funcionais

**Próximos passos**:
1. Adicionar anotações `@Operation`, `@Parameter` nos controllers para enriquecer a documentação
2. Criar exemplos de requisição/resposta com `@Schema(example = "...")`
3. Documentar códigos de erro esperados com `@ApiResponse`
