# Application Layer

Esta camada contém a **lógica de aplicação** e **casos de uso** do sistema.

## Estrutura

### `/services`
Contém os serviços de aplicação que orquestram as operações:
- `AuthService.java` - Lógica de autenticação (registro, login)
- `UserService.java` - Lógica de gerenciamento de usuários
- `PlanService.java` - Lógica de gerenciamento de planos
- `SubscriptionService.java` - Lógica de gerenciamento de assinaturas
- `WebhookService.java` - Processamento de eventos de webhook

## Princípios

- Depende apenas da camada **Domain**
- Coordena operações entre repositórios e serviços de infraestrutura
- Contém regras de negócio da aplicação (não do domínio)
- Lança exceções de negócio customizadas
- **NÃO** deve conter detalhes de implementação técnica
