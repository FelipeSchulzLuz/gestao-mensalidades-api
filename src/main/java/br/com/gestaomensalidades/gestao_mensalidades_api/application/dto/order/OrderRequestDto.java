package br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.order;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO para requisições de criação de pedidos.
 * 
 * <p>Validações aplicadas:
 * <ul>
 *   <li>items: obrigatório, estrutura JSON com produtos/preços</li>
 *   <li>subtotal: obrigatório, >= 0.00</li>
 *   <li>discount: opcional, >= 0.00</li>
 *   <li>total: calculado automaticamente (subtotal - discount)</li>
 *   <li>currency: obrigatório, 3 caracteres (ISO 4217)</li>
 *   <li>paymentMethod: opcional, máximo 50 caracteres</li>
 * </ul>
 * 
 * <p><strong>Nota Multi-Tenancy:</strong>
 * userId e organizationId são automaticamente obtidos do contexto
 * de segurança via UserService.getCurrentUserId() e getCurrentOrganizationId().
 * 
 * <p><strong>Estrutura do campo 'items':</strong>
 * <pre>
 * {
 *   "items": [
 *     {
 *       "product_id": 1,
 *       "price_id": 2,
 *       "quantity": 1,
 *       "amount": 99.00
 *     }
 *   ]
 * }
 * </pre>
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto {
    
    /**
     * Itens do pedido em formato JSON.
     * 
     * <p>Cada item deve conter:
     * <ul>
     *   <li>product_id: ID do produto</li>
     *   <li>price_id: ID do preço selecionado</li>
     *   <li>quantity: Quantidade (padrão: 1)</li>
     *   <li>amount: Valor unitário</li>
     * </ul>
     * 
     * <p>Exemplo:
     * <pre>
     * {
     *   "items": [
     *     {"product_id": 1, "price_id": 2, "quantity": 1, "amount": 99.00},
     *     {"product_id": 3, "price_id": 4, "quantity": 2, "amount": 49.90}
     *   ]
     * }
     * </pre>
     */
    @NotNull(message = "Itens do pedido são obrigatórios")
    private Map<String, Object> items;
    
    /**
     * Subtotal do pedido (soma de todos os itens).
     * 
     * <p>Calculado automaticamente no frontend:
     * <code>subtotal = items.reduce((sum, item) => sum + (item.amount * item.quantity), 0)</code>
     */
    @NotNull(message = "Subtotal é obrigatório")
    @DecimalMin(value = "0.00", inclusive = true, message = "Subtotal deve ser maior ou igual a zero")
    @Digits(integer = 10, fraction = 2, message = "Subtotal deve ter no máximo 10 dígitos inteiros e 2 decimais")
    private BigDecimal subtotal;
    
    /**
     * Desconto aplicado ao pedido.
     * 
     * <p>Pode vir de:
     * <ul>
     *   <li>Cupom de desconto (Stripe Coupon)</li>
     *   <li>Promoção manual</li>
     *   <li>Desconto por volume</li>
     * </ul>
     */
    @DecimalMin(value = "0.00", inclusive = true, message = "Desconto deve ser maior ou igual a zero")
    @Digits(integer = 10, fraction = 2, message = "Desconto deve ter no máximo 10 dígitos inteiros e 2 decimais")
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;
    
    /**
     * Código da moeda (ISO 4217).
     * 
     * <p>Padrão: BRL (Real Brasileiro)
     */
    @NotBlank(message = "Moeda é obrigatória")
    @Size(min = 3, max = 3, message = "Moeda deve ter exatamente 3 caracteres (código ISO 4217)")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Moeda deve estar em maiúsculas (ex: BRL, USD, EUR)")
    @Builder.Default
    private String currency = "BRL";
    
    /**
     * Método de pagamento escolhido pelo cliente.
     * 
     * <p>Exemplos comuns:
     * <ul>
     *   <li>card - Cartão de crédito</li>
     *   <li>boleto - Boleto bancário</li>
     *   <li>pix - PIX</li>
     * </ul>
     */
    @Size(max = 50, message = "Método de pagamento deve ter no máximo 50 caracteres")
    private String paymentMethod;
    
    /**
     * ID do PaymentIntent no Stripe (opcional).
     * Formato: pi_xxxxxxxxxxxxx
     * 
     * <p>Preenchido automaticamente pelo sistema durante o checkout.
     */
    @Pattern(
        regexp = "^(pi_[A-Za-z0-9]{24,})?$",
        message = "Stripe Payment Intent ID deve seguir o formato: pi_xxxxxxxxxxxxx"
    )
    private String stripePaymentIntentId;
    
    /**
     * ID da Checkout Session no Stripe (opcional).
     * Formato: cs_test_xxxxxxxxxxxxx ou cs_live_xxxxxxxxxxxxx
     * 
     * <p>Preenchido automaticamente ao criar Checkout Session.
     */
    @Pattern(
        regexp = "^(cs_(test|live)_[A-Za-z0-9]{50,})?$",
        message = "Stripe Checkout Session ID deve seguir o formato: cs_test_xxxxxxxxxxxxx"
    )
    private String stripeCheckoutSessionId;
    
    /**
     * Metadados adicionais do pedido (uso interno).
     * 
     * <p>Exemplo:
     * <pre>
     * {
     *   "campaign": "black-friday-2025",
     *   "referrer": "google-ads",
     *   "notes": "Cliente solicitou nota fiscal"
     * }
     * </pre>
     */
    private Map<String, Object> metadata;
    
    /**
     * Calcula o total do pedido (subtotal - discount).
     * 
     * @return Total calculado
     */
    public BigDecimal calculateTotal() {
        if (subtotal == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountValue = (discount != null) ? discount : BigDecimal.ZERO;
        return subtotal.subtract(discountValue);
    }
}
