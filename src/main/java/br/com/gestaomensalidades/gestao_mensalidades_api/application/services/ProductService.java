package br.com.gestaomensalidades.gestao_mensalidades_api.application.services;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Organization;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.Product;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.ProductType;
import br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service para operações de negócio relacionadas a Products.
 * 
 * Products são o conceito central da nova arquitetura híbrida.
 * Um produto pode ser:
 * - SUBSCRIPTION: Para assinaturas recorrentes (Stripe subscription mode)
 * - ONE_TIME: Para compras únicas (Stripe payment mode)
 * 
 * Cada produto pertence a uma Organization e pode ter múltiplos Prices.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @since 2025-10-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrganizationService organizationService;

    /**
     * Cria um novo produto.
     * 
     * Valida:
     * - Organização existe e está ativa
     * - Organização suporta o tipo de produto (subscription/ecommerce)
     * - Tipo de produto é válido
     * 
     * @param product dados do produto
     * @return produto criado com ID
     * @throws IllegalArgumentException se validações falharem
     */
    @Transactional
    public Product create(Product product) {
        log.info("Creating product: {} for organization: {}", 
            product.getName(), product.getOrganization().getId());
        
        // Validar organização
        Organization organization = organizationService.findById(product.getOrganization().getId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Organization not found with ID: " + product.getOrganization().getId()
            ));
        
        if (!organization.getIsActive()) {
            throw new IllegalStateException("Cannot create products for inactive organization");
        }
        
        // Validar se organização suporta o tipo de produto
        if (product.getType() == ProductType.SUBSCRIPTION) {
            organizationService.validateCommerceMode(organization, true, false);
        } else if (product.getType() == ProductType.ONE_TIME) {
            organizationService.validateCommerceMode(organization, false, true);
        }
        
        // Definir valores padrão
        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }
        if (product.getIsFeatured() == null) {
            product.setIsFeatured(false);
        }
        if (product.getDisplayOrder() == null) {
            product.setDisplayOrder(0);
        }
        
        product.setOrganization(organization);
        
        Product saved = productRepository.save(product);
        log.info("Product created successfully with ID: {}", saved.getId());
        
        return saved;
    }

    /**
     * Busca produto por ID com organização carregada (JOIN FETCH).
     * 
     * @param id ID do produto
     * @return Optional com o produto se encontrado
     */
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        log.debug("Finding product by ID: {}", id);
        return productRepository.findById(id);
    }

    /**
     * Busca produto por Stripe Product ID.
     * 
     * @param stripeProductId ID do produto no Stripe
     * @return Optional com o produto se encontrado
     */
    @Transactional(readOnly = true)
    public Optional<Product> findByStripeProductId(String stripeProductId) {
        log.debug("Finding product by Stripe Product ID: {}", stripeProductId);
        return productRepository.findByStripeProductId(stripeProductId);
    }

    /**
     * Lista produtos ativos de uma organização, filtrados por tipo.
     * 
     * @param organizationId ID da organização
     * @param type tipo de produto (SUBSCRIPTION ou ONE_TIME), null para todos
     * @return lista de produtos
     */
    @Transactional(readOnly = true)
    public List<Product> findByOrganizationAndType(Long organizationId, ProductType type) {
        log.debug("Finding products for organization: {} and type: {}", organizationId, type);
        
        Organization organization = organizationService.findById(organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + organizationId));
        
        if (type == null) {
            return productRepository.findByOrganizationAndIsActive(organization, true);
        }
        
        return productRepository.findByOrganizationAndType(organization, type);
    }

    /**
     * Lista todos os produtos ativos com organização (JOIN FETCH para performance).
     * 
     * @return lista de produtos ativos
     */
    @Transactional(readOnly = true)
    public List<Product> findAllActive() {
        log.debug("Finding all active products");
        return productRepository.findAll()
            .stream()
            .filter(Product::getIsActive)
            .toList();
    }

    /**
     * Lista produtos ativos de uma organização.
     * 
     * @param organizationId ID da organização
     * @return lista de produtos ativos
     */
    @Transactional(readOnly = true)
    public List<Product> findByOrganization(Long organizationId) {
        return findByOrganizationAndType(organizationId, null);
    }

    /**
     * Lista produtos em destaque (featured) de uma organização.
     * 
     * @param organizationId ID da organização
     * @return lista de produtos em destaque ordenados por displayOrder
     */
    @Transactional(readOnly = true)
    public List<Product> findFeaturedByOrganization(Long organizationId) {
        log.debug("Finding featured products for organization: {}", organizationId);
        
        return productRepository.findAll()
            .stream()
            .filter(p -> p.getOrganization().getId().equals(organizationId))
            .filter(Product::getIsActive)
            .filter(Product::getIsFeatured)
            .sorted((p1, p2) -> p1.getDisplayOrder().compareTo(p2.getDisplayOrder()))
            .toList();
    }

    /**
     * Conta produtos ativos de uma organização.
     * 
     * @param organizationId ID da organização
     * @return número de produtos ativos
     */
    @Transactional(readOnly = true)
    public long countActiveByOrganization(Long organizationId) {
        log.debug("Counting active products for organization: {}", organizationId);
        
        Organization organization = organizationService.findById(organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + organizationId));
        
        return productRepository.countByOrganizationAndIsActive(organization, true);
    }

    /**
     * Atualiza um produto existente.
     * 
     * @param id ID do produto
     * @param updatedProduct dados atualizados
     * @return produto atualizado
     * @throws IllegalArgumentException se produto não existir
     */
    @Transactional
    public Product update(Long id, Product updatedProduct) {
        log.info("Updating product with ID: {}", id);
        
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        
        // Atualizar campos (não permite trocar organização ou tipo)
        if (updatedProduct.getName() != null) {
            existing.setName(updatedProduct.getName());
        }
        if (updatedProduct.getDescription() != null) {
            existing.setDescription(updatedProduct.getDescription());
        }
        if (updatedProduct.getIsActive() != null) {
            existing.setIsActive(updatedProduct.getIsActive());
        }
        if (updatedProduct.getIsFeatured() != null) {
            existing.setIsFeatured(updatedProduct.getIsFeatured());
        }
        if (updatedProduct.getDisplayOrder() != null) {
            existing.setDisplayOrder(updatedProduct.getDisplayOrder());
        }
        if (updatedProduct.getFeatures() != null) {
            existing.setFeatures(updatedProduct.getFeatures());
        }
        if (updatedProduct.getStripeProductId() != null) {
            existing.setStripeProductId(updatedProduct.getStripeProductId());
        }
        
        Product saved = productRepository.save(existing);
        log.info("Product updated successfully: {}", saved.getId());
        
        return saved;
    }

    /**
     * Ativa um produto.
     * 
     * @param id ID do produto
     * @return produto ativado
     */
    @Transactional
    public Product activate(Long id) {
        log.info("Activating product with ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        
        product.setIsActive(true);
        return productRepository.save(product);
    }

    /**
     * Desativa um produto.
     * 
     * @param id ID do produto
     * @return produto desativado
     */
    @Transactional
    public Product deactivate(Long id) {
        log.info("Deactivating product with ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        
        product.setIsActive(false);
        return productRepository.save(product);
    }

    /**
     * Define/remove produto como destaque.
     * 
     * @param id ID do produto
     * @param featured true para destacar, false para remover destaque
     * @return produto atualizado
     */
    @Transactional
    public Product setFeatured(Long id, boolean featured) {
        log.info("Setting product {} as featured: {}", id, featured);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        
        product.setIsFeatured(featured);
        return productRepository.save(product);
    }

    /**
     * Atualiza a ordem de exibição do produto.
     * 
     * @param id ID do produto
     * @param displayOrder nova ordem
     * @return produto atualizado
     */
    @Transactional
    public Product updateDisplayOrder(Long id, Integer displayOrder) {
        log.info("Updating display order for product {} to: {}", id, displayOrder);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        
        product.setDisplayOrder(displayOrder);
        return productRepository.save(product);
    }

    /**
     * Deleta um produto (soft delete via desativação).
     * 
     * Use com cuidado! Considera usar deactivate() ao invés de deletar permanentemente.
     * 
     * @param id ID do produto
     */
    @Transactional
    public void delete(Long id) {
        log.warn("Deleting product with ID: {} (consider using deactivate instead)", id);
        
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }
        
        // Por segurança, fazemos soft delete
        deactivate(id);
    }
}
