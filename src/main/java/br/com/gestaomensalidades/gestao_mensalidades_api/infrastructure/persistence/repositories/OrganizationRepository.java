package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para operações de persistência da entidade Organization.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06 (Migration 003)
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    
    /**
     * Busca uma organização por slug (identificador único amigável).
     * @param slug Slug da organização
     * @return Optional contendo a organização, se encontrada
     */
    Optional<Organization> findBySlug(String slug);
    
    /**
     * Busca uma organização por ID da conta Stripe.
     * @param stripeAccountId ID da conta Stripe
     * @return Optional contendo a organização, se encontrada
     */
    Optional<Organization> findByStripeAccountId(String stripeAccountId);
    
    /**
     * Verifica se existe uma organização com o slug especificado.
     * @param slug Slug da organização
     * @return true se existir
     */
    boolean existsBySlug(String slug);
    
    /**
     * Verifica se existe uma organização com o ID da conta Stripe.
     * @param stripeAccountId ID da conta Stripe
     * @return true se existir
     */
    boolean existsByStripeAccountId(String stripeAccountId);
}
