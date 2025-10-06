package br.com.gestaomensalidades.gestao_mensalidades_api.application.services;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Order;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Organization;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.OrderStatus;
import br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service para operações de negócio relacionadas a Orders (Pedidos de E-commerce).
 * 
 * Orders são usados apenas para mode=payment (compras únicas ONE_TIME).
 * Para mode=subscription, o Stripe gerencia diretamente as subscriptions.
 * 
 * Fluxo de um Order:
 * 1. PENDING: Pedido criado, aguardando pagamento
 * 2. PAID: Pagamento confirmado via webhook
 * 3. FAILED/REFUNDED/CANCELED: Estados finais negativos
 * 
 * @author Sistema de Gestão de Mensalidades
 * @since 2025-10-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrganizationService organizationService;
    // UserService será necessário quando implementarmos validação de usuário
    // private final UserService userService;

    /**
     * Cria um novo pedido.
     * 
     * Valida:
     * - Organização existe, está ativa e suporta e-commerce
     * - Usuário existe
     * - Items não está vazio
     * - Valores são positivos
     * 
     * @param order dados do pedido
     * @return pedido criado com ID
     * @throws IllegalArgumentException se validações falharem
     */
    @Transactional
    public Order create(Order order) {
        log.info("Creating order for user: {} in organization: {}", 
            order.getUser().getId(), order.getOrganization().getId());
        
        // Validar organização
        Organization organization = organizationService.findById(order.getOrganization().getId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Organization not found with ID: " + order.getOrganization().getId()
            ));
        
        if (!organization.getIsActive()) {
            throw new IllegalStateException("Cannot create orders for inactive organization");
        }
        
        // Validar se organização suporta e-commerce
        organizationService.validateCommerceMode(organization, false, true);
        
        // Validar que tem items
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        
        // Validar valores positivos
        if (order.getSubtotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order subtotal must be greater than 0");
        }
        if (order.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total must be greater than 0");
        }
        
        // Definir valores padrão
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        if (order.getDiscount() == null) {
            order.setDiscount(BigDecimal.ZERO);
        }
        
        order.setOrganization(organization);
        
        Order saved = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", saved.getId());
        
        return saved;
    }

    /**
     * Busca pedido por ID.
     * 
     * @param id ID do pedido
     * @return Optional com o pedido se encontrado
     */
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        log.debug("Finding order by ID: {}", id);
        return orderRepository.findById(id);
    }

    /**
     * Busca pedido por Stripe Payment Intent ID.
     * 
     * @param paymentIntentId ID do payment intent no Stripe
     * @return Optional com o pedido se encontrado
     */
    @Transactional(readOnly = true)
    public Optional<Order> findByStripePaymentIntentId(String paymentIntentId) {
        log.debug("Finding order by Stripe Payment Intent ID: {}", paymentIntentId);
        return orderRepository.findByStripePaymentIntentId(paymentIntentId);
    }

    /**
     * Lista pedidos de um usuário, filtrados por status.
     * 
     * @param userId ID do usuário
     * @param status status do pedido (null para todos)
     * @return lista de pedidos
     */
    @Transactional(readOnly = true)
    public List<Order> findByUserAndStatus(Long userId, OrderStatus status) {
        log.debug("Finding orders for user: {} with status: {}", userId, status);
        
        // TODO: Idealmente deveria validar se o user existe usando UserService.findById()
        
        return orderRepository.findAll()
            .stream()
            .filter(o -> o.getUser().getId().equals(userId))
            .filter(o -> status == null || o.getStatus() == status)
            .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
            .toList();
    }

    /**
     * Lista todos os pedidos de um usuário.
     * 
     * @param userId ID do usuário
     * @return lista de pedidos ordenados por data de criação (mais recente primeiro)
     */
    @Transactional(readOnly = true)
    public List<Order> findByUser(Long userId) {
        log.debug("Finding all orders for user: {}", userId);
        
        return orderRepository.findAll()
            .stream()
            .filter(o -> o.getUser().getId().equals(userId))
            .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
            .toList();
    }

    /**
     * Lista pedidos de uma organização.
     * 
     * @param organizationId ID da organização
     * @return lista de pedidos
     */
    @Transactional(readOnly = true)
    public List<Order> findByOrganization(Long organizationId) {
        log.debug("Finding orders for organization: {}", organizationId);
        
        return orderRepository.findAll()
            .stream()
            .filter(o -> o.getOrganization().getId().equals(organizationId))
            .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
            .toList();
    }

    /**
     * Busca pedidos pendentes antigos (útil para limpeza).
     * 
     * @param olderThan data limite (pedidos criados antes dessa data)
     * @return lista de pedidos pendentes antigos
     */
    @Transactional(readOnly = true)
    public List<Order> findStalePendingOrders(LocalDateTime olderThan) {
        log.debug("Finding stale pending orders older than: {}", olderThan);
        return orderRepository.findStalePendingOrders(olderThan);
    }

    /**
     * Marca um pedido como pago.
     * Geralmente chamado via webhook do Stripe.
     * 
     * @param id ID do pedido
     * @return pedido atualizado
     * @throws IllegalArgumentException se pedido não existir
     * @throws IllegalStateException se pedido já estiver em estado final
     */
    @Transactional
    public Order markAsPaid(Long id) {
        log.info("Marking order {} as PAID", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));
        
        if (order.getStatus().isFinal() && order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException(
                "Cannot change order status from " + order.getStatus() + " to PAID"
            );
        }
        
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        
        Order saved = orderRepository.save(order);
        log.info("Order {} marked as PAID successfully", id);
        
        return saved;
    }

    /**
     * Marca um pedido como falhado.
     * 
     * @param id ID do pedido
     * @return pedido atualizado
     */
    @Transactional
    public Order markAsFailed(Long id) {
        log.info("Marking order {} as FAILED", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));
        
        if (order.getStatus().isFinal()) {
            log.warn("Order {} is already in final state: {}", id, order.getStatus());
        }
        
        order.setStatus(OrderStatus.FAILED);
        
        return orderRepository.save(order);
    }

    /**
     * Cancela um pedido.
     * Só permite cancelar pedidos PENDING ou FAILED.
     * 
     * @param id ID do pedido
     * @return pedido cancelado
     * @throws IllegalStateException se pedido não pode ser cancelado
     */
    @Transactional
    public Order cancel(Long id) {
        log.info("Canceling order {}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));
        
        if (!order.getStatus().isCancelable()) {
            throw new IllegalStateException(
                "Cannot cancel order in status: " + order.getStatus() +
                ". Only PENDING and FAILED orders can be canceled."
            );
        }
        
        order.setStatus(OrderStatus.CANCELED);
        
        return orderRepository.save(order);
    }

    /**
     * Processa um reembolso de pedido.
     * 
     * @param id ID do pedido
     * @return pedido reembolsado
     * @throws IllegalStateException se pedido não foi pago
     */
    @Transactional
    public Order refund(Long id) {
        log.info("Refunding order {}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));
        
        if (!order.getStatus().isPaid()) {
            throw new IllegalStateException(
                "Cannot refund order that is not PAID. Current status: " + order.getStatus()
            );
        }
        
        order.setStatus(OrderStatus.REFUNDED);
        order.setRefundedAt(LocalDateTime.now());
        
        Order saved = orderRepository.save(order);
        log.info("Order {} refunded successfully", id);
        
        return saved;
    }

    /**
     * Atualiza o Stripe Payment Intent ID de um pedido.
     * Geralmente chamado após criar o checkout session no Stripe.
     * 
     * @param id ID do pedido
     * @param paymentIntentId ID do payment intent do Stripe
     * @return pedido atualizado
     */
    @Transactional
    public Order updateStripePaymentIntent(Long id, String paymentIntentId) {
        log.info("Updating Stripe Payment Intent ID for order {}: {}", id, paymentIntentId);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));
        
        order.setStripePaymentIntentId(paymentIntentId);
        
        return orderRepository.save(order);
    }

    /**
     * Calcula a receita total de uma organização em um período.
     * Considera apenas pedidos com status PAID.
     * 
     * @param organizationId ID da organização
     * @param startDate data inicial
     * @param endDate data final
     * @return receita total no período
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenue(Long organizationId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Calculating revenue for organization {} between {} and {}", 
            organizationId, startDate, endDate);
        
        Organization organization = organizationService.findById(organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + organizationId));
        
        return orderRepository.calculateTotalRevenue(organization, startDate, endDate);
    }

    /**
     * Calcula a receita total de uma organização (todos os tempos).
     * 
     * @param organizationId ID da organização
     * @return receita total
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue(Long organizationId) {
        log.debug("Calculating total revenue for organization {}", organizationId);
        
        Organization organization = organizationService.findById(organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + organizationId));
        
        // Usar LocalDateTime.MIN e MAX para pegar todos os pedidos
        return orderRepository.calculateTotalRevenue(
            organization,
            LocalDateTime.of(2000, 1, 1, 0, 0),
            LocalDateTime.now()
        );
    }

    /**
     * Conta pedidos por status em uma organização.
     * 
     * @param organizationId ID da organização
     * @param status status a contar
     * @return número de pedidos
     */
    @Transactional(readOnly = true)
    public long countByOrganizationAndStatus(Long organizationId, OrderStatus status) {
        log.debug("Counting orders for organization {} with status {}", organizationId, status);
        
        return findByOrganization(organizationId)
            .stream()
            .filter(o -> o.getStatus() == status)
            .count();
    }

    /**
     * Limpa pedidos pendentes antigos.
     * Útil para rodar como job agendado.
     * 
     * @param olderThan pedidos criados antes dessa data serão cancelados
     * @return número de pedidos cancelados
     */
    @Transactional
    public int cleanupStalePendingOrders(LocalDateTime olderThan) {
        log.info("Cleaning up stale pending orders older than {}", olderThan);
        
        List<Order> staleOrders = findStalePendingOrders(olderThan);
        
        int count = 0;
        for (Order order : staleOrders) {
            try {
                cancel(order.getId());
                count++;
            } catch (Exception e) {
                log.error("Failed to cancel stale order {}: {}", order.getId(), e.getMessage());
            }
        }
        
        log.info("Cleaned up {} stale pending orders", count);
        return count;
    }
}
