package br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.price;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.PriceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respostas de consultas de preços.
 * 
 * <p>Contém todos os dados do preço incluindo:
 * <ul>
 *   <li>Identificação (id, productId)</li>
 *   <li>Valores (amount, currency)</li>
 *   <li>Tipo e recorrência (type, recurringInterval, recurringIntervalCount)</li>
 *   <li>Status (isActive)</li>
 *   <li>Integração Stripe (stripePriceId)</li>
 *   <li>Timestamp (createdAt)</li>
 * </ul>
 * 
 * <p>Este DTO é usado para retornar preços em APIs REST,
 * evitando expor a entidade JPA diretamente.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceResponseDto {
    
    /**
     * ID único do preço.
     */
    private Long id;
    
    /**
     * ID do produto ao qual este preço pertence.
     */
    private Long productId;
    
    /**
     * Valor do preço.
     */
    private BigDecimal amount;
    
    /**
     * Código da moeda (ISO 4217): BRL, USD, EUR, etc.
     */
    private String currency;
    
    /**
     * Tipo do preço: RECURRING ou ONE_TIME.
     */
    private PriceType type;
    
    /**
     * Intervalo de recorrência (apenas para RECURRING).
     * Valores: day, week, month, year
     */
    private String recurringInterval;
    
    /**
     * Quantidade de intervalos entre cobranças.
     */
    private Integer recurringIntervalCount;
    
    /**
     * Indica se o preço está ativo.
     */
    private Boolean isActive;
    
    /**
     * ID do preço no Stripe (se integrado).
     */
    private String stripePriceId;
    
    /**
     * Data e hora de criação do preço.
     */
    private LocalDateTime createdAt;
    
    /**
     * Descrição legível do período de recorrência.
     * 
     * <p>Exemplos:
     * <ul>
     *   <li>"Mensal" (month, count=1)</li>
     *   <li>"Anual" (year, count=1)</li>
     *   <li>"A cada 3 meses" (month, count=3)</li>
     * </ul>
     * 
     * <p>Campo calculado, não persistido no banco.
     */
    private String recurringDescription;
    
    /**
     * Valor formatado em moeda brasileira.
     * 
     * <p>Exemplo: "R$ 99,90"
     * 
     * <p>Campo calculado, não persistido no banco.
     */
    private String formattedAmount;
}
