package br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.order;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para respostas de consultas de pedidos.
 * 
 * <p>Contém todos os dados do pedido incluindo:
 * <ul>
 *   <li>Identificação (id, userId, organizationId)</li>
 *   <li>Itens e valores (items, subtotal, discount, total, currency)</li>
 *   <li>Status (status, paidAt, refundedAt, canceledAt)</li>
 *   <li>Pagamento (paymentMethod, stripePaymentIntentId, stripeCheckoutSessionId)</li>
 *   <li>Metadados (metadata)</li>
 *   <li>Timestamps (createdAt, updatedAt)</li>
 * </ul>
 * 
 * <p>Este DTO é usado para retornar pedidos em APIs REST,
 * evitando expor a entidade JPA diretamente.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    
    /**
     * ID único do pedido.
     */
    private Long id;
    
    /**
     * ID do usuário que fez o pedido.
     */
    private Long userId;
    
    /**
     * ID da organização proprietária do pedido.
     */
    private Long organizationId;
    
    /**
     * Itens do pedido em formato JSON.
     * 
     * <p>Estrutura:
     * <pre>
     * [
     *   {"product_id": 1, "price_id": 2, "quantity": 1, "amount": 99.00}
     * ]
     * </pre>
     */
    private Map<String, Object> items;
    
    /**
     * Subtotal do pedido (soma dos itens).
     */
    private BigDecimal subtotal;
    
    /**
     * Desconto aplicado.
     */
    private BigDecimal discount;
    
    /**
     * Total do pedido (subtotal - discount).
     */
    private BigDecimal total;
    
    /**
     * Código da moeda (ISO 4217): BRL, USD, EUR, etc.
     */
    private String currency;
    
    /**
     * Status do pedido.
     * 
     * <p>Valores possíveis:
     * <ul>
     *   <li>PENDING - Aguardando pagamento</li>
     *   <li>PAID - Pago</li>
     *   <li>FAILED - Falhou</li>
     *   <li>CANCELED - Cancelado</li>
     *   <li>REFUNDED - Reembolsado</li>
     * </ul>
     */
    private OrderStatus status;
    
    /**
     * Método de pagamento usado.
     */
    private String paymentMethod;
    
    /**
     * ID do PaymentIntent no Stripe.
     */
    private String stripePaymentIntentId;
    
    /**
     * ID da Checkout Session no Stripe.
     */
    private String stripeCheckoutSessionId;
    
    /**
     * Data/hora em que o pagamento foi confirmado.
     */
    private LocalDateTime paidAt;
    
    /**
     * Data/hora em que o pedido foi reembolsado.
     */
    private LocalDateTime refundedAt;
    
    /**
     * Data/hora em que o pedido foi cancelado.
     */
    private LocalDateTime canceledAt;
    
    /**
     * Metadados adicionais do pedido.
     */
    private Map<String, Object> metadata;
    
    /**
     * Data e hora de criação do pedido.
     */
    private LocalDateTime createdAt;
    
    /**
     * Data e hora da última atualização.
     */
    private LocalDateTime updatedAt;
    
    /**
     * Total formatado em moeda brasileira.
     * 
     * <p>Exemplo: "R$ 199,80"
     * 
     * <p>Campo calculado, não persistido no banco.
     */
    private String formattedTotal;
    
    /**
     * Indica se o pedido pode ser cancelado.
     * 
     * <p>Campo calculado, não persistido no banco.
     */
    private Boolean cancelable;
}
