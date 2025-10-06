package br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.price;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.PriceType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para requisições de criação e atualização de preços.
 * 
 * <p>Validações aplicadas:
 * <ul>
 *   <li>productId: obrigatório (ID do produto associado)</li>
 *   <li>amount: obrigatório, >= 0.00, máximo 2 decimais</li>
 *   <li>currency: obrigatório, 3 caracteres (ISO 4217)</li>
 *   <li>type: obrigatório (RECURRING ou ONE_TIME)</li>
 *   <li>recurringInterval: obrigatório se type=RECURRING</li>
 *   <li>recurringIntervalCount: opcional, mínimo 1</li>
 *   <li>isActive: obrigatório, boolean</li>
 * </ul>
 * 
 * <p><strong>Validação de Negócio:</strong>
 * Se type=RECURRING, recurringInterval deve ser preenchido com um dos valores:
 * day, week, month, year
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceRequestDto {
    
    /**
     * ID do produto ao qual este preço está vinculado.
     * 
     * <p><strong>Importante:</strong> O sistema valida automaticamente
     * se o produto pertence à organização do usuário autenticado.
     */
    @NotNull(message = "ID do produto é obrigatório")
    private Long productId;
    
    /**
     * Valor do preço em centavos (ou menor unidade da moeda).
     * 
     * <p>Exemplos:
     * <ul>
     *   <li>R$ 99,90 = 99.90</li>
     *   <li>R$ 1.000,00 = 1000.00</li>
     *   <li>$10.50 USD = 10.50</li>
     * </ul>
     */
    @NotNull(message = "Valor do preço é obrigatório")
    @DecimalMin(value = "0.00", inclusive = true, message = "Valor deve ser maior ou igual a zero")
    @Digits(integer = 10, fraction = 2, message = "Valor deve ter no máximo 10 dígitos inteiros e 2 decimais")
    private BigDecimal amount;
    
    /**
     * Código da moeda (ISO 4217).
     * 
     * <p>Exemplos comuns:
     * <ul>
     *   <li>BRL - Real Brasileiro</li>
     *   <li>USD - Dólar Americano</li>
     *   <li>EUR - Euro</li>
     * </ul>
     */
    @NotBlank(message = "Moeda é obrigatória")
    @Size(min = 3, max = 3, message = "Moeda deve ter exatamente 3 caracteres (código ISO 4217)")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Moeda deve estar em maiúsculas (ex: BRL, USD, EUR)")
    @Builder.Default
    private String currency = "BRL";
    
    /**
     * Tipo do preço.
     * 
     * <ul>
     *   <li>RECURRING: Cobrança recorrente (assinaturas)</li>
     *   <li>ONE_TIME: Cobrança única (vendas pontuais)</li>
     * </ul>
     */
    @NotNull(message = "Tipo do preço é obrigatório")
    private PriceType type;
    
    /**
     * Intervalo de recorrência.
     * 
     * <p><strong>Obrigatório apenas se type=RECURRING.</strong>
     * 
     * <p>Valores aceitos:
     * <ul>
     *   <li>day - Diário</li>
     *   <li>week - Semanal</li>
     *   <li>month - Mensal</li>
     *   <li>year - Anual</li>
     * </ul>
     */
    @Pattern(
        regexp = "^(day|week|month|year)?$",
        message = "Intervalo de recorrência deve ser: day, week, month ou year"
    )
    private String recurringInterval;
    
    /**
     * Quantidade de intervalos entre cobranças.
     * 
     * <p>Exemplos:
     * <ul>
     *   <li>recurringInterval=month, recurringIntervalCount=1 → Mensal</li>
     *   <li>recurringInterval=month, recurringIntervalCount=3 → Trimestral</li>
     *   <li>recurringInterval=year, recurringIntervalCount=1 → Anual</li>
     * </ul>
     */
    @Min(value = 1, message = "Intervalo de recorrência deve ser no mínimo 1")
    @Builder.Default
    private Integer recurringIntervalCount = 1;
    
    /**
     * Indica se o preço está ativo e disponível para uso.
     * Preços inativos não podem ser usados em novas cobranças.
     */
    @NotNull(message = "Status 'active' é obrigatório")
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * ID do preço no Stripe (opcional).
     * Formato: price_xxxxxxxxxxxxx
     * 
     * <p><strong>Nota:</strong> Geralmente preenchido automaticamente
     * pelo sistema ao sincronizar com Stripe.
     */
    @Pattern(
        regexp = "^(price_[A-Za-z0-9]{14,})?$",
        message = "Stripe Price ID deve seguir o formato: price_xxxxxxxxxxxxx"
    )
    private String stripePriceId;
    
    /**
     * Valida se recurringInterval foi preenchido quando type=RECURRING.
     * 
     * <p>Esta validação é feita no Service layer:
     * <pre>
     * if (type == PriceType.RECURRING && (recurringInterval == null || recurringInterval.isBlank())) {
     *     throw new IllegalArgumentException("recurringInterval é obrigatório para preços recorrentes");
     * }
     * </pre>
     */
}
