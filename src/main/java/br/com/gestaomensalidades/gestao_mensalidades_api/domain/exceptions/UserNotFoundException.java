package br.com.gestaomensalidades.gestao_mensalidades_api.domain.exceptions;

/**
 * Exceção lançada quando um usuário não é encontrado no sistema.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String identifier) {
        super("Usuário não encontrado: " + identifier);
    }
    
    public UserNotFoundException(Long id) {
        super("Usuário com ID " + id + " não foi encontrado");
    }
}
