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
    private final UserService userService;

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
        // ✅ MULTI-TENANCY: Forçar organizationId e userId do usuário autenticado
        Long currentOrgId = userService.getCurrentOrganizationId();
        Long currentUserId = userService.getCurrentUserId();
        
        log.info("Creating order for user: {} in organization: {}", currentUserId, currentOrgId);
        
        // Validar organização (usa a do usuário autenticado)
        Organization organization = organizationService.findById(currentOrgId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Organization not found with ID: " + currentOrgId
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
     * Lista pedidos do usuário autenticado, filtrados por status.
     * ✅ MULTI-TENANCY: Usa getCurrentUserId() e getCurrentOrganizationId()
     * 
     * @param status status do pedido (null para todos)
     * @return lista de pedidos do usuário autenticado
     */
    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        // ✅ MULTI-TENANCY: Obter userId e organizationId do contexto autenticado
        Long currentUserId = userService.getCurrentUserId();
        Long currentOrgId = userService.getCurrentOrganizationId();
        
        log.debug("Finding orders for user: {} in org: {} with status: {}", 
            currentUserId, currentOrgId, status);
        
        return orderRepository.findAll()
            .stream()
            .filter(o -> o.getUser().getId().equals(currentUserId))
            .filter(o -> o.getOrganization().getId().equals(currentOrgId))
            .filter(o -> status == null || o.getStatus() == status)
            .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
            .toList();
    }

    /**
     * Lista todos os pedidos do usuário autenticado.
     * ✅ MULTI-TENANCY: Usa getCurrentUserId() e getCurrentOrganizationId()
     * 
     * @return lista de pedidos do usuário autenticado ordenados por data (mais recente primeiro)
     */
    @Transactional(readOnly = true)
    public List<Order> findAllMyOrders() {
        // ✅ MULTI-TENANCY: Obter userId e organizationId do contexto autenticado
        Long currentUserId = userService.getCurrentUserId();
        Long currentOrgId = userService.getCurrentOrganizationId();
        
        log.debug("Finding all orders for user: {} in org: {}", currentUserId, currentOrgId);
        
        return orderRepository.findAll()
            .stream()
            .filter(o -> o.getUser().getId().equals(currentUserId))
            .filter(o -> o.getOrganization().getId().equals(currentOrgId))
            .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
            .toList();
    }

    /**
     * Lista todos os pedidos da organização do usuário autenticado.
     * ✅ MULTI-TENANCY: Usa getCurrentOrganizationId()
     * 
     * @return lista de pedidos da organização ordenados por data (mais recente primeiro)
     */
    @Transactional(readOnly = true)
    public List<Order> findAllOrganizationOrders() {
        // ✅ MULTI-TENANCY: Obter organizationId do contexto autenticado
        Long currentOrgId = userService.getCurrentOrganizationId();
        
        log.debug("Finding all orders for organization: {}", currentOrgId);
        
        return orderRepository.findAll()
            .stream()
            .filter(o -> o.getOrganization().getId().equals(currentOrgId))
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
     * Calcula a receita total da organização do usuário autenticado em um período.
     * Considera apenas pedidos com status PAID.
     * ✅ MULTI-TENANCY: Usa getCurrentOrganizationId()
     * 
     * @param startDate data inicial
     * @param endDate data final
     * @return receita total no período
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        // ✅ MULTI-TENANCY: Obter organizationId do contexto autenticado
        Long currentOrgId = userService.getCurrentOrganizationId();
        
        log.debug("Calculating revenue for organization {} between {} and {}", 
            currentOrgId, startDate, endDate);
        
        Organization organization = organizationService.findById(currentOrgId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + currentOrgId));
        
        return orderRepository.calculateTotalRevenue(organization, startDate, endDate);
    }

    /**
     * Calcula a receita total da organização do usuário autenticado (todos os tempos).
     * ✅ MULTI-TENANCY: Usa getCurrentOrganizationId()
     * 
     * @return receita total
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue() {
        // ✅ MULTI-TENANCY: Obter organizationId do contexto autenticado
        Long currentOrgId = userService.getCurrentOrganizationId();
        
        log.debug("Calculating total revenue for organization {}", currentOrgId);
        
        Organization organization = organizationService.findById(currentOrgId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + currentOrgId));
        
        // Usar LocalDateTime.MIN e MAX para pegar todos os pedidos
        return orderRepository.calculateTotalRevenue(
            organization,
            LocalDateTime.of(2000, 1, 1, 0, 0),
            LocalDateTime.now()
        );
    }

    /**
     * Conta pedidos por status na organização do usuário autenticado.
     * ✅ MULTI-TENANCY: Usa getCurrentOrganizationId()
     * 
     * @param status status a contar
     * @return número de pedidos
     */
    @Transactional(readOnly = true)
    public long countByStatus(OrderStatus status) {
        // ✅ MULTI-TENANCY: Obter organizationId do contexto autenticado
        Long currentOrgId = userService.getCurrentOrganizationId();
        
        log.debug("Counting orders for organization {} with status {}", currentOrgId, status);
        
        return findAllOrganizationOrders()
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
