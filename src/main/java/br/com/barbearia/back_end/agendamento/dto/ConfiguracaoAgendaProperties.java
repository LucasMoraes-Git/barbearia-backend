package br.com.barbearia.back_end.agendamento.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;

@Validated
@ConfigurationProperties(prefix = "barbearia.agenda")
public record ConfiguracaoAgendaProperties(

        @NotNull
        ZoneId fusoHorario,

        @NotNull
        LocalTime horarioAbertura,

        @NotNull
        LocalTime horarioFechamento,

        @NotEmpty
        Set<DayOfWeek> diasAtendimento,

        boolean intervaloHabilitado,

        @NotNull
        LocalTime intervaloInicio,

        @NotNull
        LocalTime intervaloFim,

        //@NotNull
        //Duration intervaloHorarios,

        @NotNull
        Duration antecedenciaMinima,

        @NotNull
        Duration antecedenciaMaxima
) {

    public ConfiguracaoAgendaProperties {
        if (
                horarioAbertura != null
                && horarioFechamento != null
                && !horarioAbertura.isBefore(horarioFechamento)
        ) {
            throw new IllegalArgumentException(
                    "O horário de abertura deve ser anterior ao fechamento."
            );
        }

        if (
                intervaloHabilitado
                && intervaloInicio != null
                && intervaloFim != null
                && !intervaloInicio.isBefore(intervaloFim)
        ) {
            throw new IllegalArgumentException(
                    "O início do intervalo deve ser anterior ao final."
            );
        }

        /*
        if (
                intervaloHorarios != null
                && (
                    intervaloHorarios.isZero()
                    || intervaloHorarios.isNegative()
                )
        ) {
            throw new IllegalArgumentException(
                    "O intervalo entre horários deve ser positivo."
            );
        }
         */

        if (
                antecedenciaMinima != null
                && antecedenciaMaxima != null
                && antecedenciaMinima.compareTo(
                        antecedenciaMaxima
                ) > 0
        ) {
            throw new IllegalArgumentException(
                    "A antecedência mínima não pode ser maior que a máxima."
            );
        }
    }
}