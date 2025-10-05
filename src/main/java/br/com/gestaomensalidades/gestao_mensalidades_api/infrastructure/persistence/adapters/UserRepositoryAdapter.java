package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.adapters;

import br.com.gestaomensalidades.gestao_mensalidades_api.application.ports.out.UserRepositoryPort;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.User;
import br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador que implementa a porta UserRepositoryPort.
 * 
 * <p>Conecta a camada de aplicação com a infraestrutura de persistência (JPA).
 * Implementa o padrão Adapter (Hexagonal Architecture).
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    public Optional<User> findByStripeCustomerId(String stripeCustomerId) {
        return userRepository.findByStripeCustomerId(stripeCustomerId);
    }
}
