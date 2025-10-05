package br.com.gestaomensalidades.gestao_mensalidades_api.application.usecases;

import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.request.RegisterRequestDTO;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.response.AuthResponseDTO;
import br.com.gestaomensalidades.gestao_mensalidades_api.application.ports.out.UserRepositoryPort;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities.User;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.UserRole;
import br.com.gestaomensalidades.gestao_mensalidades_api.domain.exceptions.UserAlreadyExistsException;
import br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para registro de novos usuários.
 * 
 * <p>Responsabilidades:
 * <ul>
 *   <li>Validar se email já existe</li>
 *   <li>Criptografar senha (BCrypt)</li>
 *   <li>Criar usuário com role USER</li>
 *   <li>Gerar token JWT</li>
 * </ul>
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
@Service
public class RegisterUserUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(RegisterUserUseCase.class);
    
    @Value("${spring.security.jwt.expiration}")
    private Long jwtExpiration;
    
    @Autowired
    private UserRepositoryPort userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    /**
     * Executa o registro de um novo usuário.
     * 
     * @param request Dados do usuário a ser registrado
     * @return AuthResponseDTO com token JWT gerado
     * @throws UserAlreadyExistsException se email já estiver cadastrado
     */
    @Transactional
    public AuthResponseDTO execute(RegisterRequestDTO request) {
        logger.info("Iniciando registro de usuário: {}", request.email());
        
        // Verificar se email já existe
        if (userRepository.existsByEmail(request.email())) {
            logger.warn("Tentativa de registro com email já existente: {}", request.email());
            throw new UserAlreadyExistsException("Email já cadastrado: " + request.email());
        }
        
        // Criar entidade User
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);
        user.setActive(true);
        
        // Salvar usuário
        User savedUser = userRepository.save(user);
        logger.info("Usuário registrado com sucesso: ID={}, email={}", savedUser.getId(), savedUser.getEmail());
        
        // Gerar token JWT
        String token = jwtTokenProvider.generateTokenFromUsername(savedUser.getEmail());
        
        return new AuthResponseDTO(token, jwtExpiration);
    }
}
