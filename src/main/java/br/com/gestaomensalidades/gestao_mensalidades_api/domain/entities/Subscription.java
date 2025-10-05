package br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.SubscriptionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma assinatura de um usuário a um plano.
 * Cada assinatura é sincronizada com uma Subscription no Stripe.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_subscription_stripe_id", columnList = "stripe_subscription_id", unique = true),
    @Index(name = "idx_subscription_user_id", columnList = "user_id"),
    @Index(name = "idx_subscription_status", columnList = "status"),
    @Index(name = "idx_subscription_user_status", columnList = "user_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "plan"}) // Evitar lazy loading em logs
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Usuário que possui esta assinatura
     */
    @NotNull(message = "Usuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subscription_user"))
    private User user;
    
    /**
     * Plano ao qual o usuário está inscrito
     */
    @NotNull(message = "Plano é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subscription_plan"))
    private Plan plan;
    
    /**
     * ID da Subscription correspondente no Stripe
     * Formato: sub_xxxxxxxxxxxxx
     */
    @Column(name = "stripe_subscription_id", unique = true, length = 50)
    private String stripeSubscriptionId;
    
    /**
     * Status atual da assinatura
     */
    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.INCOMPLETE;
    
    /**
     * Data de início do período de cobrança atual
     */
    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;
    
    /**
     * Data de término do período de cobrança atual (data da próxima cobrança)
     */
    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;
    
    /**
     * Indica se a assinatura será cancelada ao final do período atual
     */
    @Column(name = "cancel_at_period_end", nullable = false)
    @Builder.Default
    private Boolean cancelAtPeriodEnd = false;
    
    /**
     * Data em que a assinatura foi cancelada
     */
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
    
    /**
     * Data em que o período de teste termina (se aplicável)
     */
    @Column(name = "trial_end")
    private LocalDateTime trialEnd;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Verifica se a assinatura está ativa (usuário pode acessar o serviço)
     * @return true se o status permitir acesso
     */
    public boolean isActive() {
        return status != null && status.isActive();
    }
    
    /**
     * Verifica se a assinatura requer ação do usuário
     * @return true se exigir intervenção
     */
    public boolean requiresAction() {
        return status != null && status.requiresAction();
    }
    
    /**
     * Verifica se a assinatura está em período de teste
     * @return true se estiver em trial
     */
    public boolean isTrialing() {
        return status == SubscriptionStatus.TRIALING;
    }
    
    /**
     * Verifica se a assinatura foi cancelada
     * @return true se cancelada
     */
    public boolean isCanceled() {
        return status == SubscriptionStatus.CANCELED;
    }
    
    /**
     * Verifica se a assinatura está com pagamento em atraso
     * @return true se past_due
     */
    public boolean isPastDue() {
        return status == SubscriptionStatus.PAST_DUE;
    }
    
    /**
     * Calcula quantos dias faltam para a próxima cobrança
     * @return número de dias, ou null se não houver período definido
     */
    public Long getDaysUntilRenewal() {
        if (currentPeriodEnd == null) return null;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), currentPeriodEnd);
    }
    
    /**
     * Verifica se a assinatura tem Stripe Subscription ID configurado
     * @return true se tiver stripeSubscriptionId
     */
    public boolean hasStripeSubscription() {
        return this.stripeSubscriptionId != null && !this.stripeSubscriptionId.isBlank();
    }
}
