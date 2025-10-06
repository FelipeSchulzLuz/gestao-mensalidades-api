package br.com.gestaomensalidades.gestao_mensalidades_api.application.dto.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de criação/atualização de Organization.
 * 
 * Validações aplicadas:
 * - name: Obrigatório, entre 3 e 255 caracteres
 * - slug: Obrigatório, entre 3 e 100 caracteres, formato kebab-case (ex: "minha-empresa")
 * - active: Obrigatório, booleano
 * 
 * @author Sistema de Gestão de Mensalidades
 * @version 1.0
 */
public class OrganizationRequestDto {

    @NotBlank(message = "Nome da organização é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    private String name;

    @NotBlank(message = "Slug da organização é obrigatório")
    @Size(min = 3, max = 100, message = "Slug deve ter entre 3 e 100 caracteres")
    @Pattern(
        regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
        message = "Slug deve estar no formato kebab-case (ex: 'minha-empresa'), apenas letras minúsculas, números e hífens"
    )
    private String slug;

    @NotNull(message = "Status 'active' é obrigatório")
    private Boolean active;

    public OrganizationRequestDto() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
