package br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de login.
 * 
 * @param email Email do usuário
 * @param password Senha do usuário
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
public record LoginRequestDTO(
        
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        String email,
        
        @NotBlank(message = "Senha é obrigatória")
        String password
) {
}
