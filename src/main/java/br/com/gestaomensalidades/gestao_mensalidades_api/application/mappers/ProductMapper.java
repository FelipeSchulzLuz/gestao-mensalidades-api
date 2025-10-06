package br.com.gestaomensalidades.gestao_mensalidades_api.application.mappers;

import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.product.ProductRequestDto;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.product.ProductResponseDto;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Product;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre entidade Product e seus DTOs.
 * 
 * <p>Responsabilidades:
 * <ul>
 *   <li>Converter ProductRequestDto → Product (para criar/atualizar)</li>
 *   <li>Converter Product → ProductResponseDto (para retornar na API)</li>
 *   <li>Converter lista de Products para lista de ProductResponseDto</li>
 * </ul>
 * 
 * <p><strong>Importante:</strong> O campo 'organization' NÃO é mapeado
 * neste mapper, pois é obtido automaticamente via UserService.getCurrentOrganizationId()
 * no ProductService.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06
 */
public class ProductMapper {
    
    private ProductMapper() {
        // Utility class - construtor privado
    }
    
    /**
     * Converte ProductRequestDto para entidade Product.
     * 
     * <p><strong>Nota:</strong> O campo 'organization' deve ser setado
     * manualmente no Service usando:
     * <pre>
     * Organization org = organizationRepository.findById(userService.getCurrentOrganizationId())...
     * product.setOrganization(org);
     * </pre>
     * 
     * @param dto DTO com dados do produto
     * @return Entidade Product (sem organization setada)
     */
    public static Product toEntity(ProductRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .type(dto.getType())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .isFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false)
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .features(dto.getFeatures())
                .metadata(dto.getMetadata())
                .stripeProductId(dto.getStripeProductId())
                .build();
    }
    
    /**
     * Converte entidade Product para ProductResponseDto.
     * 
     * @param product Entidade Product
     * @return DTO para resposta da API
     */
    public static ProductResponseDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        
        return ProductResponseDto.builder()
                .id(product.getId())
                .organizationId(product.getOrganization() != null ? product.getOrganization().getId() : null)
                .name(product.getName())
                .description(product.getDescription())
                .type(product.getType())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .displayOrder(product.getDisplayOrder())
                .features(product.getFeatures())
                .metadata(product.getMetadata())
                .stripeProductId(product.getStripeProductId())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    /**
     * Converte uma lista de Products para lista de ProductResponseDto.
     * 
     * @param products Lista de entidades Product
     * @return Lista de DTOs para resposta da API
     */
    public static List<ProductResponseDto> toDtoList(List<Product> products) {
        if (products == null) {
            return null;
        }
        
        return products.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Atualiza uma entidade Product existente com dados do DTO.
     * 
     * <p>Útil para operações de UPDATE, onde queremos manter
     * o ID e outros campos não editáveis.
     * 
     * @param product Entidade existente a ser atualizada
     * @param dto DTO com novos valores
     */
    public static void updateEntity(Product product, ProductRequestDto dto) {
        if (product == null || dto == null) {
            return;
        }
        
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setType(dto.getType());
        product.setActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        product.setFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
        product.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        product.setFeatures(dto.getFeatures());
        product.setMetadata(dto.getMetadata());
        
        // Stripe Product ID só é atualizado se fornecido
        if (dto.getStripeProductId() != null) {
            product.setStripeProductId(dto.getStripeProductId());
        }
    }
}
