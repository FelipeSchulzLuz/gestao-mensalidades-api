package br.com.gestaomensalidades.gestao_mensalidades_api.application.ports.out;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.User;

import java.util.Optional;

/**
 * Port de saída para operações de repositório de usuários.
 * 
 * <p>Interface que define o contrato para persistência de usuários,
 * seguindo o padrão Hexagonal Architecture (Ports and Adapters).
 * 
 * <p>A implementação concreta ficará na camada de infraestrutura.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
public interface UserRepositoryPort {
    
    /**
     * Salva um usuário no repositório.
     * 
     * @param user Usuário a ser salvo
     * @return Usuário salvo com ID gerado
     */
    User save(User user);
    
    /**
     * Busca um usuário por email.
     * 
     * @param email Email do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Busca um usuário por ID.
     * 
     * @param id ID do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<User> findById(Long id);
    
    /**
     * Verifica se existe um usuário com o email fornecido.
     * 
     * @param email Email a verificar
     * @return true se existe, false caso contrário
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca um usuário pelo ID do cliente Stripe.
     * 
     * @param stripeCustomerId ID do cliente no Stripe
     * @return Optional contendo o usuário se encontrado
     */
    Optional<User> findByStripeCustomerId(String stripeCustomerId);
}
