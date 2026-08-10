package br.com.barbearia.back_end.agendamento.repository;

import br.com.barbearia.back_end.agendamento.entity.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query(value = "SELECT * FROM TBL_AGENDAMENTO a WHERE a.TX_STATUS_AGENDAMENTO = :status", nativeQuery = true)
    List<Agendamento> findByStatusAgendamento(@Param("status") String status);

}
