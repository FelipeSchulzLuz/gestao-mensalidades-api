package br.com.gestaomensalidades.gestao_mensalidades_api.presentation.controllers;

import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.request.LoginRequestDTO;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.request.RegisterRequestDTO;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.response.AuthResponseDTO;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.usecases.LoginUseCase;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.usecases.RegisterUserUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para endpoints de autenticação.
 * 
 * <p>Endpoints disponíveis:
 * <ul>
 *   <li>POST /api/auth/register - Registro de novo usuário</li>
 *   <li>POST /api/auth/login - Login de usuário</li>
 * </ul>
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private RegisterUserUseCase registerUserUseCase;
    
    @Autowired
    private LoginUseCase loginUseCase;
    
    /**
     * Endpoint para registro de novo usuário.
     * 
     * @param request Dados do usuário (nome, email, senha)
     * @return AuthResponseDTO com token JWT (201 Created)
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        logger.info("Requisição de registro recebida: {}", request.email());
        
        AuthResponseDTO response = registerUserUseCase.execute(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Endpoint para login de usuário.
     * 
     * @param request Credenciais do usuário (email, senha)
     * @return AuthResponseDTO com token JWT (200 OK)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        logger.info("Requisição de login recebida: {}", request.email());
        
        AuthResponseDTO response = loginUseCase.execute(request);
        
        return ResponseEntity.ok(response);
    }
}
