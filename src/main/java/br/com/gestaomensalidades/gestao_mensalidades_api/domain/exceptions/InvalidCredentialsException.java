package br.com.gestaomensalidades.gestao_mensalidades_api.domain.exceptions;

/**
 * Exceção lançada quando as credenciais fornecidas no login são inválidas.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException() {
        super("Email ou senha inválidos");
    }
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
