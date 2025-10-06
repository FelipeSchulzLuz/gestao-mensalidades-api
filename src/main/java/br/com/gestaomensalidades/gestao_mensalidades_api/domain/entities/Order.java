package br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entidade que representa um pedido de compra (e-commerce).
 * Usado para vendas únicas (não-recorrentes).
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06 (Migration 003)
 */
@Entity
@Table(name = "orders",
    indexes = {
        @Index(name = "idx_order_user", columnList = "user_id"),
        @Index(name = "idx_order_organization", columnList = "organization_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_created", columnList = "created_at"),
        @Index(name = "idx_order_stripe_payment_intent", columnList = "stripe_payment_intent_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "organization", "items", "metadata"})
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    /**
     * Itens do pedido em formato JSON.
     * Estrutura: [{"product_id": 1, "price_id": 2, "quantity": 1, "amount": 99.00}]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> items;
    
    @NotNull(message = "Subtotal é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Subtotal deve ser maior ou igual a zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Desconto deve ser maior ou igual a zero")
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;
    
    @NotNull(message = "Total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total deve ser maior ou igual a zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @NotNull(message = "Moeda é obrigatória")
    @Size(min = 3, max = 3, message = "Moeda deve ter 3 caracteres (ISO 4217)")
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "BRL";
    
    /**
     * Status do pedido.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    
    /**
     * Método de pagamento usado.
     * Exemplo: "card", "boleto", "pix", etc.
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    /**
     * ID do PaymentIntent no Stripe.
     * Formato: pi_xxxxxxxxxxxxx
     */
    @Column(name = "stripe_payment_intent_id", unique = true, length = 255)
    private String stripePaymentIntentId;
    
    /**
     * ID da Checkout Session no Stripe.
     * Formato: cs_test_xxxxxxxxxxxxx
     */
    @Column(name = "stripe_checkout_session_id", unique = true, length = 255)
    private String stripeCheckoutSessionId;
    
    /**
     * Data/hora em que o pagamento foi confirmado.
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    /**
     * Data/hora em que o pedido foi reembolsado.
     */
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    /**
     * Data/hora em que o pedido foi cancelado.
     */
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
    
    /**
     * Metadados adicionais em formato JSON.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Marca o pedido como pago.
     */
    public void markAsPaid() {
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }
    
    /**
     * Marca o pedido como falhou.
     */
    public void markAsFailed() {
        this.status = OrderStatus.FAILED;
    }
    
    /**
     * Marca o pedido como reembolsado.
     */
    public void markAsRefunded() {
        this.status = OrderStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }
    
    /**
     * Marca o pedido como cancelado.
     */
    public void markAsCanceled() {
        this.status = OrderStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }
    
    /**
     * Verifica se o pedido foi pago.
     * @return true se status == PAID
     */
    public boolean isPaid() {
        return this.status == OrderStatus.PAID;
    }
    
    /**
     * Verifica se o pedido está pendente.
     * @return true se status == PENDING
     */
    public boolean isPending() {
        return this.status == OrderStatus.PENDING;
    }
    
    /**
     * Verifica se o pedido pode ser cancelado.
     * @return true se status permite cancelamento
     */
    public boolean isCancelable() {
        return this.status != null && this.status.isCancelable();
    }
    
    /**
     * Verifica se o pedido tem PaymentIntent do Stripe.
     * @return true se tiver stripePaymentIntentId
     */
    public boolean hasStripePaymentIntent() {
        return this.stripePaymentIntentId != null && !this.stripePaymentIntentId.isBlank();
    }
    
    /**
     * Calcula o total (subtotal - desconto).
     * Deve ser chamado antes de salvar se subtotal ou desconto mudaram.
     */
    public void calculateTotal() {
        if (this.subtotal != null && this.discount != null) {
            this.total = this.subtotal.subtract(this.discount);
        }
    }
    
    /**
     * Formata o total em moeda brasileira (BRL).
     * @return String formatada "R$ 99,00"
     */
    public String getFormattedTotal() {
        if (total == null) return "R$ 0,00";
        return String.format("R$ %.2f", total);
    }
}
