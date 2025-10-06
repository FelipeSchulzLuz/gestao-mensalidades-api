package br.com.gestaomensalidades.gestao_mensalidades_api.application.mappers;

import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.order.OrderRequestDto;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.order.OrderResponseDto;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Order;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre entidade Order e seus DTOs.
 * 
 * <p>Responsabilidades:
 * <ul>
 *   <li>Converter OrderRequestDto → Order (para criar pedidos)</li>
 *   <li>Converter Order → OrderResponseDto (para retornar na API)</li>
 *   <li>Converter lista de Orders para lista de OrderResponseDto</li>
 * </ul>
 * 
 * <p><strong>Importante:</strong> Os campos 'user' e 'organization' NÃO são mapeados
 * neste mapper, pois são obtidos automaticamente via UserService no OrderService:
 * <ul>
 *   <li>userId → UserService.getCurrentUserId()</li>
 *   <li>organizationId → UserService.getCurrentOrganizationId()</li>
 * </ul>
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06
 */
public class OrderMapper {
    
    private OrderMapper() {
        // Utility class - construtor privado
    }
    
    /**
     * Converte OrderRequestDto para entidade Order.
     * 
     * <p><strong>Nota:</strong> Os campos 'user' e 'organization' devem ser setados
     * manualmente no Service usando:
     * <pre>
     * User user = userRepository.findById(userService.getCurrentUserId())...
     * Organization org = organizationRepository.findById(userService.getCurrentOrganizationId())...
     * order.setUser(user);
     * order.setOrganization(org);
     * </pre>
     * 
     * @param dto DTO com dados do pedido
     * @return Entidade Order (sem user/organization setados)
     */
    public static Order toEntity(OrderRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        BigDecimal total = dto.calculateTotal();
        
        return Order.builder()
                .items(dto.getItems())
                .subtotal(dto.getSubtotal())
                .discount(dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO)
                .total(total)
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "BRL")
                .status(OrderStatus.PENDING) // Novos pedidos sempre começam PENDING
                .paymentMethod(dto.getPaymentMethod())
                .stripePaymentIntentId(dto.getStripePaymentIntentId())
                .stripeCheckoutSessionId(dto.getStripeCheckoutSessionId())
                .metadata(dto.getMetadata())
                .build();
    }
    
    /**
     * Converte entidade Order para OrderResponseDto.
     * 
     * <p>Inclui campos calculados:
     * <ul>
     *   <li>formattedTotal - valor formatado em BRL</li>
     *   <li>cancelable - se o pedido pode ser cancelado</li>
     * </ul>
     * 
     * @param order Entidade Order
     * @return DTO para resposta da API
     */
    public static OrderResponseDto toDto(Order order) {
        if (order == null) {
            return null;
        }
        
        return OrderResponseDto.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .organizationId(order.getOrganization() != null ? order.getOrganization().getId() : null)
                .items(order.getItems())
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .currency(order.getCurrency())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .stripePaymentIntentId(order.getStripePaymentIntentId())
                .stripeCheckoutSessionId(order.getStripeCheckoutSessionId())
                .paidAt(order.getPaidAt())
                .refundedAt(order.getRefundedAt())
                .canceledAt(order.getCanceledAt())
                .metadata(order.getMetadata())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .formattedTotal(order.getFormattedTotal())
                .cancelable(order.isCancelable())
                .build();
    }
    
    /**
     * Converte uma lista de Orders para lista de OrderResponseDto.
     * 
     * @param orders Lista de entidades Order
     * @return Lista de DTOs para resposta da API
     */
    public static List<OrderResponseDto> toDtoList(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        
        return orders.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Atualiza uma entidade Order existente com dados do DTO.
     * 
     * <p><strong>Nota:</strong> Normalmente Orders não são editados após criação,
     * apenas seu status é atualizado via métodos específicos (markAsPaid, markAsCanceled, etc).
     * 
     * <p>Este método é útil para casos especiais onde metadata ou outros campos
     * precisam ser atualizados.
     * 
     * @param order Entidade existente a ser atualizada
     * @param dto DTO com novos valores
     */
    public static void updateEntity(Order order, OrderRequestDto dto) {
        if (order == null || dto == null) {
            return;
        }
        
        // Atualiza apenas campos seguros (não altera valores financeiros de pedidos já processados)
        if (order.isPending()) {
            order.setItems(dto.getItems());
            order.setSubtotal(dto.getSubtotal());
            order.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO);
            order.calculateTotal();
            order.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "BRL");
        }
        
        // Metadata pode sempre ser atualizado
        if (dto.getMetadata() != null) {
            order.setMetadata(dto.getMetadata());
        }
        
        // Stripe IDs só são atualizados se fornecidos
        if (dto.getStripePaymentIntentId() != null) {
            order.setStripePaymentIntentId(dto.getStripePaymentIntentId());
        }
        if (dto.getStripeCheckoutSessionId() != null) {
            order.setStripeCheckoutSessionId(dto.getStripeCheckoutSessionId());
        }
        if (dto.getPaymentMethod() != null) {
            order.setPaymentMethod(dto.getPaymentMethod());
        }
    }
}
