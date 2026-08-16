package br.com.barbearia.back_end.agendamento.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import jakarta.validation.Valid;

@Validated
@ConfigurationProperties(prefix = "barbearia.agenda")
public record ConfiguracaoAgendaProperties(

        @NotNull
        ZoneId fusoHorario,

        @Valid
        @NotNull
        HorarioFuncionamento diasUteis,

        @Valid
        @NotNull
        HorarioFuncionamento fimDeSemana,

        boolean intervaloHabilitado,

        @NotNull
        LocalTime intervaloInicio,

        @NotNull
        LocalTime intervaloFim,

        @NotNull
        Duration antecedenciaMinima,

        @NotNull
        Duration antecedenciaMaxima
) {

    public ConfiguracaoAgendaProperties {
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

        if (
                antecedenciaMinima != null
                        && antecedenciaMinima.isNegative()
        ) {
            throw new IllegalArgumentException(
                    "A antecedência mínima não pode ser negativa."
            );
        }

        if (
                antecedenciaMaxima != null
                        && (
                        antecedenciaMaxima.isNegative()
                                || antecedenciaMaxima.isZero()
                )
        ) {
            throw new IllegalArgumentException(
                    "A antecedência máxima deve ser positiva."
            );
        }

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

    public HorarioFuncionamento horarioPara(
            DayOfWeek dia
    ) {
        return switch (dia) {
            case MONDAY,
                 TUESDAY,
                 WEDNESDAY,
                 THURSDAY,
                 FRIDAY -> diasUteis;

            case SATURDAY,
                 SUNDAY -> fimDeSemana;
        };
    }

    public record HorarioFuncionamento(

            @NotNull
            LocalTime abertura,

            @NotNull
            LocalTime fechamento
    ) {
        public HorarioFuncionamento {
            if (
                    abertura != null
                            && fechamento != null
                            && !abertura.isBefore(fechamento)
            ) {
                throw new IllegalArgumentException(
                        "A abertura deve ser anterior ao fechamento."
                );
            }
        }
    }
}