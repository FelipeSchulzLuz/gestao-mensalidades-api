package br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.response;

/**
 * DTO de resposta para autenticação (registro e login).
 * 
 * @param accessToken Token JWT gerado
 * @param tokenType Tipo do token (sempre "Bearer")
 * @param expiresIn Tempo de expiração em milissegundos
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        Long expiresIn
) {
    
    /**
     * Construtor de conveniência que define tokenType como "Bearer" automaticamente.
     * 
     * @param accessToken Token JWT gerado
     * @param expiresIn Tempo de expiração em milissegundos
     */
    public AuthResponseDTO(String accessToken, Long expiresIn) {
        this(accessToken, "Bearer", expiresIn);
    }
}
