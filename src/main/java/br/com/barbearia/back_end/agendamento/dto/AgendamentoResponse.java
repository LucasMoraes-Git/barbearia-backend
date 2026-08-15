package br.com.barbearia.back_end.agendamento.dto;

import br.com.barbearia.back_end.agendamento.enums.StatusAgendamentoEnum;

import java.time.LocalDateTime;

public record AgendamentoResponse(
        Long id,
        Long usuarioId,
        String usuarioNome,
        String usuarioEmail,
        String usuarioTelefone,
        Long servicoId,
        String servicoNome,
        LocalDateTime dataCriado,
        LocalDateTime dataServico,
        StatusAgendamentoEnum status,
        String observacao
) {
}
