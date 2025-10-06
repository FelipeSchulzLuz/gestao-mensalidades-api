package br.com.gestaomensalidades.gestao_mensalidades_api.application.services;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Price;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Product;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.PriceType;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.ProductType;
import br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service para operações de negócio relacionadas a Prices.
 * 
 * Prices definem os valores que podem ser cobrados por um Product.
 * Um produto pode ter múltiplos preços (ex: mensal, anual, etc.).
 * 
 * Tipos de Price:
 * - RECURRING: Para assinaturas recorrentes (com interval e interval_count)
 * - ONE_TIME: Para pagamentos únicos
 * 
 * Validação importante: O tipo do Price deve ser compatível com o tipo do Product.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @since 2025-10-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceService {

    private final PriceRepository priceRepository;
    private final ProductService productService;
    // ✅ MULTI-TENANCY: PriceService herda isolamento do ProductService
    // Todos os métodos que buscam Product já estão filtrados por organizationId

    /**
     * Cria um novo preço para um produto.
     * 
     * Valida:
     * - Produto existe e está ativo
     * - Tipo de preço é compatível com tipo de produto
     * - Preço recorrente tem intervalo definido
     * - Valor é positivo
     * 
     * @param price dados do preço
     * @return preço criado com ID
     * @throws IllegalArgumentException se validações falharem
     */
    @Transactional
    public Price create(Price price) {
        log.info("Creating price for product: {}", price.getProduct().getId());
        
        // ✅ MULTI-TENANCY: ProductService.findById() já valida organizationId
        // Validar produto
        Product product = productService.findById(price.getProduct().getId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Product not found with ID: " + price.getProduct().getId()
            ));
        
        if (!product.getIsActive()) {
            throw new IllegalStateException("Cannot create prices for inactive product");
        }
        
        // Validar compatibilidade de tipos
        validatePriceProductCompatibility(price.getType(), product.getType());
        
        // Validar preço recorrente tem intervalo
        if (price.getType() == PriceType.RECURRING) {
            if (price.getRecurringInterval() == null || price.getRecurringIntervalCount() == null) {
                throw new IllegalArgumentException(
                    "Recurring prices must have interval and interval_count defined"
                );
            }
            if (price.getRecurringIntervalCount() <= 0) {
                throw new IllegalArgumentException("Recurring interval_count must be greater than 0");
            }
        }
        
        // Validar valor positivo
        if (price.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price amount must be greater than 0");
        }
        
        // Definir valores padrão
        if (price.getIsActive() == null) {
            price.setIsActive(true);
        }
        if (price.getCurrency() == null) {
            price.setCurrency("BRL");
        }
        
        price.setProduct(product);
        
        Price saved = priceRepository.save(price);
        log.info("Price created successfully with ID: {}", saved.getId());
        
        return saved;
    }

    /**
     * Valida se o tipo de preço é compatível com o tipo de produto.
     * 
     * Regras:
     * - SUBSCRIPTION products devem ter RECURRING prices
     * - ONE_TIME products devem ter ONE_TIME prices
     * 
     * @param priceType tipo do preço
     * @param productType tipo do produto
     * @throws IllegalArgumentException se incompatível
     */
    private void validatePriceProductCompatibility(PriceType priceType, ProductType productType) {
        if (productType == ProductType.SUBSCRIPTION && priceType != PriceType.RECURRING) {
            throw new IllegalArgumentException(
                "SUBSCRIPTION products must have RECURRING prices. " +
                "Product type: " + productType + ", Price type: " + priceType
            );
        }
        
        if (productType == ProductType.ONE_TIME && priceType != PriceType.ONE_TIME) {
            throw new IllegalArgumentException(
                "ONE_TIME products must have ONE_TIME prices. " +
                "Product type: " + productType + ", Price type: " + priceType
            );
        }
    }

    /**
     * Busca preço por ID.
     * 
     * @param id ID do preço
     * @return Optional com o preço se encontrado
     */
    @Transactional(readOnly = true)
    public Optional<Price> findById(Long id) {
        log.debug("Finding price by ID: {}", id);
        return priceRepository.findById(id);
    }

    /**
     * Busca preço por Stripe Price ID.
     * 
     * @param stripePriceId ID do preço no Stripe
     * @return Optional com o preço se encontrado
     */
    @Transactional(readOnly = true)
    public Optional<Price> findByStripePriceId(String stripePriceId) {
        log.debug("Finding price by Stripe Price ID: {}", stripePriceId);
        return priceRepository.findByStripePriceId(stripePriceId);
    }

    /**
     * Lista todos os preços de um produto.
     * ⚠️ MULTI-TENANCY: Herdado do ProductService (filtra automaticamente).
     * 
     * @param productId ID do produto
     * @return lista de preços
     */
    @Transactional(readOnly = true)
    public List<Price> findByProduct(Long productId) {
        log.debug("Finding prices for product: {}", productId);
        
        // ✅ MULTI-TENANCY: ProductService.findById() já valida organizationId
        Product product = productService.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        
        return priceRepository.findByProduct(product);
    }

    /**
     * Lista preços ativos de um produto.
     * 
     * @param productId ID do produto
     * @return lista de preços ativos
     */
    @Transactional(readOnly = true)
    public List<Price> findActiveByProduct(Long productId) {
        log.debug("Finding active prices for product: {}", productId);
        
        return findByProduct(productId)
            .stream()
            .filter(Price::getIsActive)
            .toList();
    }

    /**
     * Lista preços ativos filtrados por tipo.
     * 
     * @param productId ID do produto
     * @param type tipo de preço (RECURRING ou ONE_TIME)
     * @return lista de preços
     */
    @Transactional(readOnly = true)
    public List<Price> findByProductAndType(Long productId, PriceType type) {
        log.debug("Finding prices for product: {} and type: {}", productId, type);
        
        Product product = productService.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        
        return priceRepository.findByProductAndType(product, type);
    }

    /**
     * Lista todos os preços ativos com produtos (JOIN FETCH).
     * 
     * @return lista de preços ativos
     */
    @Transactional(readOnly = true)
    public List<Price> findAllActive() {
        log.debug("Finding all active prices");
        return priceRepository.findAll()
            .stream()
            .filter(Price::getIsActive)
            .toList();
    }

    /**
     * Busca o preço padrão (mais barato) de um produto.
     * Útil para exibições rápidas.
     * 
     * @param productId ID do produto
     * @return Optional com o preço mais barato ativo
     */
    @Transactional(readOnly = true)
    public Optional<Price> findCheapestByProduct(Long productId) {
        log.debug("Finding cheapest price for product: {}", productId);
        
        return findActiveByProduct(productId)
            .stream()
            .min((p1, p2) -> p1.getAmount().compareTo(p2.getAmount()));
    }

    /**
     * Atualiza um preço existente.
     * 
     * CUIDADO: Atualizar preços pode impactar assinaturas ativas.
     * Considere criar um novo preço e desativar o antigo.
     * 
     * @param id ID do preço
     * @param updatedPrice dados atualizados
     * @return preço atualizado
     * @throws IllegalArgumentException se preço não existir
     */
    @Transactional
    public Price update(Long id, Price updatedPrice) {
        log.info("Updating price with ID: {}", id);
        
        Price existing = priceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Price not found with ID: " + id));
        
        // Não permite alterar tipo ou produto (crie novo preço ao invés)
        
        // Atualizar campos permitidos
        if (updatedPrice.getAmount() != null) {
            if (updatedPrice.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Price amount must be greater than 0");
            }
            existing.setAmount(updatedPrice.getAmount());
        }
        if (updatedPrice.getCurrency() != null) {
            existing.setCurrency(updatedPrice.getCurrency());
        }
        if (updatedPrice.getIsActive() != null) {
            existing.setIsActive(updatedPrice.getIsActive());
        }
        if (updatedPrice.getStripePriceId() != null) {
            existing.setStripePriceId(updatedPrice.getStripePriceId());
        }
        
        // Permitir atualizar intervalo recorrente apenas se for RECURRING
        if (existing.getType() == PriceType.RECURRING) {
            if (updatedPrice.getRecurringInterval() != null) {
                existing.setRecurringInterval(updatedPrice.getRecurringInterval());
            }
            if (updatedPrice.getRecurringIntervalCount() != null) {
                if (updatedPrice.getRecurringIntervalCount() <= 0) {
                    throw new IllegalArgumentException("Recurring interval_count must be greater than 0");
                }
                existing.setRecurringIntervalCount(updatedPrice.getRecurringIntervalCount());
            }
        }
        
        Price saved = priceRepository.save(existing);
        log.info("Price updated successfully: {}", saved.getId());
        
        return saved;
    }

    /**
     * Ativa um preço.
     * 
     * @param id ID do preço
     * @return preço ativado
     */
    @Transactional
    public Price activate(Long id) {
        log.info("Activating price with ID: {}", id);
        
        Price price = priceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Price not found with ID: " + id));
        
        price.setIsActive(true);
        return priceRepository.save(price);
    }

    /**
     * Desativa um preço.
     * 
     * CUIDADO: Desativar preços pode impactar novas assinaturas.
     * Assinaturas existentes com esse preço continuarão funcionando.
     * 
     * @param id ID do preço
     * @return preço desativado
     */
    @Transactional
    public Price deactivate(Long id) {
        log.info("Deactivating price with ID: {}", id);
        
        Price price = priceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Price not found with ID: " + id));
        
        price.setIsActive(false);
        return priceRepository.save(price);
    }

    /**
     * Calcula o valor total anualizado de um preço recorrente.
     * Útil para comparações e métricas MRR/ARR.
     * 
     * @param id ID do preço
     * @return valor anualizado
     * @throws IllegalStateException se não for preço recorrente
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateAnnualizedValue(Long id) {
        Price price = priceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Price not found with ID: " + id));
        
        if (price.getType() != PriceType.RECURRING) {
            throw new IllegalStateException("Can only calculate annualized value for recurring prices");
        }
        
        // Calcular quantos ciclos por ano
        int cyclesPerYear = switch (price.getRecurringInterval().toLowerCase()) {
            case "day" -> 365 / price.getRecurringIntervalCount();
            case "week" -> 52 / price.getRecurringIntervalCount();
            case "month" -> 12 / price.getRecurringIntervalCount();
            case "year" -> 1 / price.getRecurringIntervalCount();
            default -> throw new IllegalStateException("Unknown recurring interval: " + price.getRecurringInterval());
        };
        
        return price.getAmount().multiply(BigDecimal.valueOf(cyclesPerYear));
    }

    /**
     * Deleta um preço (soft delete via desativação).
     * 
     * Use com cuidado! Prefira usar deactivate() ao invés de deletar permanentemente.
     * 
     * @param id ID do preço
     */
    @Transactional
    public void delete(Long id) {
        log.warn("Deleting price with ID: {} (consider using deactivate instead)", id);
        
        if (!priceRepository.existsById(id)) {
            throw new IllegalArgumentException("Price not found with ID: " + id);
        }
        
        // Por segurança, fazemos soft delete
        deactivate(id);
    }
}
