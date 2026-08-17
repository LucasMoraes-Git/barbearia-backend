package br.com.barbearia.back_end.agendamento.controller;

import br.com.barbearia.back_end.agendamento.dto.AgendamentoResponse;
import br.com.barbearia.back_end.agendamento.enums.StatusAgendamentoEnum;
import br.com.barbearia.back_end.agendamento.service.AgendamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{id}/recusar")
    public ResponseEntity<Void> recusarAgendamento(@PathVariable Long id)
    {
        service.recusarAgendamento(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<Void> confirmarAgendamento(@PathVariable Long id)
    {
        service.confirmarAgendamento(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<Void> concluirAgendamento(@PathVariable Long id)
    {
        service.concluirAgendamento(id);
        return ResponseEntity.noContent().build();
    }

}
