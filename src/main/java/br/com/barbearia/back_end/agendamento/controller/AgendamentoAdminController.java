package br.com.barbearia.back_end.agendamento.controller;

import br.com.barbearia.back_end.agendamento.dto.AgendamentoResponse;
import br.com.barbearia.back_end.agendamento.enums.StatusAgendamentoEnum;
import br.com.barbearia.back_end.agendamento.service.AgendamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/admin/agendamentos")
@RestController

public class AgendamentoAdminController {

    @Autowired
    private AgendamentoService service;

    @GetMapping
    public ResponseEntity<List<AgendamentoResponse>> listarTodosAgendamentos()
    {
        return ResponseEntity.ok(service.listarTodosAgendamentos());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AgendamentoResponse>> listarAgendamentoPorStatus(@PathVariable StatusAgendamentoEnum status)
    {
        return ResponseEntity.ok(service.listarAgendamentoPorStatus(status));
    }
}
