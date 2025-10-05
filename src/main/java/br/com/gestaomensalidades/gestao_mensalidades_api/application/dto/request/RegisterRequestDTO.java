package br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de registro de novo usuário.
 * 
 * @param name Nome completo do usuário
 * @param email Email do usuário (deve ser único)
 * @param password Senha do usuário (mínimo 8 caracteres)
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
public record RegisterRequestDTO(
        
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
        String name,
        
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        String email,
        
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
        String password
) {
}
