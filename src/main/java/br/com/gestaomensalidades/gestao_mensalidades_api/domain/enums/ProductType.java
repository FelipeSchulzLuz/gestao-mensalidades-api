package br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums;

/**
 * Tipo de produto.
 * Define se o produto é uma assinatura recorrente ou uma venda única.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06 (Migration 003)
 */
public enum ProductType {
    /**
     * Assinatura recorrente (SaaS, mensalidades).
     * Gera cobranças periódicas (mensal, anual, etc).
     * Cria registros na tabela 'subscriptions'.
     */
    SUBSCRIPTION("Assinatura Recorrente"),
    
    /**
     * Venda única (e-commerce, produtos físicos/digitais).
     * Gera cobrança única.
     * Cria registros na tabela 'orders'.
     */
    ONE_TIME("Venda Única");

    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Retorna o modo de checkout do Stripe correspondente.
     * @return "subscription" ou "payment"
     */
    public String getStripeCheckoutMode() {
        return this == SUBSCRIPTION ? "subscription" : "payment";
    }

    /**
     * Verifica se o produto requer preço recorrente.
     * @return true se SUBSCRIPTION
     */
    public boolean requiresRecurringPrice() {
        return this == SUBSCRIPTION;
    }
}
