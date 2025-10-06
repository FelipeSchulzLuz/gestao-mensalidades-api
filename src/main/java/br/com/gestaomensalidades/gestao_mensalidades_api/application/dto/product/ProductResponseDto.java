package br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.product;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para respostas de consultas de produtos.
 * 
 * <p>Contém todos os dados do produto incluindo:
 * <ul>
 *   <li>Identificação (id, organizationId)</li>
 *   <li>Informações básicas (name, description, type)</li>
 *   <li>Status (isActive, isFeatured)</li>
 *   <li>Ordenação (displayOrder)</li>
 *   <li>Metadados (features, metadata)</li>
 *   <li>Integração Stripe (stripeProductId)</li>
 *   <li>Timestamps (createdAt, updatedAt)</li>
 * </ul>
 * 
 * <p>Este DTO é usado para retornar produtos em APIs REST,
 * evitando expor a entidade JPA diretamente.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    
    /**
     * ID único do produto.
     */
    private Long id;
    
    /**
     * ID da organização proprietária do produto.
     * Usado para validação de multi-tenancy.
     */
    private Long organizationId;
    
    /**
     * Nome do produto.
     */
    private String name;
    
    /**
     * Descrição detalhada do produto.
     */
    private String description;
    
    /**
     * Tipo do produto: SUBSCRIPTION ou ONE_TIME.
     */
    private ProductType type;
    
    /**
     * Indica se o produto está ativo.
     */
    private Boolean isActive;
    
    /**
     * Indica se o produto está em destaque.
     */
    private Boolean isFeatured;
    
    /**
     * Ordem de exibição do produto.
     */
    private Integer displayOrder;
    
    /**
     * Características/benefícios do produto em formato JSON.
     */
    private Map<String, Object> features;
    
    /**
     * Metadados adicionais do produto.
     */
    private Map<String, Object> metadata;
    
    /**
     * ID do produto no Stripe (se integrado).
     */
    private String stripeProductId;
    
    /**
     * Data e hora de criação do produto.
     */
    private LocalDateTime createdAt;
    
    /**
     * Data e hora da última atualização.
     */
    private LocalDateTime updatedAt;
}
