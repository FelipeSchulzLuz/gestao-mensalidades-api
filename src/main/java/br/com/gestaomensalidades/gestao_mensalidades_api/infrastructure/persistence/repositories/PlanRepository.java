package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Plan;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.BillingInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de persistência da entidade Plan.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    
    /**
     * Busca planos ativos (disponíveis para novas assinaturas)
     * @return lista de planos ativos ordenados por ordem de exibição
     */
    @Query("SELECT p FROM Plan p WHERE p.isActive = true ORDER BY p.displayOrder ASC, p.price ASC")
    List<Plan> findByIsActiveTrue();
    
    /**
     * Busca um plano pelo Stripe Price ID
     * @param stripePriceId ID do price no Stripe
     * @return Optional contendo o plano se encontrado
     */
    Optional<Plan> findByStripePriceId(String stripePriceId);
    
    /**
     * Verifica se existe um plano com o Stripe Price ID informado
     * @param stripePriceId ID do price no Stripe
     * @return true se existir plano com este Price ID
     */
    boolean existsByStripePriceId(String stripePriceId);
    
    /**
     * Busca planos por intervalo de cobrança
     * @param interval intervalo de cobrança (MONTH, YEAR)
     * @return lista de planos com o intervalo especificado
     */
    @Query("SELECT p FROM Plan p WHERE p.interval = :interval AND p.isActive = true ORDER BY p.price ASC")
    List<Plan> findByInterval(@Param("interval") BillingInterval interval);
    
    /**
     * Busca planos em destaque
     * @return lista de planos marcados como featured
     */
    @Query("SELECT p FROM Plan p WHERE p.isFeatured = true AND p.isActive = true ORDER BY p.displayOrder ASC")
    List<Plan> findFeaturedPlans();
    
    /**
     * Busca planos por moeda
     * @param currency código da moeda (BRL, USD, EUR)
     * @return lista de planos na moeda especificada
     */
    @Query("SELECT p FROM Plan p WHERE p.currency = :currency AND p.isActive = true ORDER BY p.price ASC")
    List<Plan> findByCurrency(@Param("currency") String currency);
    
    /**
     * Busca todos os planos (ativos e inativos) ordenados
     * @return lista de todos os planos
     */
    @Query("SELECT p FROM Plan p ORDER BY p.isActive DESC, p.displayOrder ASC, p.price ASC")
    List<Plan> findAllOrdered();
    
    /**
     * Conta quantos planos estão ativos
     * @return número de planos ativos
     */
    @Query("SELECT COUNT(p) FROM Plan p WHERE p.isActive = true")
    long countActivePlans();
    
    /**
     * Busca planos mensais ativos
     * @return lista de planos mensais
     */
    @Query("SELECT p FROM Plan p WHERE p.interval = 'MONTH' AND p.isActive = true ORDER BY p.price ASC")
    List<Plan> findMonthlyPlans();
    
    /**
     * Busca planos anuais ativos
     * @return lista de planos anuais
     */
    @Query("SELECT p FROM Plan p WHERE p.interval = 'YEAR' AND p.isActive = true ORDER BY p.price ASC")
    List<Plan> findYearlyPlans();
}
