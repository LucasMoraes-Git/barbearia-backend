package br.com.barbearia.back_end.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaUsuarioRequest(
        @NotBlank(message = "A senha atual deve ser informada.")
        String senhaAtual,

        @NotBlank(message = "A nova senha deve ser informada.")
        @Size(
                min = 8,
                max = 72,
                message = "A nova senha deve possuir entre 8 e 72 caracteres."
        )
        String novaSenha,

        @NotBlank(message = "A confirmação da senha deve ser informada.")
        String confirmacaoNovaSenha
) {
}
