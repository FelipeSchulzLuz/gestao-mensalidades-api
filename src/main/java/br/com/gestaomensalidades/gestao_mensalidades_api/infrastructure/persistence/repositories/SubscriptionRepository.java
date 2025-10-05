package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Plan;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Subscription;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.User;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de persistência da entidade Subscription.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    /**
     * Busca assinatura ativa de um usuário
     * @param userId ID do usuário
     * @param status status da assinatura
     * @return Optional contendo a assinatura se encontrada
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = :status")
    Optional<Subscription> findByUserIdAndStatus(@Param("userId") Long userId, 
                                                   @Param("status") SubscriptionStatus status);
    
    /**
     * Busca assinatura pelo Stripe Subscription ID
     * @param stripeSubscriptionId ID da subscription no Stripe
     * @return Optional contendo a assinatura se encontrada
     */
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
    
    /**
     * Busca todas as assinaturas de um usuário
     * @param userId ID do usuário
     * @return lista de assinaturas do usuário
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<Subscription> findByUserId(@Param("userId") Long userId);
    
    /**
     * Busca a assinatura ativa ou em trial de um usuário
     * @param userId ID do usuário
     * @return Optional contendo a assinatura ativa
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId " +
           "AND s.status IN ('ACTIVE', 'TRIALING') ORDER BY s.createdAt DESC")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId);
    
    /**
     * Busca todas as assinaturas por status
     * @param status status da assinatura
     * @return lista de assinaturas com o status especificado
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = :status ORDER BY s.createdAt DESC")
    List<Subscription> findByStatus(@Param("status") SubscriptionStatus status);
    
    /**
     * Busca assinaturas de um plano específico
     * @param planId ID do plano
     * @return lista de assinaturas do plano
     */
    @Query("SELECT s FROM Subscription s WHERE s.plan.id = :planId ORDER BY s.createdAt DESC")
    List<Subscription> findByPlanId(@Param("planId") Long planId);
    
    /**
     * Busca assinaturas que expiram em breve
     * @param date data limite para buscar assinaturas próximas ao vencimento
     * @return lista de assinaturas próximas ao vencimento
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' " +
           "AND s.currentPeriodEnd <= :date ORDER BY s.currentPeriodEnd ASC")
    List<Subscription> findExpiringSoon(@Param("date") LocalDateTime date);
    
    /**
     * Busca assinaturas com pagamento em atraso
     * @return lista de assinaturas past_due
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'PAST_DUE' ORDER BY s.updatedAt DESC")
    List<Subscription> findPastDueSubscriptions();
    
    /**
     * Verifica se um usuário tem assinatura ativa
     * @param userId ID do usuário
     * @return true se o usuário tiver assinatura ativa ou em trial
     */
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.user.id = :userId " +
           "AND s.status IN ('ACTIVE', 'TRIALING')")
    boolean hasActiveSubscription(@Param("userId") Long userId);
    
    /**
     * Conta total de assinaturas ativas
     * @return número de assinaturas ativas
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status IN ('ACTIVE', 'TRIALING')")
    long countActiveSubscriptions();
    
    /**
     * Calcula receita recorrente mensal (MRR)
     * Considera apenas assinaturas ativas e converte anuais para mensais
     * @return valor do MRR
     */
    @Query("SELECT COALESCE(SUM(CASE " +
           "WHEN s.plan.interval = 'MONTH' THEN s.plan.price " +
           "WHEN s.plan.interval = 'YEAR' THEN s.plan.price / 12 " +
           "ELSE 0 END), 0) " +
           "FROM Subscription s WHERE s.status = 'ACTIVE'")
    java.math.BigDecimal calculateMonthlyRecurringRevenue();
    
    /**
     * Busca assinaturas criadas em um período
     * @param startDate data inicial
     * @param endDate data final
     * @return lista de assinaturas criadas no período
     */
    @Query("SELECT s FROM Subscription s WHERE s.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY s.createdAt DESC")
    List<Subscription> findCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Busca assinaturas por usuário e plano
     * @param user usuário
     * @param plan plano
     * @return Optional contendo a assinatura se encontrada
     */
    Optional<Subscription> findByUserAndPlan(User user, Plan plan);
    
    /**
     * Busca assinaturas que serão canceladas no final do período
     * @return lista de assinaturas com cancelamento agendado
     */
    @Query("SELECT s FROM Subscription s WHERE s.cancelAtPeriodEnd = true " +
           "AND s.status = 'ACTIVE' ORDER BY s.currentPeriodEnd ASC")
    List<Subscription> findScheduledForCancellation();
}
