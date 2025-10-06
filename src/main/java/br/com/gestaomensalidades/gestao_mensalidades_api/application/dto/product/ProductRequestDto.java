package br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.product;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.ProductType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para requisições de criação e atualização de produtos.
 * 
 * <p>Validações aplicadas:
 * <ul>
 *   <li>name: obrigatório, 3-255 caracteres</li>
 *   <li>description: opcional, máximo 5000 caracteres</li>
 *   <li>type: obrigatório (SUBSCRIPTION ou ONE_TIME)</li>
 *   <li>isActive: obrigatório, boolean</li>
 *   <li>isFeatured: obrigatório, boolean</li>
 *   <li>displayOrder: opcional, mínimo 0</li>
 *   <li>features: opcional, metadados JSON</li>
 *   <li>metadata: opcional, metadados JSON</li>
 * </ul>
 * 
 * <p><strong>Nota:</strong> organizationId é automaticamente obtido do contexto
 * de segurança via UserService.getCurrentOrganizationId().
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 * @since 2025-10-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto {
    
    /**
     * Nome do produto.
     * Deve ser único dentro da organização e descritivo.
     * 
     * Exemplos: "Plano Premium", "Curso de Java Avançado", "Ebook Marketing Digital"
     */
    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    private String name;
    
    /**
     * Descrição detalhada do produto.
     * Pode conter HTML ou Markdown para formatação.
     * 
     * Exemplo: "Plano completo com acesso ilimitado a todos os recursos..."
     */
    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    private String description;
    
    /**
     * Tipo do produto.
     * 
     * <ul>
     *   <li>SUBSCRIPTION: Assinatura recorrente (mensal, anual, etc.)</li>
     *   <li>ONE_TIME: Venda única (cursos, ebooks, etc.)</li>
     * </ul>
     */
    @NotNull(message = "Tipo do produto é obrigatório")
    private ProductType type;
    
    /**
     * Indica se o produto está ativo e disponível para venda.
     * Produtos inativos não aparecem em listagens públicas.
     */
    @NotNull(message = "Status 'active' é obrigatório")
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Indica se o produto está em destaque na página principal.
     * Produtos em destaque são exibidos com prioridade.
     */
    @NotNull(message = "Status 'featured' é obrigatório")
    @Builder.Default
    private Boolean isFeatured = false;
    
    /**
     * Ordem de exibição do produto.
     * Produtos com menor valor aparecem primeiro.
     * 
     * Exemplo: displayOrder=1 (primeiro), displayOrder=2 (segundo), etc.
     */
    @Min(value = 0, message = "Ordem de exibição não pode ser negativa")
    @Builder.Default
    private Integer displayOrder = 0;
    
    /**
     * Características/benefícios do produto em formato JSON.
     * 
     * <p>Exemplo:
     * <pre>
     * {
     *   "users": "10 usuários",
     *   "storage": "100 GB",
     *   "support": "24/7"
     * }
     * </pre>
     */
    private Map<String, Object> features;
    
    /**
     * Metadados adicionais do produto (uso interno).
     * 
     * <p>Exemplo:
     * <pre>
     * {
     *   "category": "premium",
     *   "internalCode": "PROD-001",
     *   "salesChannel": "website"
     * }
     * </pre>
     */
    private Map<String, Object> metadata;
    
    /**
     * ID do produto no Stripe (opcional).
     * Formato: prod_xxxxxxxxxxxxx
     * 
     * <p><strong>Nota:</strong> Geralmente preenchido automaticamente
     * pelo sistema ao sincronizar com Stripe.
     */
    @Pattern(
        regexp = "^(prod_[A-Za-z0-9]{14,})?$",
        message = "Stripe Product ID deve seguir o formato: prod_xxxxxxxxxxxxx"
    )
    private String stripeProductId;
}
