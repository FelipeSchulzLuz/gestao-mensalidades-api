package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Organization;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Product;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de persistência da entidade Product.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06 (Migration 003)
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Busca todos os produtos de uma organização.
     * @param organization Organização
     * @return Lista de produtos
     */
    List<Product> findByOrganization(Organization organization);
    
    /**
     * Busca produtos ativos de uma organização.
     * @param organization Organização
     * @param isActive Status de ativação
     * @return Lista de produtos
     */
    List<Product> findByOrganizationAndIsActive(Organization organization, Boolean isActive);
    
    /**
     * Busca produtos por tipo em uma organização.
     * @param organization Organização
     * @param type Tipo do produto (SUBSCRIPTION ou ONE_TIME)
     * @return Lista de produtos
     */
    List<Product> findByOrganizationAndType(Organization organization, ProductType type);
    
    /**
     * Busca produtos ativos por tipo em uma organização.
     * @param organization Organização
     * @param type Tipo do produto
     * @param isActive Status de ativação
     * @return Lista de produtos
     */
    List<Product> findByOrganizationAndTypeAndIsActive(
        Organization organization, 
        ProductType type, 
        Boolean isActive
    );
    
    /**
     * Busca produtos em destaque de uma organização.
     * @param organization Organização
     * @param isFeatured Status de destaque
     * @return Lista de produtos
     */
    List<Product> findByOrganizationAndIsFeatured(Organization organization, Boolean isFeatured);
    
    /**
     * Busca produtos ativos ordenados por displayOrder.
     * @param organization Organização
     * @param isActive Status de ativação
     * @return Lista de produtos ordenados
     */
    List<Product> findByOrganizationAndIsActiveOrderByDisplayOrderAsc(
        Organization organization, 
        Boolean isActive
    );
    
    /**
     * Busca um produto por ID do Stripe.
     * @param stripeProductId ID do produto no Stripe
     * @return Optional contendo o produto, se encontrado
     */
    Optional<Product> findByStripeProductId(String stripeProductId);
    
    /**
     * Verifica se existe um produto com o ID do Stripe.
     * @param stripeProductId ID do produto no Stripe
     * @return true se existir
     */
    boolean existsByStripeProductId(String stripeProductId);
    
    /**
     * Conta quantos produtos ativos existem em uma organização.
     * @param organization Organização
     * @param isActive Status de ativação
     * @return Quantidade de produtos
     */
    long countByOrganizationAndIsActive(Organization organization, Boolean isActive);
    
    /**
     * Busca produtos com preços relacionados (JOIN FETCH para evitar N+1).
     * @param organizationId ID da organização
     * @param isActive Status de ativação
     * @return Lista de produtos com preços carregados
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.organization " +
           "WHERE p.organization.id = :organizationId " +
           "AND p.isActive = :isActive " +
           "ORDER BY p.displayOrder ASC")
    List<Product> findActiveProductsWithOrganization(
        @Param("organizationId") Long organizationId,
        @Param("isActive") Boolean isActive
    );
}
