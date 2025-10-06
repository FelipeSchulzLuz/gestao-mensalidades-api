package br.com.gestaomensalidades.gestao_mensalidades_api.application.services;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.User;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.UserRole;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.exceptions.UserNotFoundException;
import br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service para operações relacionadas ao usuário autenticado.
 * 
 * Este service fornece métodos para:
 * - Obter usuário atualmente autenticado (JWT ou Firebase)
 * - Extrair organization_id para multi-tenancy
 * - Validar permissões do usuário
 * 
 * IMPORTANTE: Este é o ponto central para multi-tenancy!
 * Todos os services que precisam filtrar por organização devem usar
 * getCurrentOrganizationId() para garantir isolamento de dados.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @since 2025-10-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Obtém o usuário atualmente autenticado.
     * 
     * Funciona para ambos os tipos de autenticação:
     * - JWT: busca por email
     * - Firebase: busca por firebaseUid
     * 
     * O UnifiedAuthenticationFilter já populou o SecurityContextHolder com:
     * - username = email (JWT) ou uid (Firebase)
     * 
     * @return User autenticado
     * @throws UserNotFoundException se usuário não for encontrado no banco
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            log.warn("Tentativa de obter usuário atual sem autenticação válida");
            throw new UserNotFoundException("Usuário não autenticado");
        }
        
        String identifier = auth.getName(); // email ou firebaseUid
        
        log.debug("Buscando usuário autenticado: {}", identifier);
        
        // Tentar buscar por email OU Firebase UID
        // O identifier pode ser email (JWT) ou uid (Firebase)
        User user = userRepository.findByEmailOrFirebaseUid(identifier, identifier)
            .orElseThrow(() -> {
                log.error("Usuário autenticado não encontrado no banco de dados: {}", identifier);
                return new UserNotFoundException("Usuário não encontrado: " + identifier);
            });
        
        // Verificar se usuário está ativo
        if (!user.getIsActive()) {
            log.warn("Tentativa de acesso por usuário inativo: {}", user.getEmail());
            throw new UserNotFoundException("Usuário inativo");
        }
        
        log.debug("Usuário encontrado: ID={}, email={}, role={}", 
            user.getId(), user.getEmail(), user.getRole());
        
        return user;
    }

    /**
     * Obtém o ID da organização do usuário autenticado.
     * 
     * ⚠️ MÉTODO CRÍTICO PARA MULTI-TENANCY ⚠️
     * 
     * Este método DEVE ser usado em TODOS os services que acessam dados
     * da organização para garantir isolamento de dados entre organizações.
     * 
     * Exemplo de uso correto:
     * <pre>
     * {@code
     * Long orgId = userService.getCurrentOrganizationId();
     * List<Plan> plans = planRepository.findByOrganizationId(orgId);
     * }
     * </pre>
     * 
     * @return ID da organização do usuário
     * @throws UserNotFoundException se usuário não tiver organização
     */
    public Long getCurrentOrganizationId() {
        User user = getCurrentUser();
        
        if (user.getOrganizationId() == null) {
            log.error("Usuário {} não tem organização configurada!", user.getEmail());
            throw new UserNotFoundException("Usuário sem organização configurada");
        }
        
        return user.getOrganizationId();
    }

    /**
     * Verifica se o usuário atual é administrador.
     * 
     * @return true se o usuário tem role ADMIN
     */
    public boolean isCurrentUserAdmin() {
        User user = getCurrentUser();
        return user.isAdmin();
    }

    /**
     * Obtém o email do usuário autenticado.
     * 
     * @return email do usuário
     */
    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * Obtém o ID do usuário autenticado.
     * 
     * @return ID do usuário
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Verifica se o usuário autenticado tem acesso a um determinado recurso
     * da mesma organização.
     * 
     * Útil para validações como:
     * - Usuário pode ver apenas assinaturas da sua organização
     * - Usuário pode editar apenas planos da sua organização
     * 
     * @param resourceOrganizationId ID da organização do recurso
     * @return true se o usuário pertence à mesma organização
     */
    public boolean hasAccessToOrganization(Long resourceOrganizationId) {
        if (resourceOrganizationId == null) {
            return false;
        }
        
        Long userOrgId = getCurrentOrganizationId();
        return userOrgId.equals(resourceOrganizationId);
    }

    /**
     * Valida que o usuário tem acesso a um recurso da organização.
     * 
     * Lança exceção se o usuário não tiver acesso.
     * 
     * @param resourceOrganizationId ID da organização do recurso
     * @param resourceName nome do recurso (para mensagem de erro)
     * @throws UserNotFoundException se usuário não tiver acesso
     */
    public void validateOrganizationAccess(Long resourceOrganizationId, String resourceName) {
        if (!hasAccessToOrganization(resourceOrganizationId)) {
            log.warn("Usuário {} tentou acessar {} de outra organização", 
                getCurrentUserEmail(), resourceName);
            throw new UserNotFoundException(
                "Acesso negado: " + resourceName + " não pertence à sua organização"
            );
        }
    }

    // ---- Firebase / external auth helpers (used by controllers) ----

    /**
     * Checa existência de usuário por firebaseUid.
     */
    public boolean existsByFirebaseUid(String firebaseUid) {
        return userRepository.existsByFirebaseUid(firebaseUid);
    }

    /**
     * Busca usuário por email.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Busca usuário por firebaseUid.
     */
    public Optional<User> findByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid);
    }

    /**
     * Cria um usuário simples a partir de dados do Firebase.
     * Assumimos validações (email verificado etc.) feitas pelo controller.
     */
    public User createFirebaseUser(String firebaseUid, String email, String name, Long organizationId) {
        User newUser = new User();
        newUser.setFirebaseUid(firebaseUid);
        newUser.setEmail(email);
        newUser.setName(name != null ? name : (email != null ? email.split("@")[0] : ""));
        newUser.setOrganizationId(organizationId);
        newUser.setRole(UserRole.USER);
        newUser.setActive(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(newUser);
    }

    /**
     * Salva/atualiza um usuário existente (atualiza updatedAt automaticamente).
     */
    public User saveUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
