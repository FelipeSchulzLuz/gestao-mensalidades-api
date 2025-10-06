package br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.PriceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que representa um preço de um produto.
 * Um produto pode ter múltiplos preços (mensal, anual, promoção, etc).
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06 (Migration 003)
 */
@Entity
@Table(name = "prices",
    indexes = {
        @Index(name = "idx_price_product", columnList = "product_id"),
        @Index(name = "idx_price_type", columnList = "type"),
        @Index(name = "idx_price_active", columnList = "is_active"),
        @Index(name = "idx_price_stripe", columnList = "stripe_price_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "product")
public class Price {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor deve ser maior ou igual a zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @NotNull(message = "Moeda é obrigatória")
    @Size(min = 3, max = 3, message = "Moeda deve ter 3 caracteres (ISO 4217)")
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "BRL";
    
    /**
     * Tipo do preço: RECURRING (recorrente) ou ONE_TIME (único).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriceType type;
    
    /**
     * Intervalo de recorrência (apenas para type=RECURRING).
     * Valores: day, week, month, year
     */
    @Column(name = "recurring_interval", length = 20)
    private String recurringInterval;
    
    /**
     * Quantidade de intervalos (default: 1).
     * Exemplo: recurringInterval=month e recurringIntervalCount=3 = a cada 3 meses
     */
    @Column(name = "recurring_interval_count")
    @Builder.Default
    private Integer recurringIntervalCount = 1;
    
    /**
     * Indica se o preço está ativo (disponível para uso).
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * ID do Price no Stripe.
     * Formato: price_xxxxxxxxxxxxx
     */
    @Column(name = "stripe_price_id", unique = true, length = 255)
    private String stripePriceId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Define se o preço está ativo.
     * @param active true para ativar, false para desativar
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    /**
     * Verifica se o preço tem ID do Stripe configurado.
     * @return true se tiver stripePriceId
     */
    public boolean hasStripePrice() {
        return this.stripePriceId != null && !this.stripePriceId.isBlank();
    }
    
    /**
     * Verifica se o preço é recorrente.
     * @return true se type == RECURRING
     */
    public boolean isRecurring() {
        return this.type == PriceType.RECURRING;
    }
    
    /**
     * Verifica se o preço é único.
     * @return true se type == ONE_TIME
     */
    public boolean isOneTime() {
        return this.type == PriceType.ONE_TIME;
    }
    
    /**
     * Retorna descrição legível do período de recorrência.
     * @return "Mensal", "Anual", etc., ou null se ONE_TIME
     */
    public String getRecurringDescription() {
        if (!isRecurring() || recurringInterval == null) {
            return null;
        }
        
        String interval = switch (recurringInterval.toLowerCase()) {
            case "day" -> "Diário";
            case "week" -> "Semanal";
            case "month" -> "Mensal";
            case "year" -> "Anual";
            default -> recurringInterval;
        };
        
        if (recurringIntervalCount != null && recurringIntervalCount > 1) {
            return String.format("A cada %d %s", recurringIntervalCount, 
                getIntervalPlural(recurringInterval));
        }
        
        return interval;
    }
    
    /**
     * Retorna forma plural do intervalo em português.
     */
    private String getIntervalPlural(String interval) {
        return switch (interval.toLowerCase()) {
            case "day" -> "dias";
            case "week" -> "semanas";
            case "month" -> "meses";
            case "year" -> "anos";
            default -> interval;
        };
    }
    
    /**
     * Formata o valor em moeda brasileira (BRL).
     * @return String formatada "R$ 99,00"
     */
    public String getFormattedAmount() {
        if (amount == null) return "R$ 0,00";
        return String.format("R$ %.2f", amount);
    }
}
