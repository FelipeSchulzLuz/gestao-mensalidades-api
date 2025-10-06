package br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums;

/**
 * Tipo de preço.
 * Define se o preço é recorrente (assinaturas) ou único (e-commerce).
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06 (Migration 003)
 */
public enum PriceType {
    /**
     * Preço recorrente (assinaturas).
     * Cobrança periódica (mensal, anual, etc).
     * Deve ter recurring_interval definido.
     */
    RECURRING("Recorrente"),
    
    /**
     * Preço único (e-commerce).
     * Cobrança única, sem repetição.
     * recurring_interval deve ser null.
     */
    ONE_TIME("Único");

    private final String displayName;

    PriceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Verifica se o preço requer intervalo de recorrência.
     * @return true se RECURRING
     */
    public boolean requiresRecurringInterval() {
        return this == RECURRING;
    }
}
