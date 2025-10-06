package com.gestao.mensalidades.presentation.controllers;

import br.com.gestaomensalidades.gestao_mensalidades_api.application.services.UserService;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.User;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.UserRole;
import br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.persistence.repositories.UserRepository;
import br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.security.FirebaseTokenProvider;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller para autenticação via Firebase.
 * 
 * <p>Este controller gerencia login e registro de usuários usando Firebase Authentication.
 * Os usuários autenticam-se no frontend usando Firebase SDK (Google, Facebook, etc.),
 * obtêm um ID token, e enviam para este backend para sincronização com o banco de dados.
 * 
 * <h3>Fluxo de Autenticação:</h3>
 * <pre>
 * 1. Frontend: Usuário faz login social (Google/Facebook) via Firebase SDK
 * 2. Frontend: Obtém Firebase ID token
 * 3. Frontend: Envia token para POST /api/auth/firebase-login ou /firebase-register
 * 4. Backend: Valida token com Firebase Admin SDK
 * 5. Backend: Cria/busca usuário no banco por firebaseUid
 * 6. Backend: Retorna informações do usuário
 * 7. Frontend: Armazena token Firebase (válido por 1h, renovado automaticamente)
 * 8. Frontend: Envia token Firebase em requisições subsequentes
 * 9. Backend: UnifiedAuthenticationFilter detecta e valida automaticamente
 * </pre>
 * 
 * <h3>Observações Importantes:</h3>
 * <ul>
 *   <li>Não geramos JWT próprio - usamos o token Firebase diretamente</li>
 *   <li>Token Firebase expira em 1h mas é renovado automaticamente pelo SDK frontend</li>
 *   <li>organizationId é OBRIGATÓRIO no registro - define tenant do usuário</li>
 *   <li>firebaseUid é único no banco - um usuário Firebase não pode ter duplicatas</li>
 *   <li>Campo password permanece null para usuários Firebase</li>
 * </ul>
 * 
 * @version 1.0
 * @since 2025-10-05
 */
@RestController
@RequestMapping("/api/auth")
public class FirebaseAuthController {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthController.class);

    @Autowired
    private FirebaseTokenProvider firebaseTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     * Registra novo usuário via Firebase Authentication.
     * 
     * <p>Cria um novo usuário no banco de dados associado ao Firebase UID.
     * Este endpoint deve ser chamado APÓS o usuário fazer login social no frontend.
     * 
     * <h4>Validações:</h4>
     * <ul>
     *   <li>Token Firebase deve ser válido</li>
     *   <li>Email deve estar verificado no Firebase</li>
     *   <li>firebaseUid não pode já existir no banco</li>
     *   <li>organizationId é obrigatório (multi-tenancy)</li>
     * </ul>
     * 
     * <h4>Request Body:</h4>
     * <pre>
     * {
     *   "firebaseToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "organizationId": 1
     * }
     * </pre>
     * 
     * <h4>Response Success (201):</h4>
     * <pre>
     * {
     *   "message": "Usuário registrado com sucesso via Firebase",
     *   "user": {
     *     "id": 123,
     *     "email": "usuario@gmail.com",
     *     "name": "João Silva",
     *     "firebaseUid": "abc123xyz",
     *     "organizationId": 1,
     *     "role": "USER",
     *     "authMethod": "FIREBASE"
     *   }
     * }
     * </pre>
     * 
     * @param request Map contendo firebaseToken (String) e organizationId (Long)
     * @return ResponseEntity com dados do usuário criado
     */
    @PostMapping("/firebase-register")
    public ResponseEntity<Map<String, Object>> registerWithFirebase(
            @RequestBody Map<String, Object> request) {
        
        try {
            // Extrair e validar parâmetros
            String firebaseToken = (String) request.get("firebaseToken");
            if (firebaseToken == null || firebaseToken.trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "firebaseToken é obrigatório"));
            }

            Object orgIdObj = request.get("organizationId");
            if (orgIdObj == null) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "organizationId é obrigatório"));
            }
            Long organizationId = Long.valueOf(orgIdObj.toString());

            logger.info("Tentando registrar usuário via Firebase para organizationId: {}", organizationId);

            // Validar token Firebase
            FirebaseToken decodedToken = firebaseTokenProvider.validateToken(firebaseToken);
            String firebaseUid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();

            logger.info("Token Firebase validado. UID: {}, Email: {}", firebaseUid, email);

            // Verificar se email está verificado (opcional mas recomendado)
            boolean emailVerified = decodedToken.isEmailVerified();
            if (!emailVerified) {
                logger.warn("Email não verificado para usuário Firebase: {}", email);
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Email não verificado no Firebase"));
            }

            // Verificar se firebaseUid já existe
            if (userRepository.existsByFirebaseUid(firebaseUid)) {
                logger.warn("firebaseUid já existe: {}", firebaseUid);
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Usuário Firebase já registrado. Use /firebase-login."));
            }

            // Verificar se email já existe (evitar conflitos)
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                logger.warn("Email já existe no sistema: {}", email);
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email já cadastrado. Se você já tem conta com senha, use login normal."));
            }

            // Criar novo usuário
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name != null ? name : email.split("@")[0]); // Fallback para parte antes do @
            newUser.setFirebaseUid(firebaseUid);
            newUser.setOrganizationId(organizationId);
            newUser.setRole(UserRole.USER); // Novo usuário sempre começa como USER
            newUser.setActive(true);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());
            // password permanece null - usuário Firebase não tem senha

            User savedUser = userRepository.save(newUser);
            logger.info("Usuário Firebase registrado com sucesso. ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());

            // Montar resposta
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuário registrado com sucesso via Firebase");
            response.put("user", buildUserResponse(savedUser));

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Token Firebase inválido: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token Firebase inválido: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro ao registrar usuário Firebase", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro ao registrar usuário: " + e.getMessage()));
        }
    }

    /**
     * Login de usuário via Firebase Authentication.
     * 
     * <p>Busca usuário existente pelo Firebase UID e retorna seus dados.
     * Este endpoint deve ser chamado quando o usuário já está registrado e faz login social.
     * 
     * <h4>Validações:</h4>
     * <ul>
     *   <li>Token Firebase deve ser válido</li>
     *   <li>Usuário deve existir no banco (firebaseUid)</li>
     *   <li>Usuário deve estar ativo</li>
     * </ul>
     * 
     * <h4>Request Body:</h4>
     * <pre>
     * {
     *   "firebaseToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * </pre>
     * 
     * <h4>Response Success (200):</h4>
     * <pre>
     * {
     *   "message": "Login via Firebase realizado com sucesso",
     *   "user": {
     *     "id": 123,
     *     "email": "usuario@gmail.com",
     *     "name": "João Silva",
     *     "firebaseUid": "abc123xyz",
     *     "organizationId": 1,
     *     "role": "USER",
     *     "authMethod": "FIREBASE"
     *   }
     * }
     * </pre>
     * 
     * @param request Map contendo firebaseToken (String)
     * @return ResponseEntity com dados do usuário
     */
    @PostMapping("/firebase-login")
    public ResponseEntity<Map<String, Object>> loginWithFirebase(
            @RequestBody Map<String, String> request) {
        
        try {
            // Extrair token
            String firebaseToken = request.get("firebaseToken");
            if (firebaseToken == null || firebaseToken.trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "firebaseToken é obrigatório"));
            }

            logger.info("Tentando login via Firebase");

            // Validar token Firebase
            FirebaseToken decodedToken = firebaseTokenProvider.validateToken(firebaseToken);
            String firebaseUid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            logger.info("Token Firebase validado. UID: {}, Email: {}", firebaseUid, email);

            // Buscar usuário por firebaseUid
            Optional<User> userOptional = userRepository.findByFirebaseUid(firebaseUid);
            
            if (userOptional.isEmpty()) {
                logger.warn("Usuário Firebase não encontrado: {}", firebaseUid);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuário não registrado. Use /firebase-register primeiro."));
            }

            User user = userOptional.get();

            // Verificar se usuário está ativo
            if (!user.getIsActive()) {
                logger.warn("Tentativa de login de usuário inativo: {}", user.getEmail());
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Usuário inativo. Contate o administrador."));
            }

            // Atualizar última atividade (opcional)
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            logger.info("Login Firebase bem-sucedido. User ID: {}, Email: {}", user.getId(), user.getEmail());

            // Montar resposta
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login via Firebase realizado com sucesso");
            response.put("user", buildUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Token Firebase inválido: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token Firebase inválido: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro ao fazer login Firebase", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro ao fazer login: " + e.getMessage()));
        }
    }

    /**
     * Endpoint de debug para verificar validade de token Firebase.
     * 
     * <p>Útil para desenvolvimento e troubleshooting.
     * NÃO use em produção sem autenticação adicional.
     * 
     * @param request Map contendo firebaseToken
     * @return ResponseEntity com informações do token
     */
    @PostMapping("/firebase-verify")
    public ResponseEntity<Map<String, Object>> verifyFirebaseToken(
            @RequestBody Map<String, String> request) {
        
        try {
            String firebaseToken = request.get("firebaseToken");
            if (firebaseToken == null || firebaseToken.trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "firebaseToken é obrigatório"));
            }

            FirebaseToken decodedToken = firebaseTokenProvider.validateToken(firebaseToken);

            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("valid", true);
            tokenInfo.put("uid", decodedToken.getUid());
            tokenInfo.put("email", decodedToken.getEmail());
            tokenInfo.put("name", decodedToken.getName());
            tokenInfo.put("emailVerified", decodedToken.isEmailVerified());
            tokenInfo.put("issuer", decodedToken.getIssuer());
            tokenInfo.put("picture", decodedToken.getPicture());

            // Verificar se usuário existe no banco
            Optional<User> user = userRepository.findByFirebaseUid(decodedToken.getUid());
            tokenInfo.put("registeredInDatabase", user.isPresent());
            if (user.isPresent()) {
                tokenInfo.put("userId", user.get().getId());
                tokenInfo.put("organizationId", user.get().getOrganizationId());
            }

            return ResponseEntity.ok(tokenInfo);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "valid", false,
                        "error", e.getMessage()
                    ));
        }
    }

    /**
     * Monta resposta padronizada com dados do usuário.
     * 
     * @param user Entidade User
     * @return Map com dados serializáveis do usuário
     */
    private Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("name", user.getName());
        userMap.put("firebaseUid", user.getFirebaseUid());
        userMap.put("organizationId", user.getOrganizationId());
        userMap.put("role", user.getRole().name());
        userMap.put("authMethod", user.usesFirebaseAuth() ? "FIREBASE" : "JWT");
        userMap.put("active", user.getIsActive());
        userMap.put("createdAt", user.getCreatedAt());
        return userMap;
    }
}
