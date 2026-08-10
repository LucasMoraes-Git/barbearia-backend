package br.com.barbearia.back_end.servico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CriarServicoRequest(
        @NotBlank(message = "Obrigatório inserir nome.")
        String nome,
        @NotBlank(message = "Obrigatório inserir descrição.")
        String descricao,
        @PositiveOrZero(message = "Necessário valor maior ou igual a 0.")
        Double preco,
        @Positive(message = "Necessário valor maior que 0.")
        Integer duracaoMinutos

) {
}
