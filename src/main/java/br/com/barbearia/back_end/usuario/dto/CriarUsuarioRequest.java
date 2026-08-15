package br.com.barbearia.back_end.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequest(

        @Size(max = 100, message = "O nome deve possuir no máximo 100 caracteres")
        @NotBlank(message = "Nome deve ser inserido")
        String nome,

        @Email
        @Size(max = 100, message = "O e-mail deve possuir no máximo 100 caracteres")
        @NotBlank(message = "E-mail deve ser inserido")
        String email,

        @NotBlank(message = "telefone deve ser inserido")
        @Pattern(
                regexp = "\\d{10,11}",
                message = "O telefone deve possuir 10 ou 11 números."
        )
        String telefone,

        @Size(min = 8, max = 72, message = "A senha deve possuir entre 8 a 72 caracteres")
        @NotBlank(message = "Senha deve ser inserida")
        String senha
) {
}
