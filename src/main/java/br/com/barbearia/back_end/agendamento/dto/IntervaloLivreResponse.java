package br.com.barbearia.back_end.agendamento.dto;

import java.time.LocalDateTime;

public record IntervaloLivreResponse(
        LocalDateTime primeiroInicioPossivel,
        LocalDateTime ultimoInicioPossivel,
        LocalDateTime fimDeIntervaloLivre
) {
}
