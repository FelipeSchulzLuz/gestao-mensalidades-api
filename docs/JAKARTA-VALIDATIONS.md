# ✅ Jakarta Bean Validation - DTOs

**Data:** 2025-10-06  
**Versão:** 1.0  
**Status:** ✅ **IMPLEMENTADO** - Validações aplicadas em todos os DTOs

---

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [DTOs com Validação](#dtos-com-validação)
3. [Anotações Utilizadas](#anotações-utilizadas)
4. [Exemplos de Validação](#exemplos-de-validação)
5. [Tratamento de Erros](#tratamento-de-erros)
6. [Próximos Passos](#próximos-passos)

---

## 🎯 Visão Geral

Jakarta Bean Validation (anteriormente Java EE Bean Validation) permite validar dados de entrada de forma declarativa usando anotações. Isso garante que:

✅ **Dados inválidos sejam rejeitados** antes de chegar à lógica de negócio  
✅ **Mensagens de erro claras** sejam retornadas ao cliente  
✅ **Validações centralizadas** no próprio DTO (DRY principle)  
✅ **Menos código boilerplate** em controllers e services  

### Dependência (já incluída)

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-validation'
}
```

Esta dependência já está incluída no projeto via `spring-boot-starter-web`.

---

## 📦 DTOs com Validação

### Status Atual

| DTO | Localização | Validações | Status |
|-----|-------------|------------|--------|
| **OrganizationRequestDto** | `application/dto/organization/` | ✅ @NotBlank, @Size, @Pattern, @NotNull | ✅ COMPLETO |
| **LoginRequestDTO** | `application/dto/request/` | ✅ @NotBlank, @Email | ✅ COMPLETO |
| **RegisterRequestDTO** | `application/dto/request/` | ✅ @NotBlank, @Email, @Size | ✅ COMPLETO |
| **AuthResponseDTO** | `application/dto/response/` | ➖ Não requer (apenas resposta) | ➖ N/A |

### Pendentes (Futuros)

| DTO | Status | Prioridade |
|-----|--------|------------|
| ProductRequestDto | ⏳ Não criado ainda | 🟡 MÉDIA |
| ProductResponseDto | ⏳ Não criado ainda | 🟡 MÉDIA |
| PriceRequestDto | ⏳ Não criado ainda | 🟡 MÉDIA |
| PriceResponseDto | ⏳ Não criado ainda | 🟡 MÉDIA |
| OrderRequestDto | ⏳ Não criado ainda | 🟡 MÉDIA |
| OrderResponseDto | ⏳ Não criado ainda | 🟡 MÉDIA |

---

## 🏷️ Anotações Utilizadas

### 1. `@NotNull`

Valida que o campo não é `null`.

```java
@NotNull(message = "Status 'active' é obrigatório")
private Boolean active;
```

**Aceita:** `true`, `false`  
**Rejeita:** `null`

---

### 2. `@NotBlank`

Valida que a string não é `null`, não é vazia (`""`) e não contém apenas espaços em branco.

```java
@NotBlank(message = "Nome da organização é obrigatório")
private String name;
```

**Aceita:** `"Minha Empresa"`, `"ABC"`  
**Rejeita:** `null`, `""`, `"   "` (apenas espaços)

---

### 3. `@Size`

Valida o tamanho de strings, coleções, arrays, e mapas.

```java
@Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
private String name;
```

**Aceita:** `"ABC"` (3 chars), `"Minha Empresa"` (13 chars)  
**Rejeita:** `"AB"` (2 chars), string com 256+ caracteres

---

### 4. `@Pattern`

Valida que a string corresponde a uma expressão regular (regex).

```java
@Pattern(
    regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
    message = "Slug deve estar no formato kebab-case"
)
private String slug;
```

**Aceita:** `"minha-empresa"`, `"empresa-123"`, `"abc"`  
**Rejeita:** `"Minha-Empresa"` (maiúsculas), `"minha_empresa"` (underscore), `"minha--empresa"` (hífens duplos)

---

### 5. `@Email`

Valida que a string é um endereço de email válido.

```java
@Email(message = "Email deve ser válido")
private String email;
```

**Aceita:** `"usuario@example.com"`, `"teste@gmail.com"`  
**Rejeita:** `"invalido"`, `"@example.com"`, `"usuario@"`

---

### 6. `@Min` e `@Max`

Valida valores numéricos mínimos e máximos.

```java
@Min(value = 0, message = "Preço não pode ser negativo")
@Max(value = 999999, message = "Preço máximo excedido")
private BigDecimal price;
```

---

### 7. `@Positive`, `@PositiveOrZero`, `@Negative`, `@NegativeOrZero`

Validações específicas para números.

```java
@Positive(message = "Quantidade deve ser positiva")
private Integer quantity;

@PositiveOrZero(message = "Estoque não pode ser negativo")
private Integer stock;
```

---

### 8. `@Past`, `@PastOrPresent`, `@Future`, `@FutureOrPresent`

Validações para datas e timestamps.

```java
@PastOrPresent(message = "Data de nascimento não pode ser futura")
private LocalDate birthDate;

@FutureOrPresent(message = "Data de vencimento deve ser futura ou hoje")
private LocalDate dueDate;
```

---

### 9. `@Valid` (Validação em Cascata)

Valida objetos aninhados.

```java
public class OrderRequestDto {
    
    @Valid  // ✅ Valida também os campos de AddressDto
    @NotNull(message = "Endereço é obrigatório")
    private AddressDto address;
}
```

---

## 📝 Exemplos de Validação

### Exemplo 1: OrganizationRequestDto (Completo)

```java
package br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de criação/atualização de Organization.
 * 
 * Validações aplicadas:
 * - name: Obrigatório, entre 3 e 255 caracteres
 * - slug: Obrigatório, entre 3 e 100 caracteres, formato kebab-case
 * - active: Obrigatório, booleano
 */
public class OrganizationRequestDto {

    @NotBlank(message = "Nome da organização é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    private String name;

    @NotBlank(message = "Slug da organização é obrigatório")
    @Size(min = 3, max = 100, message = "Slug deve ter entre 3 e 100 caracteres")
    @Pattern(
        regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
        message = "Slug deve estar no formato kebab-case (ex: 'minha-empresa')"
    )
    private String slug;

    @NotNull(message = "Status 'active' é obrigatório")
    private Boolean active;

    // Getters e Setters...
}
```

**Testes de Validação:**

```java
// ✅ VÁLIDO
{
  "name": "Minha Empresa LTDA",
  "slug": "minha-empresa",
  "active": true
}

// ❌ INVÁLIDO - name muito curto
{
  "name": "AB",  // Erro: "Nome deve ter entre 3 e 255 caracteres"
  "slug": "minha-empresa",
  "active": true
}

// ❌ INVÁLIDO - slug com maiúsculas
{
  "name": "Minha Empresa",
  "slug": "Minha-Empresa",  // Erro: "Slug deve estar no formato kebab-case"
  "active": true
}

// ❌ INVÁLIDO - active ausente
{
  "name": "Minha Empresa",
  "slug": "minha-empresa"
  // Erro: "Status 'active' é obrigatório"
}
```

---

### Exemplo 2: RegisterRequestDTO (Completo)

```java
public record RegisterRequestDTO(
        
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    String name,
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    String email,
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
    String password
) {}
```

**Testes de Validação:**

```java
// ✅ VÁLIDO
{
  "name": "João Silva",
  "email": "joao@example.com",
  "password": "senhaSegura123"
}

// ❌ INVÁLIDO - senha muito curta
{
  "name": "João Silva",
  "email": "joao@example.com",
  "password": "1234567"  // Erro: "Senha deve ter entre 8 e 100 caracteres"
}

// ❌ INVÁLIDO - email inválido
{
  "name": "João Silva",
  "email": "joao@",  // Erro: "Email deve ser válido"
  "password": "senhaSegura123"
}
```

---

### Exemplo 3: LoginRequestDTO (Completo)

```java
public record LoginRequestDTO(
        
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    String email,
    
    @NotBlank(message = "Senha é obrigatória")
    String password
) {}
```

---

## 🔌 Uso em Controllers

### Com @Valid

Para ativar as validações, use `@Valid` no controller:

```java
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    
    private final OrganizationService organizationService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponseDto> create(
        @Valid @RequestBody OrganizationRequestDto dto  // ✅ @Valid ativa validações
    ) {
        Organization organization = OrganizationMapper.toEntity(dto);
        Organization created = organizationService.create(organization);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(OrganizationMapper.toDto(created));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponseDto> update(
        @PathVariable Long id,
        @Valid @RequestBody OrganizationRequestDto dto  // ✅ @Valid ativa validações
    ) {
        // ...
    }
}
```

**⚠️ IMPORTANTE:** Sem `@Valid`, as validações **NÃO são executadas**!

---

## 🚨 Tratamento de Erros

### Resposta de Erro Padrão

Quando uma validação falha, Spring retorna **400 Bad Request** com detalhes:

```json
{
  "timestamp": "2025-10-06T14:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for object='organizationRequestDto'",
  "errors": [
    {
      "field": "name",
      "rejectedValue": "AB",
      "message": "Nome deve ter entre 3 e 255 caracteres"
    },
    {
      "field": "slug",
      "rejectedValue": "Minha-Empresa",
      "message": "Slug deve estar no formato kebab-case (ex: 'minha-empresa')"
    }
  ]
}
```

---

### Exception Handler Customizado (Opcional)

Para melhorar a resposta de erro:

```java
@RestControllerAdvice
public class ValidationExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
        MethodArgumentNotValidException ex
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        
        response.put("errors", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
```

**Resposta melhorada:**

```json
{
  "timestamp": "2025-10-06T14:30:00",
  "status": 400,
  "errors": {
    "name": "Nome deve ter entre 3 e 255 caracteres",
    "slug": "Slug deve estar no formato kebab-case (ex: 'minha-empresa')"
  }
}
```

---

## 🧪 Testes Automatizados

### Teste Unitário de Validação

```java
@Test
void shouldRejectInvalidOrganizationRequestDto() {
    // Arrange
    OrganizationRequestDto dto = new OrganizationRequestDto();
    dto.setName("AB");  // Muito curto
    dto.setSlug("Invalid-Slug");  // Maiúsculas
    dto.setActive(null);  // Null
    
    // Act
    Set<ConstraintViolation<OrganizationRequestDto>> violations = 
        validator.validate(dto);
    
    // Assert
    assertEquals(3, violations.size());
    
    List<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .toList();
    
    assertTrue(messages.contains("Nome deve ter entre 3 e 255 caracteres"));
    assertTrue(messages.contains("Slug deve estar no formato kebab-case"));
    assertTrue(messages.contains("Status 'active' é obrigatório"));
}
```

---

### Teste de Integração (Controller)

```java
@Test
void shouldReturn400WhenOrganizationRequestDtoIsInvalid() throws Exception {
    // Arrange
    String invalidJson = """
        {
            "name": "AB",
            "slug": "Invalid-Slug",
            "active": null
        }
        """;
    
    // Act & Assert
    mockMvc.perform(post("/api/organizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.name").value("Nome deve ter entre 3 e 255 caracteres"))
        .andExpect(jsonPath("$.errors.slug").value(containsString("kebab-case")))
        .andExpect(jsonPath("$.errors.active").value("Status 'active' é obrigatório"));
}
```

---

## 📚 Referências e Boas Práticas

### Boas Práticas

1. ✅ **Sempre use `@Valid`** nos parâmetros de controller
2. ✅ **Mensagens descritivas** em português claro
3. ✅ **Combine múltiplas anotações** quando necessário (`@NotBlank` + `@Size` + `@Pattern`)
4. ✅ **Documente as validações** no Javadoc do DTO
5. ✅ **Teste as validações** com testes unitários

### Anti-Padrões a Evitar

1. ❌ **NÃO validar em múltiplos lugares** (DRY violation)
   ```java
   // ❌ ERRADO - validação duplicada
   @PostMapping
   public ResponseEntity<?> create(@Valid @RequestBody Dto dto) {
       if (dto.getName() == null || dto.getName().length() < 3) {  // ❌ Já está no DTO!
           throw new ValidationException("Nome inválido");
       }
   }
   ```

2. ❌ **NÃO esquecer `@Valid`** no controller
   ```java
   // ❌ ERRADO - validações não serão executadas
   @PostMapping
   public ResponseEntity<?> create(@RequestBody Dto dto) {  // Faltou @Valid!
       // ...
   }
   ```

3. ❌ **NÃO usar validações em Response DTOs**
   ```java
   // ❌ DESNECESSÁRIO - Response DTOs não precisam de validação
   public class OrganizationResponseDto {
       @NotBlank  // ❌ Desnecessário - é resposta, não entrada
       private String name;
   }
   ```

---

## 🎯 Próximos Passos

### 1. Criar DTOs Pendentes com Validações

#### ProductRequestDto

```java
public class ProductRequestDto {
    
    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    private String name;
    
    @Size(max = 1000, message = "Descrição não pode exceder 1000 caracteres")
    private String description;
    
    @NotNull(message = "Tipo do produto é obrigatório")
    private ProductType type;  // SUBSCRIPTION ou ONE_TIME
    
    @NotNull(message = "Status 'active' é obrigatório")
    private Boolean active;
    
    @NotNull(message = "Status 'featured' é obrigatório")
    private Boolean featured;
    
    // Getters e Setters...
}
```

---

#### PriceRequestDto

```java
public class PriceRequestDto {
    
    @NotNull(message = "ID do produto é obrigatório")
    @Positive(message = "ID do produto deve ser positivo")
    private Long productId;
    
    @NotNull(message = "Valor unitário é obrigatório")
    @Positive(message = "Valor unitário deve ser positivo")
    @Digits(integer = 10, fraction = 2, message = "Valor deve ter no máximo 10 dígitos inteiros e 2 decimais")
    private BigDecimal unitAmount;
    
    @NotBlank(message = "Moeda é obrigatória")
    @Size(min = 3, max = 3, message = "Moeda deve ter 3 caracteres (ex: BRL, USD)")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Moeda deve estar em formato ISO 4217 (ex: BRL, USD, EUR)")
    private String currency;
    
    @NotNull(message = "Status 'active' é obrigatório")
    private Boolean active;
    
    // Campos para preços recorrentes (SUBSCRIPTION)
    @Pattern(
        regexp = "^(day|week|month|year)$", 
        message = "Intervalo deve ser: day, week, month ou year"
    )
    private String recurringInterval;  // Apenas para SUBSCRIPTION
    
    @Positive(message = "Contagem de intervalo deve ser positiva")
    private Integer recurringIntervalCount;  // Apenas para SUBSCRIPTION
    
    // Getters e Setters...
}
```

---

#### OrderRequestDto

```java
public class OrderRequestDto {
    
    @NotNull(message = "ID do usuário é obrigatório")
    @Positive(message = "ID do usuário deve ser positivo")
    private Long userId;
    
    @NotEmpty(message = "Pedido deve ter pelo menos um item")
    @Valid  // ✅ Valida cada OrderItemDto
    private List<OrderItemDto> items;
    
    @NotNull(message = "Subtotal é obrigatório")
    @PositiveOrZero(message = "Subtotal não pode ser negativo")
    private BigDecimal subtotal;
    
    @NotNull(message = "Total é obrigatório")
    @Positive(message = "Total deve ser positivo")
    private BigDecimal total;
    
    @PositiveOrZero(message = "Desconto não pode ser negativo")
    private BigDecimal discount;
    
    @Size(max = 500, message = "Observações não podem exceder 500 caracteres")
    private String notes;
    
    // Getters e Setters...
}

// DTO aninhado
public class OrderItemDto {
    
    @NotNull(message = "ID do preço é obrigatório")
    @Positive
    private Long priceId;
    
    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser positiva")
    private Integer quantity;
    
    @NotNull(message = "Valor unitário é obrigatório")
    @Positive
    private BigDecimal unitAmount;
    
    // Getters e Setters...
}
```

---

### 2. Implementar Exception Handler Customizado

Criar `GlobalExceptionHandler` para respostas de erro consistentes.

---

### 3. Adicionar Testes de Validação

Criar testes unitários e de integração para todas as validações.

---

## 📊 Resumo do Status

| Componente | Status | Próximo Passo |
|------------|--------|---------------|
| **OrganizationRequestDto** | ✅ **COMPLETO** | - |
| **LoginRequestDTO** | ✅ **COMPLETO** | - |
| **RegisterRequestDTO** | ✅ **COMPLETO** | - |
| **ProductRequestDto** | ⏳ **PENDENTE** | Criar DTO com validações |
| **PriceRequestDto** | ⏳ **PENDENTE** | Criar DTO com validações |
| **OrderRequestDto** | ⏳ **PENDENTE** | Criar DTO com validações |
| **Exception Handler** | ⏳ **PENDENTE** | Implementar GlobalExceptionHandler |
| **Testes de Validação** | ⏳ **PENDENTE** | Criar testes unitários |

---

**Documentado por:** GitHub Copilot AI  
**Última Atualização:** 2025-10-06  
**Versão:** 1.0  
**Status:** ✅ Fase 1 completa - DTOs existentes validados
