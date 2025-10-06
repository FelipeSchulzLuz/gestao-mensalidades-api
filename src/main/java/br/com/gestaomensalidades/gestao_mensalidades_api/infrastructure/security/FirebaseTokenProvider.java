package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Provider para validação de tokens Firebase ID.
 * 
 * Esta classe é responsável por validar tokens Firebase ID recebidos
 * do frontend e extrair informações do usuário autenticado.
 * 
 * Funciona em conjunto com o UnifiedAuthenticationFilter para suportar
 * autenticação híbrida (JWT próprio + Firebase Auth).
 * 
 * Erros tratados:
 * - TOKEN_EXPIRED: Token expirado (401)
 * - TOKEN_INVALID: Token mal formado ou inválido (401)
 * - TOKEN_REVOKED: Token revogado (401)
 * - Outros erros: Erro genérico de autenticação (401)
 * 
 * @author Sistema de Gestão de Mensalidades
 * @since 2025-10-05
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseTokenProvider {

    private final FirebaseAuth firebaseAuth;

    /**
     * Valida um token Firebase ID.
     * 
     * @param idToken token Firebase ID recebido do frontend
     * @return FirebaseToken contendo informações do usuário
     * @throws ResponseStatusException com status 401 se o token for inválido
     */
    public FirebaseToken validateToken(String idToken) {
        try {
            log.debug("Validando token Firebase...");
            FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(idToken);
            log.debug("Token Firebase válido para usuário: {}", firebaseToken.getUid());
            return firebaseToken;
            
        } catch (FirebaseAuthException ex) {
            log.error("Falha na validação do token Firebase: {}", ex.getMessage());
            handleFirebaseAuthException(ex);
            return null; // Nunca alcançado devido ao throw acima
        } catch (Exception ex) {
            log.error("Erro inesperado durante validação do token Firebase", ex);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro inesperado durante autenticação: " + ex.getMessage()
            );
        }
    }

    /**
     * Trata exceções específicas do Firebase Auth.
     * 
     * @param ex exceção do Firebase Auth
     * @throws ResponseStatusException com status HTTP apropriado
     */
    private void handleFirebaseAuthException(FirebaseAuthException ex) {
        String errorCode = ex.getAuthErrorCode() != null 
            ? ex.getAuthErrorCode().name() 
            : "UNKNOWN";
        
        switch (errorCode) {
            case "EXPIRED_ID_TOKEN":
            case "ID_TOKEN_EXPIRED":
                throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token Firebase expirado. Faça login novamente."
                );
                
            case "INVALID_ID_TOKEN":
            case "ID_TOKEN_INVALID":
                throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token Firebase inválido ou mal formado."
                );
                
            case "REVOKED_ID_TOKEN":
                throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token Firebase foi revogado. Faça login novamente."
                );
                
            case "USER_NOT_FOUND":
                throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário Firebase não encontrado."
                );
                
            default:
                log.warn("Erro Firebase não mapeado: {} - {}", errorCode, ex.getMessage());
                throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Falha na autenticação Firebase: " + ex.getMessage()
                );
        }
    }

    /**
     * Extrai o UID do usuário do token Firebase.
     * 
     * @param idToken token Firebase ID
     * @return UID do usuário
     */
    public String getUidFromToken(String idToken) {
        FirebaseToken token = validateToken(idToken);
        return token.getUid();
    }

    /**
     * Extrai o email do usuário do token Firebase.
     * 
     * @param idToken token Firebase ID
     * @return email do usuário (pode ser null)
     */
    public String getEmailFromToken(String idToken) {
        FirebaseToken token = validateToken(idToken);
        return token.getEmail();
    }

    /**
     * Extrai o nome do usuário do token Firebase.
     * 
     * @param idToken token Firebase ID
     * @return nome do usuário (pode ser null)
     */
    public String getNameFromToken(String idToken) {
        FirebaseToken token = validateToken(idToken);
        return token.getName();
    }

    /**
     * Verifica se o token tem a claim de admin.
     * 
     * Custom claims devem ser configuradas no Firebase Admin SDK
     * para marcar usuários como administradores.
     * 
     * @param idToken token Firebase ID
     * @return true se o usuário é admin
     */
    public boolean isAdmin(String idToken) {
        FirebaseToken token = validateToken(idToken);
        Object adminClaim = token.getClaims().get("admin");
        return adminClaim instanceof Boolean && (Boolean) adminClaim;
    }

    /**
     * Extrai um custom claim específico do token.
     * 
     * @param idToken token Firebase ID
     * @param claimName nome da claim
     * @return valor da claim (pode ser null)
     */
    public Object getCustomClaim(String idToken, String claimName) {
        FirebaseToken token = validateToken(idToken);
        return token.getClaims().get(claimName);
    }
}
