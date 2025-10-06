package br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums;

/**
 * Tipos de autenticação suportados pelo sistema.
 * 
 * O sistema suporta autenticação híbrida, permitindo que usuários
 * se autentiquem via:
 * - JWT: Email/senha (sistema próprio)
 * - FIREBASE: Social login (Google, Facebook, etc.)
 * 
 * @author Sistema de Gestão de Mensalidades
 * @since 2025-10-05
 */
public enum AuthType {
    
    /**
     * Autenticação JWT própria (email/senha).
     * Token gerado pelo sistema após validação de credenciais.
     */
    JWT("JWT"),
    
    /**
     * Autenticação via Firebase (social login).
     * Token gerado pelo Firebase Authentication após login social.
     */
    FIREBASE("Firebase");

    private final String displayName;

    AuthType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
