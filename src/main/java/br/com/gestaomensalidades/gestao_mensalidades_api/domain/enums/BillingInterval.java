package br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums;

/**
 * Enum que representa os intervalos de cobrança disponíveis para planos de assinatura.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
public enum BillingInterval {
    
    /**
     * Cobrança mensal - recorrência a cada 30 dias
     */
    MONTH("month", "Mensal"),
    
    /**
     * Cobrança anual - recorrência a cada 365 dias
     */
    YEAR("year", "Anual");
    
    private final String stripeValue;
    private final String displayName;
    
    BillingInterval(String stripeValue, String displayName) {
        this.stripeValue = stripeValue;
        this.displayName = displayName;
    }
    
    /**
     * Retorna o valor usado na API do Stripe
     * @return String com o valor do intervalo no formato do Stripe
     */
    public String getStripeValue() {
        return stripeValue;
    }
    
    /**
     * Retorna o nome amigável para exibição
     * @return String com o nome para exibir ao usuário
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Converte o valor do Stripe para o enum correspondente
     * @param stripeValue valor retornado pela API do Stripe
     * @return BillingInterval correspondente
     * @throws IllegalArgumentException se o valor não for reconhecido
     */
    public static BillingInterval fromStripeValue(String stripeValue) {
        for (BillingInterval interval : values()) {
            if (interval.stripeValue.equalsIgnoreCase(stripeValue)) {
                return interval;
            }
        }
        throw new IllegalArgumentException("Intervalo de cobrança inválido: " + stripeValue);
    }
}
