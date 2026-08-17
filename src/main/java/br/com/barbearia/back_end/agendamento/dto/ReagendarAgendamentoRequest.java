package br.com.barbearia.back_end.agendamento.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReagendarAgendamentoRequest(
        @NotNull(message = "A nova data deve ser informada.")
        LocalDateTime novaDataServico
) {
}