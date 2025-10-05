package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Provedor de tokens JWT para autenticação stateless.
 * 
 * <p>Responsável por:
 * <ul>
 *   <li>Gerar tokens JWT após autenticação bem-sucedida</li>
 *   <li>Validar tokens recebidos nas requisições</li>
 *   <li>Extrair informações do usuário do token</li>
 * </ul>
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;
    
    @Value("${spring.security.jwt.expiration}")
    private long jwtExpirationMs;
    
    /**
     * Gera um token JWT a partir da autenticação.
     * 
     * @param authentication Objeto de autenticação do Spring Security
     * @return Token JWT assinado
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateTokenFromUsername(userDetails.getUsername());
    }
    
    /**
     * Gera um token JWT a partir do username (email).
     * 
     * @param username Username do usuário (email)
     * @return Token JWT assinado
     */
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        SecretKey key = getSigningKey();
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * Extrai o username (email) do token JWT.
     * 
     * @param token Token JWT
     * @return Username extraído do token
     */
    public String getUsernameFromToken(String token) {
        SecretKey key = getSigningKey();
        
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
    
    /**
     * Valida um token JWT.
     * 
     * <p>Verifica:
     * <ul>
     *   <li>Assinatura do token</li>
     *   <li>Formato do token</li>
     *   <li>Expiração do token</li>
     * </ul>
     * 
     * @param authToken Token JWT a ser validado
     * @return true se válido, false caso contrário
     */
    public boolean validateToken(String authToken) {
        try {
            SecretKey key = getSigningKey();
            
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authToken);
            
            return true;
        } catch (SignatureException ex) {
            logger.error("Assinatura JWT inválida: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Token JWT malformado: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Token JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Token JWT não suportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("Claims JWT vazio: {}", ex.getMessage());
        }
        
        return false;
    }
    
    /**
     * Obtém a chave de assinatura a partir do secret configurado.
     * 
     * @return Chave secreta para assinar/validar tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Retorna o tempo de expiração do token em milissegundos.
     * 
     * @return Tempo de expiração em ms
     */
    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}
