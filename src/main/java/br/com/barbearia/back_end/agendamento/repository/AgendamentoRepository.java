package br.com.barbearia.back_end.agendamento.repository;

import br.com.barbearia.back_end.agendamento.entity.Agendamento;
import br.com.barbearia.back_end.agendamento.enums.StatusAgendamentoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByStatusAgendamento(StatusAgendamentoEnum status);

    @Query(
            value = """
                SELECT CASE
                    WHEN EXISTS (
                        SELECT 1
                        FROM TBL_AGENDAMENTO a
                        INNER JOIN TBL_SERVICO s
                            ON s.ID_SERVICO = a.ID_SERVICO
                        WHERE a.TX_STATUS_AGENDAMENTO
                            IN ('PENDENTE', 'CONFIRMADO')
                          AND a.DT_DATA_SERVICO < :novoFim
                          AND DATEADD(
                                MINUTE,
                                s.NR_DURACAO_MINUTOS,
                                a.DT_DATA_SERVICO
                              ) > :novoInicio
                    )
                    THEN CAST(1 AS BIT)
                    ELSE CAST(0 AS BIT)
                END
                """,
            nativeQuery = true
    )
    boolean existeConflitoDeHorario(
            @Param("novoInicio") LocalDateTime novoInicio,
            @Param("novoFim") LocalDateTime novoFim
    );

}
