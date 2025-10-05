package br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.BillingInterval;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que representa um plano de assinatura.
 * Cada plano é sincronizado com um Price no Stripe.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
@Entity
@Table(name = "plans", indexes = {
    @Index(name = "idx_plan_stripe_price_id", columnList = "stripe_price_id", unique = true),
    @Index(name = "idx_plan_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Plan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome do plano é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(length = 500)
    private String description;
    
    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotBlank(message = "Moeda é obrigatória")
    @Size(min = 3, max = 3, message = "Código da moeda deve ter 3 caracteres")
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "BRL";
    
    @NotNull(message = "Intervalo de cobrança é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingInterval interval;
    
    /**
     * ID do Price correspondente no Stripe
     * Formato: price_xxxxxxxxxxxxx
     */
    @NotBlank(message = "Stripe Price ID é obrigatório")
    @Column(name = "stripe_price_id", nullable = false, unique = true, length = 50)
    private String stripePriceId;
    
    /**
     * Indica se o plano está disponível para novas assinaturas
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Indica se o plano é destaque/recomendado
     */
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;
    
    /**
     * Ordem de exibição na listagem de planos
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Retorna o preço formatado com a moeda
     * @return String formatada (ex: "R$ 99,90")
     */
    public String getFormattedPrice() {
        if (price == null) return "";
        
        String symbol = switch (currency.toUpperCase()) {
            case "BRL" -> "R$ ";
            case "USD" -> "$ ";
            case "EUR" -> "€ ";
            default -> currency + " ";
        };
        
        return symbol + String.format("%.2f", price);
    }
    
    /**
     * Retorna descrição do intervalo de cobrança
     * @return String com o intervalo (ex: "Mensal")
     */
    public String getIntervalDisplay() {
        return interval != null ? interval.getDisplayName() : "";
    }
    
    /**
     * Verifica se o plano está disponível para novas assinaturas
     * @return true se ativo
     */
    public boolean isAvailable() {
        return Boolean.TRUE.equals(isActive);
    }
}
