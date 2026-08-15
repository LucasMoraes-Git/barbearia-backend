package br.com.barbearia.back_end.auth.dto;

public record LoginResponse(
        String token,
        String tipo,
        Long expiraEmSegundos
) {
}
