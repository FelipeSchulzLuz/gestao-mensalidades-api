# Infrastructure Layer

Esta camada contém as **implementações técnicas** e integrações externas.

## Estrutura

### `/persistence/repositories`
Repositórios JPA para acesso ao banco de dados:
- `UserRepository.java`
- `PlanRepository.java`
- `SubscriptionRepository.java`

### `/security`
Configurações e componentes de segurança:
- `SecurityConfig.java` - Configuração Spring Security
- `JwtTokenProvider.java` - Geração e validação de JWT
- `JwtAuthenticationFilter.java` - Filtro de autenticação
- `CustomUserDetailsService.java` - Carregamento de usuários

### `/stripe`
Integrações com o Stripe:
- `StripeConfig.java` - Configuração do SDK Stripe
- `StripeCustomerService.java` - Operações de Customer
- `StripeCheckoutService.java` - Criação de Checkout Sessions
- `StripePortalService.java` - Geração de Portal Sessions
- `StripeWebhookService.java` - Verificação e processamento de webhooks

## Princípios

- Depende das camadas **Domain** e **Application**
- Contém todos os detalhes de implementação técnica
- Isola dependências externas (Stripe, Banco, etc)
- Implementa interfaces definidas na camada de aplicação (quando aplicável)
