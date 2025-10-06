package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para operações de persistência da entidade User.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca um usuário pelo email
     * @param email email do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Busca um usuário pelo Stripe Customer ID
     * @param stripeCustomerId ID do customer no Stripe
     * @return Optional contendo o usuário se encontrado
     */
    Optional<User> findByStripeCustomerId(String stripeCustomerId);
    
    /**
     * Busca um usuário pelo Firebase UID
     * @param firebaseUid UID do usuário no Firebase Authentication
     * @return Optional contendo o usuário se encontrado
     */
    Optional<User> findByFirebaseUid(String firebaseUid);
    
    /**
     * Busca um usuário pelo email OU Firebase UID.
     * Útil para sistema híbrido onde não sabemos qual método foi usado.
     * @param email email do usuário
     * @param firebaseUid Firebase UID
     * @return Optional contendo o usuário se encontrado
     */
    @Query("SELECT u FROM User u WHERE u.email = :email OR u.firebaseUid = :firebaseUid")
    Optional<User> findByEmailOrFirebaseUid(@Param("email") String email, @Param("firebaseUid") String firebaseUid);
    
    /**
     * Verifica se existe um usuário com o email informado
     * @param email email a verificar
     * @return true se existir usuário com este email
     */
    boolean existsByEmail(String email);
    
    /**
     * Verifica se existe um usuário com o Stripe Customer ID informado
     * @param stripeCustomerId ID do customer no Stripe
     * @return true se existir usuário com este Customer ID
     */
    boolean existsByStripeCustomerId(String stripeCustomerId);
    
    /**
     * Verifica se existe um usuário com o Firebase UID informado
     * @param firebaseUid Firebase UID a verificar
     * @return true se existir usuário com este Firebase UID
     */
    boolean existsByFirebaseUid(String firebaseUid);
    
    /**
     * Busca usuários ativos
     * @param isActive status de ativação
     * @return lista de usuários com o status especificado
     */
    @Query("SELECT u FROM User u WHERE u.isActive = :isActive ORDER BY u.createdAt DESC")
    java.util.List<User> findByIsActive(@Param("isActive") Boolean isActive);
    
    /**
     * Busca usuários por role/perfil
     * @param role role do usuário (USER, ADMIN)
     * @return lista de usuários com o role especificado
     */
    @Query("SELECT u FROM User u WHERE u.role = :role ORDER BY u.name")
    java.util.List<User> findByRole(@Param("role") String role);
    
    /**
     * Conta quantos usuários estão ativos
     * @return número de usuários ativos
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();
    
    /**
     * Busca um usuário ativo pelo email
     * Útil para autenticação (não permitir login de usuários inativos)
     * @param email email do usuário
     * @return Optional contendo o usuário se encontrado e ativo
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
}
