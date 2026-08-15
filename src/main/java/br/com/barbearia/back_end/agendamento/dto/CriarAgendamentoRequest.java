package br.com.barbearia.back_end.agendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CriarAgendamentoRequest(
        @NotNull(message = "O serviço deve ser informado.")
        Long servicoId,

        @NotNull(message = "A data do serviço deve ser informada.")
        @Future(message = "O agendamento deve ser realizado para uma data futura.")
        LocalDateTime dataServico,

        @Size(
                max = 500,
                message = "A observação deve possuir no máximo 500 caracteres."
        )
        String observacao
) {
}
