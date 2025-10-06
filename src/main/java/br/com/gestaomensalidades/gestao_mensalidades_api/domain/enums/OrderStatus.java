package br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums;

/**
 * Status do pedido (order).
 * Define o estado atual de um pedido de compra (e-commerce).
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06 (Migration 003)
 */
public enum OrderStatus {
    /**
     * Pedido criado, aguardando pagamento.
     */
    PENDING("Pendente"),
    
    /**
     * Pagamento confirmado com sucesso.
     */
    PAID("Pago"),
    
    /**
     * Pagamento falhou ou foi recusado.
     */
    FAILED("Falhou"),
    
    /**
     * Pedido reembolsado total ou parcialmente.
     */
    REFUNDED("Reembolsado"),
    
    /**
     * Pedido cancelado antes do pagamento.
     */
    CANCELED("Cancelado");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Verifica se o pedido foi concluído com sucesso.
     * @return true se PAID
     */
    public boolean isPaid() {
        return this == PAID;
    }

    /**
     * Verifica se o pedido está em estado final (não pode mais mudar).
     * @return true se PAID, FAILED, REFUNDED ou CANCELED
     */
    public boolean isFinal() {
        return this != PENDING;
    }

    /**
     * Verifica se o pedido pode ser cancelado.
     * @return true se PENDING
     */
    public boolean isCancelable() {
        return this == PENDING;
    }
}
