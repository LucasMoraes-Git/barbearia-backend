package br.com.barbearia.back_end.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "E-mail deve ser inserido")
        @Email(message = "O e-mail informado é inválido")
        String email,

        @NotBlank(message = "A senha deve ser informada")
        String senha
) {
}
