package br.com.gestaomensalidades.gestao_mensalidades_api.domain.exceptions;

/**
 * Exceção lançada quando tenta-se registrar um usuário com email já existente.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String email) {
        super("Usuário com email '" + email + "' já existe no sistema");
    }
}
