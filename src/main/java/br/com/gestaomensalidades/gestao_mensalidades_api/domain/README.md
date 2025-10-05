# Domain Layer

Esta camada contém as **entidades de domínio** e **regras de negócio** principais do sistema.

## Estrutura

### `/entities`
Contém as entidades JPA que representam o modelo de dados do negócio:
- `User.java` - Entidade de usuário
- `Plan.java` - Entidade de plano de assinatura
- `Subscription.java` - Entidade de assinatura

### `/enums`
Contém os enums utilizados pelas entidades:
- `BillingInterval.java` - MONTH, YEAR
- `SubscriptionStatus.java` - ACTIVE, PAST_DUE, CANCELED, INCOMPLETE

## Princípios

- Esta camada **NÃO** deve depender de outras camadas
- Contém apenas lógica de domínio pura
- Usa anotações JPA apenas para mapeamento (não lógica)
- Entidades devem ser imutáveis quando possível (use Lombok @Value ou @Builder)
