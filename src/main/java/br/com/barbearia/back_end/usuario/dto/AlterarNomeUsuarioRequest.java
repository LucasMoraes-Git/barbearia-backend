package br.com.barbearia.back_end.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarNomeUsuarioRequest(
        @Size(max = 100, message = "O nome deve possuir no máximo 100 caracteres")
        @NotBlank(message = "Nome deve ser inserido")
        String nome
) {
}
