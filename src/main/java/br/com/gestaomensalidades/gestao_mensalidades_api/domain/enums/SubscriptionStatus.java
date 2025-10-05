package br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums;

/**
 * Enum que representa os possíveis status de uma assinatura.
 * Sincronizado com os status do Stripe Subscriptions.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
public enum SubscriptionStatus {
    
    /**
     * Assinatura ativa e em dia
     */
    ACTIVE("active", "Ativa"),
    
    /**
     * Pagamento em atraso - tentativas de cobrança falharam
     */
    PAST_DUE("past_due", "Pagamento em Atraso"),
    
    /**
     * Assinatura cancelada pelo usuário ou administrador
     */
    CANCELED("canceled", "Cancelada"),
    
    /**
     * Assinatura incompleta - pagamento inicial não concluído
     */
    INCOMPLETE("incomplete", "Incompleta"),
    
    /**
     * Assinatura incompleta expirada - pagamento não foi realizado a tempo
     */
    INCOMPLETE_EXPIRED("incomplete_expired", "Expirada"),
    
    /**
     * Assinatura não paga - todas tentativas de cobrança falharam
     */
    UNPAID("unpaid", "Não Paga"),
    
    /**
     * Assinatura em período de teste gratuito
     */
    TRIALING("trialing", "Em Teste");
    
    private final String stripeValue;
    private final String displayName;
    
    SubscriptionStatus(String stripeValue, String displayName) {
        this.stripeValue = stripeValue;
        this.displayName = displayName;
    }
    
    /**
     * Retorna o valor usado na API do Stripe
     * @return String com o valor do status no formato do Stripe
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
     * Verifica se a assinatura está ativa (pode acessar o serviço)
     * @return true se o status permitir acesso ao serviço
     */
    public boolean isActive() {
        return this == ACTIVE || this == TRIALING;
    }
    
    /**
     * Verifica se a assinatura requer ação do usuário
     * @return true se o status exigir intervenção do usuário
     */
    public boolean requiresAction() {
        return this == PAST_DUE || this == INCOMPLETE || this == UNPAID;
    }
    
    /**
     * Converte o valor do Stripe para o enum correspondente
     * @param stripeValue valor retornado pela API do Stripe
     * @return SubscriptionStatus correspondente
     * @throws IllegalArgumentException se o valor não for reconhecido
     */
    public static SubscriptionStatus fromStripeValue(String stripeValue) {
        for (SubscriptionStatus status : values()) {
            if (status.stripeValue.equalsIgnoreCase(stripeValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status de assinatura inválido: " + stripeValue);
    }
}
