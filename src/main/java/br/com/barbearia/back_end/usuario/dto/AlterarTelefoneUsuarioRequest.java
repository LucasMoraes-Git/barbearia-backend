package br.com.barbearia.back_end.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AlterarTelefoneUsuarioRequest(
        @NotBlank(message = "telefone deve ser inserido")
        @Pattern(
                regexp = "\\d{10,11}",
                message = "O telefone deve possuir 10 ou 11 números."
        )
        String telefone
) {

}
