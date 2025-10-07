package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.security;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.AuthType;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Filtro unificado para autenticação híbrida (JWT + Firebase).
 * 
 * Este filtro intercepta todas as requisições e detecta automaticamente
 * o tipo de token (JWT próprio ou Firebase), delegando a validação para
 * o provider apropriado.
 * 
 * Fluxo:
 * 1. Extrai token do header Authorization
 * 2. Detecta tipo de token (JWT ou Firebase)
 * 3. Valida com o provider apropriado
 * 4. Popula SecurityContextHolder com dados do usuário
 * 5. Permite que a requisição continue
 * 
 * Rotas públicas (definidas em SecurityConfig) são ignoradas.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @since 2025-10-05
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnifiedAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final FirebaseTokenProvider firebaseTokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            String token = extractToken(request);
            
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                AuthType authType = detectAuthType(token);
                
                log.debug("Token detectado do tipo: {}", authType.getDisplayName());
                
                if (authType == AuthType.JWT) {
                    authenticateWithJwt(token, request);
                } else if (authType == AuthType.FIREBASE) {
                    authenticateWithFirebase(token, request);
                }
            }
            
        } catch (ResponseStatusException ex) {
            log.warn("Falha na autenticação: {}", ex.getReason());
            response.setStatus(ex.getStatusCode().value());
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\": \"%s\", \"message\": \"%s\"}",
                ex.getStatusCode(),
                ex.getReason()
            ));
            return;
            
        } catch (Exception ex) {
            log.error("Erro inesperado no filtro de autenticação", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Erro interno durante autenticação\"}"
            );
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token Bearer do header Authorization.
     * 
     * @param request requisição HTTP
     * @return token sem o prefixo "Bearer ", ou null se não encontrado
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }

    /**
     * Detecta o tipo de token analisando sua estrutura.
     * 
     * Estratégia de detecção:
     * 1. JWT próprio: 3 partes separadas por ponto + validação com nosso secret
     * 2. Firebase: qualquer outro formato (geralmente maior e sem pontos fixos)
     * 
     * @param token token a ser analisado
     * @return tipo de autenticação detectado
     */
    private AuthType detectAuthType(String token) {
        // JWT tem 3 partes separadas por ponto: header.payload.signature
        String[] parts = token.split("\\.");
        
        if (parts.length == 3) {
            // Tentar validar como JWT próprio
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    return AuthType.JWT;
                }
            } catch (Exception e) {
                log.debug("Token com 3 partes mas não é JWT válido, assumindo Firebase");
            }
        }
        
        // Se não for JWT válido, assumir Firebase
        return AuthType.FIREBASE;
    }

    /**
     * Autentica usando JWT próprio.
     * 
     * Extrai o email do usuário do token e busca suas authorities
     * no banco de dados via CustomUserDetailsService.
     * 
     * @param token token JWT
     * @param request requisição HTTP
     */
    private void authenticateWithJwt(String token, HttpServletRequest request) {
        String email = jwtTokenProvider.getEmailFromToken(token);
        
        if (email != null) {
            // Buscar role do token (claim customizada)
            String role = jwtTokenProvider.getRoleFromToken(token);
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role)
            );
            
            UserDetails userDetails = User.builder()
                .username(email)
                .password("") // Não relevante para token-based auth
                .authorities(authorities)
                .build();
            
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("Usuário autenticado via JWT: {}", email);
        }
    }

    /**
     * Autentica usando Firebase ID Token.
     * 
     * Valida o token com Firebase Admin SDK e extrai informações
     * do usuário. As authorities são baseadas em custom claims do Firebase.
     * 
     * @param token Firebase ID token
     * @param request requisição HTTP
     */
    private void authenticateWithFirebase(String token, HttpServletRequest request) {
        FirebaseToken firebaseToken = firebaseTokenProvider.validateToken(token);
        
        if (firebaseToken != null) {
            String uid = firebaseToken.getUid();
            
            // Verificar claim de admin (deve ser configurada no Firebase)
            boolean isAdmin = firebaseToken.getClaims().containsKey("admin") 
                && (Boolean) firebaseToken.getClaims().get("admin");
            
            String role = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role)
            );
            
            // Usar UID como username (identificador único do Firebase)
            UserDetails userDetails = User.builder()
                .username(uid)
                .password("") // Não relevante para Firebase auth
                .authorities(authorities)
                .build();
            
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("Usuário autenticado via Firebase: {} (email: {})", uid, firebaseToken.getEmail());
        }
    }
    
    /**
     * Define quais rotas devem pular este filtro (rotas públicas).
     * 
     * Importante: Este método é chamado ANTES do doFilterInternal.
     * Retornar true aqui significa que a requisição NÃO passará pelo filtro.
     * 
     * Rotas públicas:
     * - /api/auth/** (login, register)
     * - /api/webhooks/** (webhooks do Stripe)
     * - /api/actuator/health (health check do Docker)
     * 
     * @param request requisição HTTP
     * @return true se a requisição deve pular o filtro
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        boolean shouldSkip = path.startsWith("/api/auth/") ||
                           path.startsWith("/api/webhooks/") ||
                           path.equals("/api/actuator/health") ||
                           path.startsWith("/api/actuator/health/") ||
                           path.equals("/actuator/health") ||            // Path sem /api também
                           path.startsWith("/actuator/health/");         // Path sem /api também
        
        // Log para debug
        if (path.contains("health") || path.contains("actuator")) {
            log.info("shouldNotFilter - path: [{}], shouldSkip: {}", path, shouldSkip);
        }
        
        return shouldSkip;
    }
}
