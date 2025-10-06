package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Order;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Organization;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.User;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de persistência da entidade Order.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06 (Migration 003)
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Busca todos os pedidos de um usuário.
     * @param user Usuário
     * @return Lista de pedidos ordenados por data de criação (mais recente primeiro)
     */
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Busca pedidos por status de um usuário.
     * @param user Usuário
     * @param status Status do pedido
     * @return Lista de pedidos
     */
    List<Order> findByUserAndStatus(User user, OrderStatus status);
    
    /**
     * Busca todos os pedidos de uma organização.
     * @param organization Organização
     * @return Lista de pedidos ordenados por data de criação
     */
    List<Order> findByOrganizationOrderByCreatedAtDesc(Organization organization);
    
    /**
     * Busca pedidos por status em uma organização.
     * @param organization Organização
     * @param status Status do pedido
     * @return Lista de pedidos
     */
    List<Order> findByOrganizationAndStatus(Organization organization, OrderStatus status);
    
    /**
     * Busca um pedido por ID do PaymentIntent do Stripe.
     * @param stripePaymentIntentId ID do PaymentIntent no Stripe
     * @return Optional contendo o pedido, se encontrado
     */
    Optional<Order> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    /**
     * Busca um pedido por ID da Checkout Session do Stripe.
     * @param stripeCheckoutSessionId ID da Checkout Session no Stripe
     * @return Optional contendo o pedido, se encontrado
     */
    Optional<Order> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);
    
    /**
     * Verifica se existe um pedido com o ID do PaymentIntent.
     * @param stripePaymentIntentId ID do PaymentIntent no Stripe
     * @return true se existir
     */
    boolean existsByStripePaymentIntentId(String stripePaymentIntentId);
    
    /**
     * Conta quantos pedidos um usuário tem por status.
     * @param user Usuário
     * @param status Status do pedido
     * @return Quantidade de pedidos
     */
    long countByUserAndStatus(User user, OrderStatus status);
    
    /**
     * Busca pedidos criados em um período.
     * @param organization Organização
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Lista de pedidos
     */
    List<Order> findByOrganizationAndCreatedAtBetween(
        Organization organization,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
    
    /**
     * Busca pedidos pagos de um usuário.
     * @param user Usuário
     * @return Lista de pedidos pagos ordenados por data de pagamento
     */
    @Query("SELECT o FROM Order o " +
           "WHERE o.user = :user " +
           "AND o.status = 'PAID' " +
           "ORDER BY o.paidAt DESC")
    List<Order> findPaidOrdersByUser(@Param("user") User user);
    
    /**
     * Busca pedidos pendentes há mais de X horas.
     * Útil para identificar pedidos abandonados.
     * @param hours Quantidade de horas
     * @return Lista de pedidos pendentes antigos
     */
    @Query("SELECT o FROM Order o " +
           "WHERE o.status = 'PENDING' " +
           "AND o.createdAt < :cutoffDate " +
           "ORDER BY o.createdAt ASC")
    List<Order> findStalePendingOrders(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Calcula o total de vendas (soma dos totais) em um período.
     * @param organization Organização
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Soma dos totais dos pedidos pagos
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
           "WHERE o.organization = :organization " +
           "AND o.status = 'PAID' " +
           "AND o.paidAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateTotalRevenue(
        @Param("organization") Organization organization,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
