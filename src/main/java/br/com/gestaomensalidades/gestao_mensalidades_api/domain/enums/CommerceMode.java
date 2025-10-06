package br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums;

/**
 * Modo de comércio da organização.
 * Define se a organização opera com assinaturas, e-commerce ou ambos.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06 (Migration 003)
 */
public enum CommerceMode {
    /**
     * Apenas assinaturas recorrentes (SaaS, mensalidades).
     * Produtos do tipo SUBSCRIPTION apenas.
     */
    SUBSCRIPTIONS_ONLY("Apenas Assinaturas"),
    
    /**
     * Apenas e-commerce (vendas únicas).
     * Produtos do tipo ONE_TIME apenas.
     */
    ECOMMERCE_ONLY("Apenas E-commerce"),
    
    /**
     * Modo híbrido: suporta assinaturas E e-commerce.
     * Produtos de ambos os tipos permitidos.
     */
    HYBRID("Híbrido");

    private final String displayName;

    CommerceMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Verifica se o modo permite assinaturas recorrentes.
     * @return true se SUBSCRIPTIONS_ONLY ou HYBRID
     */
    public boolean supportsSubscriptions() {
        return this == SUBSCRIPTIONS_ONLY || this == HYBRID;
    }

    /**
     * Verifica se o modo permite vendas únicas (e-commerce).
     * @return true se ECOMMERCE_ONLY ou HYBRID
     */
    public boolean supportsEcommerce() {
        return this == ECOMMERCE_ONLY || this == HYBRID;
    }
}
