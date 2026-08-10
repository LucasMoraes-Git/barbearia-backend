package br.com.barbearia.back_end.agendamento.service;

import br.com.barbearia.back_end.agendamento.entity.Agendamento;
import br.com.barbearia.back_end.agendamento.repository.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository ar;

    public List<Agendamento> listarTodosAgendamentos()
    {
        var agendamentos = ar.findAll();
        return agendamentos;
    }

    public List<Agendamento> listarAgendamentoPorStatus(String status)
    {
        var agendamentos = ar.findByStatusAgendamento(status);
        return agendamentos;
    }
}
