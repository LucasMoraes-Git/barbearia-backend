package br.com.barbearia.back_end.agendamento.controller;

import br.com.barbearia.back_end.agendamento.dto.AgendamentoResponse;
import br.com.barbearia.back_end.agendamento.dto.CriarAgendamentoRequest;
import br.com.barbearia.back_end.agendamento.dto.DisponibilidadeAgendamentoResponse;
import br.com.barbearia.back_end.agendamento.entity.Agendamento;
import br.com.barbearia.back_end.agendamento.enums.StatusAgendamentoEnum;
import br.com.barbearia.back_end.agendamento.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/agendamentos")
@RestController
public class AgendamentoController {

    @Autowired
    private AgendamentoService service;

    @PostMapping
    public ResponseEntity<AgendamentoResponse> cadastrarAgendamento(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CriarAgendamentoRequest request)
    {
        Long usuarioId = Long.valueOf(jwt.getClaimAsString("usuarioId"));

        AgendamentoResponse response = service.cadastrarAgendamento(usuarioId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<AgendamentoResponse>> buscarAgendamentosIdUsuario(@AuthenticationPrincipal Jwt jwt)
    {
        Long usuarioId = Long.valueOf(jwt.getClaimAsString("usuarioId"));

        List<AgendamentoResponse> agendamentos = service.buscarAgendamentosIdUsuario(usuarioId);

        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/disponibilidade")
    public ResponseEntity<DisponibilidadeAgendamentoResponse> buscarDisponibilidade(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data, @RequestParam Long servicoId)
    {
        DisponibilidadeAgendamentoResponse response = service.buscarDisponibilidade(data, servicoId);

        return ResponseEntity.ok(response);
    }

}
