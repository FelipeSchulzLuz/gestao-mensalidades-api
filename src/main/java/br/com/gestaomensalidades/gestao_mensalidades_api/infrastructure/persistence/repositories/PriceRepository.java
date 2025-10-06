package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Price;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Product;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.PriceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de persistência da entidade Price.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06 (Migration 003)
 */
@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    
    /**
     * Busca todos os preços de um produto.
     * @param product Produto
     * @return Lista de preços
     */
    List<Price> findByProduct(Product product);
    
    /**
     * Busca preços ativos de um produto.
     * @param product Produto
     * @param isActive Status de ativação
     * @return Lista de preços
     */
    List<Price> findByProductAndIsActive(Product product, Boolean isActive);
    
    /**
     * Busca preços por tipo em um produto.
     * @param product Produto
     * @param type Tipo do preço (RECURRING ou ONE_TIME)
     * @return Lista de preços
     */
    List<Price> findByProductAndType(Product product, PriceType type);
    
    /**
     * Busca preços ativos por tipo em um produto.
     * @param product Produto
     * @param type Tipo do preço
     * @param isActive Status de ativação
     * @return Lista de preços
     */
    List<Price> findByProductAndTypeAndIsActive(
        Product product, 
        PriceType type, 
        Boolean isActive
    );
    
    /**
     * Busca um preço por ID do Stripe.
     * @param stripePriceId ID do preço no Stripe
     * @return Optional contendo o preço, se encontrado
     */
    Optional<Price> findByStripePriceId(String stripePriceId);
    
    /**
     * Verifica se existe um preço com o ID do Stripe.
     * @param stripePriceId ID do preço no Stripe
     * @return true se existir
     */
    boolean existsByStripePriceId(String stripePriceId);
    
    /**
     * Conta quantos preços ativos existem para um produto.
     * @param product Produto
     * @param isActive Status de ativação
     * @return Quantidade de preços
     */
    long countByProductAndIsActive(Product product, Boolean isActive);
    
    /**
     * Busca preços recorrentes por intervalo.
     * @param product Produto
     * @param recurringInterval Intervalo (month, year, etc)
     * @param isActive Status de ativação
     * @return Lista de preços
     */
    List<Price> findByProductAndRecurringIntervalAndIsActive(
        Product product, 
        String recurringInterval, 
        Boolean isActive
    );
    
    /**
     * Busca preços com produto relacionado (JOIN FETCH para evitar N+1).
     * @param productId ID do produto
     * @param isActive Status de ativação
     * @return Lista de preços com produto carregado
     */
    @Query("SELECT p FROM Price p " +
           "LEFT JOIN FETCH p.product " +
           "WHERE p.product.id = :productId " +
           "AND p.isActive = :isActive " +
           "ORDER BY p.amount ASC")
    List<Price> findActivePricesWithProduct(
        @Param("productId") Long productId,
        @Param("isActive") Boolean isActive
    );
}
