package br.com.gestaomensalidades.gestao_mensalidades_api.domain.entities;

import br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade que representa um usuário do sistema.
 * Cada usuário é sincronizado com um Customer no Stripe.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_stripe_customer_id", columnList = "stripe_customer_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password") // Nunca logar senha
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Column(nullable = false)
    private String password; // Sempre hasheada com BCrypt
    
    /**
     * ID do Customer correspondente no Stripe
     * Formato: cus_xxxxxxxxxxxxx
     */
    @Column(name = "stripe_customer_id", unique = true, length = 50)
    private String stripeCustomerId;
    
    /**
     * Indica se o usuário está ativo no sistema
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Role/Perfil do usuário (USER, ADMIN)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Verifica se o usuário tem permissão de administrador
     * @return true se for admin
     */
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }
    
    /**
     * Define se o usuário está ativo
     * @param active true para ativar, false para desativar
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    /**
     * Verifica se o usuário tem Customer ID do Stripe configurado
     * @return true se tiver stripeCustomerId
     */
    public boolean hasStripeCustomer() {
        return this.stripeCustomerId != null && !this.stripeCustomerId.isBlank();
    }
}
