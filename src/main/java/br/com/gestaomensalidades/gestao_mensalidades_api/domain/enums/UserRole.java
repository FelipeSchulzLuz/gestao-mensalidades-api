package br.com.gestaomensalidades.gestao_mensalidades_api.domain.enums;

/**
 * Enum que define os papéis (roles) dos usuários no sistema.
 * 
 * <p>Utilizado para controle de acesso e autorização.
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
public enum UserRole {
    
    /**
     * Usuário comum do sistema.
     * 
     * <p>Permissões:
     * <ul>
     *   <li>Gerenciar própria conta</li>
     *   <li>Visualizar planos públicos</li>
     *   <li>Criar assinaturas</li>
     *   <li>Acessar portal do cliente</li>
     * </ul>
     */
    USER("ROLE_USER", "Usuário"),
    
    /**
     * Administrador do sistema.
     * 
     * <p>Permissões:
     * <ul>
     *   <li>Todas permissões de USER</li>
     *   <li>Criar/editar/deletar planos</li>
     *   <li>Visualizar métricas e relatórios</li>
     *   <li>Gerenciar todos usuários</li>
     *   <li>Configurar webhooks e integrações</li>
     * </ul>
     */
    ADMIN("ROLE_ADMIN", "Administrador");
    
    private final String authority;
    private final String displayName;
    
    UserRole(String authority, String displayName) {
        this.authority = authority;
        this.displayName = displayName;
    }
    
    /**
     * Retorna a authority usada pelo Spring Security.
     * 
     * @return Authority no formato "ROLE_XXX"
     */
    public String getAuthority() {
        return authority;
    }
    
    /**
     * Retorna o nome de exibição amigável.
     * 
     * @return Nome para exibir na UI
     */
    public String getDisplayName() {
        return displayName;
    }
}
