package br.com.gestaomensalidades.gestao_mensalidades_api.application.services;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Organization;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.CommerceMode;
import br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service para operações de negócio relacionadas a Organizations.
 * 
 * Organizations representam o conceito de multi-tenancy no sistema.
 * Cada organização pode ter:
 * - Modo de comércio (SUBSCRIPTIONS_ONLY, ECOMMERCE_ONLY, HYBRID)
 * - Integração com Stripe (stripeAccountId)
 * - Produtos e usuários isolados
 * 
 * @author Sistema de Gestão de Mensalidades
 * @since 2025-10-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    /**
     * Cria uma nova organização.
     * 
     * @param organization dados da organização
     * @return organização criada com ID
     * @throws IllegalArgumentException se slug já existir
     */
    @Transactional
    public Organization create(Organization organization) {
        log.info("Creating organization with slug: {}", organization.getSlug());
        
        // Validar se slug já existe
        if (organizationRepository.existsBySlug(organization.getSlug())) {
            throw new IllegalArgumentException(
                "Organization with slug '" + organization.getSlug() + "' already exists"
            );
        }
        
        // Definir valores padrão se necessário
        if (organization.getIsActive() == null) {
            organization.setIsActive(true);
        }
        if (organization.getCommerceMode() == null) {
            organization.setCommerceMode(CommerceMode.SUBSCRIPTIONS_ONLY);
        }
        
        Organization saved = organizationRepository.save(organization);
        log.info("Organization created successfully with ID: {}", saved.getId());
        
        return saved;
    }

    /**
     * Busca organização por ID.
     * 
     * @param id ID da organização
     * @return Optional com a organização se encontrada
     */
    @Transactional(readOnly = true)
    public Optional<Organization> findById(Long id) {
        log.debug("Finding organization by ID: {}", id);
        return organizationRepository.findById(id);
    }

    /**
     * Busca organização por slug.
     * 
     * @param slug slug único da organização
     * @return Optional com a organização se encontrada
     */
    @Transactional(readOnly = true)
    public Optional<Organization> findBySlug(String slug) {
        log.debug("Finding organization by slug: {}", slug);
        return organizationRepository.findBySlug(slug);
    }

    /**
     * Busca organização por Stripe Account ID.
     * 
     * @param stripeAccountId ID da conta no Stripe
     * @return Optional com a organização se encontrada
     */
    @Transactional(readOnly = true)
    public Optional<Organization> findByStripeAccountId(String stripeAccountId) {
        log.debug("Finding organization by Stripe Account ID: {}", stripeAccountId);
        return organizationRepository.findByStripeAccountId(stripeAccountId);
    }

    /**
     * Lista todas as organizações ativas.
     * 
     * @return lista de organizações ativas
     */
    @Transactional(readOnly = true)
    public List<Organization> findAllActive() {
        log.debug("Finding all active organizations");
        return organizationRepository.findAll()
            .stream()
            .filter(Organization::getIsActive)
            .toList();
    }

    /**
     * Lista todas as organizações (ativas e inativas).
     * 
     * @return lista de todas as organizações
     */
    @Transactional(readOnly = true)
    public List<Organization> findAll() {
        log.debug("Finding all organizations");
        return organizationRepository.findAll();
    }

    /**
     * Atualiza uma organização existente.
     * 
     * @param id ID da organização
     * @param updatedOrganization dados atualizados
     * @return organização atualizada
     * @throws IllegalArgumentException se organização não existir
     */
    @Transactional
    public Organization update(Long id, Organization updatedOrganization) {
        log.info("Updating organization with ID: {}", id);
        
        Organization existing = organizationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + id));
        
        // Atualizar campos
        if (updatedOrganization.getName() != null) {
            existing.setName(updatedOrganization.getName());
        }
        if (updatedOrganization.getCommerceMode() != null) {
            existing.setCommerceMode(updatedOrganization.getCommerceMode());
        }
        if (updatedOrganization.getStripeAccountId() != null) {
            existing.setStripeAccountId(updatedOrganization.getStripeAccountId());
        }
        if (updatedOrganization.getMetadata() != null) {
            existing.setMetadata(updatedOrganization.getMetadata());
        }
        if (updatedOrganization.getIsActive() != null) {
            existing.setIsActive(updatedOrganization.getIsActive());
        }
        
        Organization saved = organizationRepository.save(existing);
        log.info("Organization updated successfully: {}", saved.getId());
        
        return saved;
    }

    /**
     * Ativa uma organização.
     * 
     * @param id ID da organização
     * @return organização ativada
     */
    @Transactional
    public Organization activate(Long id) {
        log.info("Activating organization with ID: {}", id);
        
        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + id));
        
        organization.setIsActive(true);
        return organizationRepository.save(organization);
    }

    /**
     * Desativa uma organização.
     * 
     * @param id ID da organização
     * @return organização desativada
     */
    @Transactional
    public Organization deactivate(Long id) {
        log.info("Deactivating organization with ID: {}", id);
        
        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + id));
        
        organization.setIsActive(false);
        return organizationRepository.save(organization);
    }

    /**
     * Valida se a organização suporta o modo de comércio especificado.
     * 
     * @param organization organização a validar
     * @param requiresSubscriptions true se requer suporte a assinaturas
     * @param requiresEcommerce true se requer suporte a e-commerce
     * @throws IllegalStateException se o modo não for suportado
     */
    public void validateCommerceMode(Organization organization, boolean requiresSubscriptions, boolean requiresEcommerce) {
        if (requiresSubscriptions && !organization.supportsSubscriptions()) {
            throw new IllegalStateException(
                "Organization '" + organization.getName() + "' does not support subscriptions. " +
                "Current mode: " + organization.getCommerceMode()
            );
        }
        
        if (requiresEcommerce && !organization.supportsEcommerce()) {
            throw new IllegalStateException(
                "Organization '" + organization.getName() + "' does not support e-commerce. " +
                "Current mode: " + organization.getCommerceMode()
            );
        }
    }

    /**
     * Verifica se a organização tem integração Stripe configurada.
     * 
     * @param organization organização a verificar
     * @throws IllegalStateException se não tiver Stripe configurado
     */
    public void requireStripeAccount(Organization organization) {
        if (!organization.hasStripeAccount()) {
            throw new IllegalStateException(
                "Organization '" + organization.getName() + "' does not have a Stripe account configured"
            );
        }
    }

    /**
     * Deleta uma organização (soft delete via desativação).
     * 
     * Use com cuidado! Considera usar deactivate() ao invés de deletar permanentemente.
     * 
     * @param id ID da organização
     */
    @Transactional
    public void delete(Long id) {
        log.warn("Deleting organization with ID: {} (consider using deactivate instead)", id);
        
        if (!organizationRepository.existsById(id)) {
            throw new IllegalArgumentException("Organization not found with ID: " + id);
        }
        
        // Por segurança, fazemos soft delete ao invés de deletar permanentemente
        deactivate(id);
    }
}
