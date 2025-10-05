package br.com.gestaomensalidades.gestao_mensalidades_api.application.usecases;

import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.request.LoginRequestDTO;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.response.AuthResponseDTO;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.exceptions.InvalidCredentialsException;
import br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para autenticação de usuários (login).
 * 
 * <p>Responsabilidades:
 * <ul>
 *   <li>Validar credenciais (email e senha)</li>
 *   <li>Autenticar via Spring Security</li>
 *   <li>Gerar token JWT</li>
 * </ul>
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
@Service
public class LoginUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginUseCase.class);
    
    @Value("${spring.security.jwt.expiration}")
    private Long jwtExpiration;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    /**
     * Executa o login do usuário.
     * 
     * @param request Credenciais do usuário
     * @return AuthResponseDTO com token JWT gerado
     * @throws InvalidCredentialsException se credenciais forem inválidas
     */
    public AuthResponseDTO execute(LoginRequestDTO request) {
        logger.info("Tentativa de login: {}", request.email());
        
        try {
            // Autenticar usuário
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
            
            // Gerar token JWT
            String token = jwtTokenProvider.generateToken(authentication);
            
            logger.info("Login bem-sucedido: {}", request.email());
            
            return new AuthResponseDTO(token, jwtExpiration);
            
        } catch (BadCredentialsException e) {
            logger.warn("Falha no login - credenciais inválidas: {}", request.email());
            throw new InvalidCredentialsException("Email ou senha incorretos");
            
        } catch (AuthenticationException e) {
            logger.error("Erro de autenticação para: {}", request.email(), e);
            throw new InvalidCredentialsException("Erro ao autenticar usuário");
        }
    }
}
