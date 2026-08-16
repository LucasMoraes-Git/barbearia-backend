package br.com.barbearia.back_end.agendamento.dto;

import java.time.LocalDate;
import java.util.List;

public record DisponibilidadeAgendamentoResponse(
        Long servicoId,
        String servicoNome,
        Integer duracaoMinutos,
        LocalDate data,
        List<IntervaloLivreResponse> intervalos
) {
}
