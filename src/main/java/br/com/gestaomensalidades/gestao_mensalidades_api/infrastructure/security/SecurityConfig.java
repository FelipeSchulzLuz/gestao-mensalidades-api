package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de segurança do Spring Security com autenticação híbrida.
 * 
 * <p>Suporta dois métodos de autenticação:
 * <ul>
 *   <li>JWT próprio (email/senha)</li>
 *   <li>Firebase Authentication (social login)</li>
 * </ul>
 * 
 * <p>Define:
 * <ul>
 *   <li>Rotas públicas e protegidas</li>
 *   <li>Configuração de CORS</li>
 *   <li>Filtro unificado (detecta tipo de token automaticamente)</li>
 *   <li>Política de sessão stateless</li>
 *   <li>Password encoder (BCrypt para JWT)</li>
 *   <li>Suporte a @PreAuthorize para controle de acesso fino</li>
 * </ul>
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 2.0 (Híbrido: JWT + Firebase)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Value("${application.cors.allowed-origins}")
    private String allowedOrigins;
    
    @Autowired
    private UnifiedAuthenticationFilter unifiedAuthenticationFilter;
    
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;
    
    /**
     * Configura o filtro de segurança do Spring Security.
     * 
     * @param http HttpSecurity para configuração
     * @return SecurityFilterChain configurado
     * @throws Exception se houver erro na configuração
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desabilitar CSRF (não necessário para API stateless com JWT)
                .csrf(csrf -> csrf.disable())
                
                // Configurar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Configurar tratamento de exceções
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                )
                
                // Configurar política de sessão (STATELESS para JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Configurar autorização de requisições
                .authorizeHttpRequests(auth -> auth
                        // Rotas públicas (sem autenticação)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/plans").permitAll()
                        .requestMatchers("/api/webhooks/stripe").permitAll()
                        
                        // Rotas administrativas (requer role ADMIN)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // Todas as outras rotas requerem autenticação
                        .anyRequest().authenticated()
                );
        
        // Adicionar filtro unificado (JWT + Firebase) antes do filtro de autenticação padrão
        http.addFilterBefore(unifiedAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Configura CORS para permitir requisições do frontend.
     * 
     * @return CorsConfigurationSource configurado
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origens permitidas (frontend)
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credenciais (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Headers expostos para o cliente
        configuration.setExposedHeaders(List.of("Authorization"));
        
        // Tempo de cache da configuração CORS (1 hora)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * Bean do AuthenticationManager para processar autenticação.
     * 
     * @param authConfig Configuração de autenticação
     * @return AuthenticationManager
     * @throws Exception se houver erro
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * Bean do PasswordEncoder usando BCrypt.
     * 
     * <p>BCrypt é um algoritmo de hash forte e recomendado para senhas.
     * 
     * @return PasswordEncoder configurado
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
