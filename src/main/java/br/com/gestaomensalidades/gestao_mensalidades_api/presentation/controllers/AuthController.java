package br.com.gestaomensalidades.gestao_mensalidades_api.presentation.controllers;

import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.request.LoginRequestDTO;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.request.RegisterRequestDTO;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.response.AuthResponseDTO;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.usecases.LoginUseCase;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.usecases.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autenticação JWT", description = "Endpoints para autenticação usando email/senha (JWT próprio)")
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
    @Operation(
        summary = "Registrar novo usuário",
        description = "Cria uma nova conta de usuário com email e senha. Retorna um token JWT para autenticação."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuário registrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos ou email já cadastrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
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
    @Operation(
        summary = "Fazer login",
        description = "Autentica um usuário existente com email e senha. Retorna um token JWT válido."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login realizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        logger.info("Requisição de login recebida: {}", request.email());
        
        AuthResponseDTO response = loginUseCase.execute(request);
        
        return ResponseEntity.ok(response);
    }
}
