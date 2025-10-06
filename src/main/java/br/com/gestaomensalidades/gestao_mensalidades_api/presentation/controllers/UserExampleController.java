package br.com.gestaomensalidades.gestao_mensalidades_api.presentation.controllers;

import br.com.gestaomensalidades.gestao_mensalidades_api.application.services.UserService;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller de exemplo demonstrando uso de autenticação híbrida e multi-tenancy.
 * 
 * Este controller mostra os padrões recomendados para:
 * 1. Uso de @PreAuthorize para controle de acesso
 * 2. Isolamento de dados por organização (multi-tenancy)
 * 3. Funcionamento com ambos JWT e Firebase Auth
 * 
 * ⚠️ PADRÕES IMPORTANTES:
 * 
 * - SEMPRE use userService.getCurrentOrganizationId() para filtrar dados
 * - SEMPRE use @PreAuthorize para proteger endpoints administrativos
 * - NUNCA confie em organizationId enviado pelo cliente
 * - SEMPRE valide que o recurso pertence à organização do usuário
 * 
 * @author Sistema de Gestão de Mensalidades
 * @since 2025-10-05
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserExampleController {

    private final UserService userService;

    /**
     * Endpoint público acessível por qualquer usuário autenticado.
     * 
     * Demonstra:
     * - Como obter dados do usuário atual
     * - Funciona automaticamente para JWT e Firebase
     * 
     * @return Dados do usuário autenticado
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        log.info("Requisição para obter dados do usuário atual");
        
        User user = userService.getCurrentUser();
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());
        response.put("organizationId", user.getOrganizationId());
        response.put("isActive", user.getIsActive());
        response.put("authMethod", user.usesFirebaseAuth() ? "Firebase" : "JWT");
        
        log.info("Dados do usuário retornados: ID={}, org={}", user.getId(), user.getOrganizationId());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint que demonstra multi-tenancy.
     * 
     * Demonstra:
     * - Como obter organizationId do usuário autenticado
     * - Como usar esse ID para filtrar dados
     * - Segurança: usuário não pode ver dados de outras organizações
     * 
     * @return Informações da organização do usuário
     */
    @GetMapping("/organization")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrganizationInfo() {
        log.info("Requisição para obter informações da organização");
        
        // ⚠️ PADRÃO MULTI-TENANCY: Sempre obter organizationId do usuário autenticado
        Long organizationId = userService.getCurrentOrganizationId();
        
        // TODO: Aqui você buscaria dados da organização no banco
        // Exemplo: Organization org = organizationRepository.findById(organizationId)
        
        Map<String, Object> response = new HashMap<>();
        response.put("organizationId", organizationId);
        response.put("message", "Esta organização é filtrada automaticamente pelo sistema");
        response.put("tip", "Use este padrão em TODOS os endpoints que acessam dados da organização");
        
        log.info("Organização retornada: ID={}", organizationId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint administrativo - apenas ADMIN pode acessar.
     * 
     * Demonstra:
     * - Como proteger endpoints apenas para administradores
     * - Mesmo assim respeitando multi-tenancy
     * - Admin só vê dados da própria organização
     * 
     * @return Estatísticas administrativas da organização
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        log.info("Requisição administrativa para estatísticas");
        
        // ⚠️ IMPORTANTE: Mesmo admin só vê dados da própria organização
        Long organizationId = userService.getCurrentOrganizationId();
        User admin = userService.getCurrentUser();
        
        // TODO: Buscar estatísticas filtradas por organizationId
        // Exemplo: int userCount = userRepository.countByOrganizationId(organizationId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("adminId", admin.getId());
        response.put("adminEmail", admin.getEmail());
        response.put("organizationId", organizationId);
        response.put("message", "Admin só vê dados da própria organização");
        response.put("totalUsers", "TODO: implementar contagem");
        response.put("totalPlans", "TODO: implementar contagem");
        
        log.info("Estatísticas admin retornadas para org: {}", organizationId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint que demonstra validação de acesso a recurso específico.
     * 
     * Demonstra:
     * - Como validar se usuário tem acesso a um recurso
     * - Prevenção de acesso a recursos de outras organizações
     * 
     * @param userId ID do usuário a ser consultado
     * @return Dados do usuário se pertencer à mesma organização
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        log.info("Requisição para obter usuário: {}", userId);
        
        // TODO: Buscar usuário no banco
        // User targetUser = userRepository.findById(userId)
        //     .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        
        // ⚠️ VALIDAÇÃO MULTI-TENANCY: Verificar se usuário pertence à mesma organização
        // userService.validateOrganizationAccess(targetUser.getOrganizationId(), "Usuário");
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("message", "Aqui você retornaria os dados do usuário");
        response.put("tip", "Sempre valide que o recurso pertence à organização do usuário");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint que demonstra criação de recurso com multi-tenancy.
     * 
     * Demonstra:
     * - Como criar recurso sempre associado à organização do usuário
     * - Prevenção de criação em outras organizações
     * 
     * @param request dados do recurso a criar
     * @return Recurso criado
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createResource(@RequestBody Map<String, String> request) {
        log.info("Requisição para criar recurso");
        
        // ⚠️ PADRÃO MULTI-TENANCY: Sempre associar novo recurso à organização do usuário
        Long organizationId = userService.getCurrentOrganizationId();
        Long userId = userService.getCurrentUserId();
        
        // TODO: Criar recurso no banco
        // Resource resource = new Resource();
        // resource.setOrganizationId(organizationId); // ← CRÍTICO!
        // resource.setCreatedBy(userId);
        // resourceRepository.save(resource);
        
        Map<String, Object> response = new HashMap<>();
        response.put("organizationId", organizationId);
        response.put("createdBy", userId);
        response.put("message", "Recurso criado com sucesso");
        response.put("tip", "Sempre force organizationId = getCurrentOrganizationId()");
        
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Endpoint de teste para verificar tipo de autenticação.
     * 
     * Útil para debugging e entender qual método foi usado.
     * 
     * @return Informações sobre o método de autenticação usado
     */
    @GetMapping("/auth-info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAuthInfo() {
        User user = userService.getCurrentUser();
        
        Map<String, Object> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("usesJWT", user.usesJwtAuth());
        response.put("usesFirebase", user.usesFirebaseAuth());
        response.put("hasPassword", user.getPassword() != null);
        response.put("hasFirebaseUid", user.getFirebaseUid() != null);
        response.put("firebaseUid", user.getFirebaseUid()); // null se JWT
        
        return ResponseEntity.ok(response);
    }
}
