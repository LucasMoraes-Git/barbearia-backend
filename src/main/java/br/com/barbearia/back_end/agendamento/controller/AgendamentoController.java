package br.com.barbearia.back_end.agendamento.controller;

import br.com.barbearia.back_end.agendamento.entity.Agendamento;
import br.com.barbearia.back_end.agendamento.service.AgendamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/agendamentos")
@RestController
public class AgendamentoController {

    @Autowired
    private AgendamentoService as;

    @GetMapping
    public ResponseEntity<List<Agendamento>> listarTodosAgendamentos()
    {
        var agendamentos = as.listarTodosAgendamentos();
        if(agendamentos.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Agendamento>> listarAgendamentoPorStatus(@PathVariable String status)
    {
        var agendamentos = as.listarAgendamentoPorStatus(status);
        if(agendamentos.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(agendamentos);
    }

}
