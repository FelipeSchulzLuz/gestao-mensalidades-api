# Presentation Layer

Esta camada contém os **pontos de entrada** da aplicação (API REST).

## Estrutura

### `/controllers`
Controllers REST que expõem os endpoints da API:
- `AuthController.java` - Endpoints de autenticação (/auth)
- `UserController.java` - Endpoints de usuários (/users)
- `PlanController.java` - Endpoints de planos (/plans)
- `SubscriptionController.java` - Endpoints de assinaturas (/subscriptions)
- `WebhookController.java` - Endpoint de webhooks (/webhooks/stripe)

### `/dto`
Data Transfer Objects para request e response:
- `request/` - DTOs de entrada (RegisterRequestDTO, LoginRequestDTO, etc)
- `response/` - DTOs de saída (UserResponseDTO, PlanResponseDTO, etc)
- `ErrorResponseDTO.java` - Resposta padrão de erro

### `/mappers`
Conversores entre entidades e DTOs:
- `UserMapper.java`
- `PlanMapper.java`
- `SubscriptionMapper.java`

## Princípios

- Depende das camadas **Application** e **Domain**
- Responsável apenas por:
  - Receber requisições HTTP
  - Validar entrada (Bean Validation)
  - Converter DTOs ↔ Entidades
  - Chamar serviços de aplicação
  - Retornar respostas HTTP
- **NÃO** deve conter lógica de negócio
- Usa anotações Spring Web (@RestController, @RequestMapping, etc)
