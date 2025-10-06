package br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.ProductType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entidade que representa um produto genérico.
 * Pode ser uma assinatura (SUBSCRIPTION) ou venda única (ONE_TIME).
 * Substitui a antiga entidade "Plan".
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06 (Migration 003)
 */
@Entity
@Table(name = "products",
    indexes = {
        @Index(name = "idx_product_organization", columnList = "organization_id"),
        @Index(name = "idx_product_type", columnList = "type"),
        @Index(name = "idx_product_active", columnList = "is_active"),
        @Index(name = "idx_product_featured", columnList = "is_featured"),
        @Index(name = "idx_product_display_order", columnList = "display_order"),
        @Index(name = "idx_product_stripe", columnList = "stripe_product_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"organization", "features", "metadata"})
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Tipo do produto: SUBSCRIPTION (assinatura) ou ONE_TIME (venda única).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType type;
    
    /**
     * Indica se o produto está ativo (disponível para venda).
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Indica se o produto está em destaque na listagem.
     */
    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;
    
    /**
     * Ordem de exibição (produtos com menor valor aparecem primeiro).
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
    
    /**
     * Características/benefícios do produto em formato JSON.
     * Exemplo: ["10 usuários", "100 GB storage", "Suporte 24/7"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> features;
    
    /**
     * Metadados adicionais em formato JSON.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    /**
     * ID do Product no Stripe.
     * Formato: prod_xxxxxxxxxxxxx
     */
    @Column(name = "stripe_product_id", unique = true, length = 255)
    private String stripeProductId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Define se o produto está ativo.
     * @param active true para ativar, false para desativar
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    /**
     * Define se o produto está em destaque.
     * @param featured true para destacar, false para remover destaque
     */
    public void setFeatured(boolean featured) {
        this.isFeatured = featured;
    }
    
    /**
     * Verifica se o produto tem ID do Stripe configurado.
     * @return true se tiver stripeProductId
     */
    public boolean hasStripeProduct() {
        return this.stripeProductId != null && !this.stripeProductId.isBlank();
    }
    
    /**
     * Verifica se o produto é uma assinatura recorrente.
     * @return true se type == SUBSCRIPTION
     */
    public boolean isSubscription() {
        return this.type == ProductType.SUBSCRIPTION;
    }
    
    /**
     * Verifica se o produto é uma venda única.
     * @return true se type == ONE_TIME
     */
    public boolean isOneTime() {
        return this.type == ProductType.ONE_TIME;
    }
    
    /**
     * Retorna o modo de checkout do Stripe para este produto.
     * @return "subscription" ou "payment"
     */
    public String getStripeCheckoutMode() {
        return this.type != null ? this.type.getStripeCheckoutMode() : null;
    }
}
