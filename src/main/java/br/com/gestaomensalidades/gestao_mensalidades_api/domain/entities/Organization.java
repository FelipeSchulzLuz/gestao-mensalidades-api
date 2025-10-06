package br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.CommerceMode;
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
 * Entidade que representa uma organização no sistema (multi-tenant).
 * Cada organização pode vender assinaturas, produtos avulsos ou ambos.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06 (Migration 003)
 */
@Entity
@Table(name = "organizations",
    indexes = {
        @Index(name = "idx_organization_slug", columnList = "slug", unique = true),
        @Index(name = "idx_organization_active", columnList = "is_active"),
        @Index(name = "idx_organization_stripe_account", columnList = "stripe_account_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "metadata")
public class Organization {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome da organização é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    @Column(nullable = false, length = 255)
    private String name;
    
    @NotBlank(message = "Slug é obrigatório")
    @Size(max = 100, message = "Slug deve ter no máximo 100 caracteres")
    @Column(nullable = false, unique = true, length = 100)
    private String slug;
    
    /**
     * Modo de comércio da organização.
     * Define se opera com assinaturas, e-commerce ou ambos.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "commerce_mode", nullable = false, length = 30)
    @Builder.Default
    private CommerceMode commerceMode = CommerceMode.SUBSCRIPTIONS_ONLY;
    
    /**
     * ID da conta Stripe Connect (se usar múltiplas contas).
     * Formato: acct_xxxxxxxxxxxxx
     */
    @Column(name = "stripe_account_id", unique = true, length = 255)
    private String stripeAccountId;
    
    /**
     * Indica se a organização está ativa no sistema.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Metadados adicionais em formato JSON.
     * Exemplos: configurações personalizadas, integrações, etc.
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
     * Define se a organização está ativa.
     * @param active true para ativar, false para desativar
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    /**
     * Verifica se a organização tem conta Stripe configurada.
     * @return true se tiver stripeAccountId
     */
    public boolean hasStripeAccount() {
        return this.stripeAccountId != null && !this.stripeAccountId.isBlank();
    }
    
    /**
     * Verifica se a organização suporta assinaturas.
     * @return true se commerceMode permite assinaturas
     */
    public boolean supportsSubscriptions() {
        return this.commerceMode != null && this.commerceMode.supportsSubscriptions();
    }
    
    /**
     * Verifica se a organização suporta e-commerce.
     * @return true se commerceMode permite e-commerce
     */
    public boolean supportsEcommerce() {
        return this.commerceMode != null && this.commerceMode.supportsEcommerce();
    }
}
