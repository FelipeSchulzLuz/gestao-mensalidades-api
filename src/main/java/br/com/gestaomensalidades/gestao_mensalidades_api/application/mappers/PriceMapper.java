package br.com.gestaomensalidades.gestao_mensalidades_api.application.mappers;

import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.price.PriceRequestDto;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.price.PriceResponseDto;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Price;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre entidade Price e seus DTOs.
 * 
 * <p>Responsabilidades:
 * <ul>
 *   <li>Converter PriceRequestDto → Price (para criar/atualizar)</li>
 *   <li>Converter Price → PriceResponseDto (para retornar na API)</li>
 *   <li>Converter lista de Prices para lista de PriceResponseDto</li>
 * </ul>
 * 
 * <p><strong>Importante:</strong> O campo 'product' NÃO é mapeado
 * neste mapper, pois é obtido via PriceService validando o productId
 * pertence à organização do usuário autenticado.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06
 */
public class PriceMapper {
    
    private PriceMapper() {
        // Utility class - construtor privado
    }
    
    /**
     * Converte PriceRequestDto para entidade Price.
     * 
     * <p><strong>Nota:</strong> O campo 'product' deve ser setado
     * manualmente no Service usando:
     * <pre>
     * Product product = productRepository.findById(dto.getProductId())...
     * price.setProduct(product);
     * </pre>
     * 
     * @param dto DTO com dados do preço
     * @return Entidade Price (sem product setado)
     */
    public static Price toEntity(PriceRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        return Price.builder()
                .amount(dto.getAmount())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "BRL")
                .type(dto.getType())
                .recurringInterval(dto.getRecurringInterval())
                .recurringIntervalCount(dto.getRecurringIntervalCount() != null ? dto.getRecurringIntervalCount() : 1)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .stripePriceId(dto.getStripePriceId())
                .build();
    }
    
    /**
     * Converte entidade Price para PriceResponseDto.
     * 
     * <p>Inclui campos calculados:
     * <ul>
     *   <li>recurringDescription - descrição legível do período</li>
     *   <li>formattedAmount - valor formatado em BRL</li>
     * </ul>
     * 
     * @param price Entidade Price
     * @return DTO para resposta da API
     */
    public static PriceResponseDto toDto(Price price) {
        if (price == null) {
            return null;
        }
        
        return PriceResponseDto.builder()
                .id(price.getId())
                .productId(price.getProduct() != null ? price.getProduct().getId() : null)
                .amount(price.getAmount())
                .currency(price.getCurrency())
                .type(price.getType())
                .recurringInterval(price.getRecurringInterval())
                .recurringIntervalCount(price.getRecurringIntervalCount())
                .isActive(price.getIsActive())
                .stripePriceId(price.getStripePriceId())
                .createdAt(price.getCreatedAt())
                .recurringDescription(price.getRecurringDescription())
                .formattedAmount(price.getFormattedAmount())
                .build();
    }
    
    /**
     * Converte uma lista de Prices para lista de PriceResponseDto.
     * 
     * @param prices Lista de entidades Price
     * @return Lista de DTOs para resposta da API
     */
    public static List<PriceResponseDto> toDtoList(List<Price> prices) {
        if (prices == null) {
            return null;
        }
        
        return prices.stream()
                .map(PriceMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Atualiza uma entidade Price existente com dados do DTO.
     * 
     * <p>Útil para operações de UPDATE, onde queremos manter
     * o ID e outros campos não editáveis.
     * 
     * @param price Entidade existente a ser atualizada
     * @param dto DTO com novos valores
     */
    public static void updateEntity(Price price, PriceRequestDto dto) {
        if (price == null || dto == null) {
            return;
        }
        
        price.setAmount(dto.getAmount());
        price.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "BRL");
        price.setType(dto.getType());
        price.setRecurringInterval(dto.getRecurringInterval());
        price.setRecurringIntervalCount(dto.getRecurringIntervalCount() != null ? dto.getRecurringIntervalCount() : 1);
        price.setActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        
        // Stripe Price ID só é atualizado se fornecido
        if (dto.getStripePriceId() != null) {
            price.setStripePriceId(dto.getStripePriceId());
        }
    }
}
