package br.com.barbearia.back_end.servico.controller;

import br.com.barbearia.back_end.servico.dto.AtualizarServicoRequest;
import br.com.barbearia.back_end.servico.dto.CriarServicoRequest;
import br.com.barbearia.back_end.servico.dto.ServicoResponse;
import br.com.barbearia.back_end.servico.entity.Servico;
import br.com.barbearia.back_end.servico.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/servicos")
public class ServicoAdminController {

    @Autowired
    private ServicoService service;

    @GetMapping("/inativo")
    public ResponseEntity<List<ServicoResponse>> findByInativoServico()
    {
        return ResponseEntity.ok(service.findByInativoServico());
    }

    @GetMapping("/pelo-id/{id}")
    public ResponseEntity<ServicoResponse> findByIdServico(@PathVariable Long id)
    {
        return ResponseEntity.ok(service.findByIdServico(id));
    }

    @GetMapping("/com-nome/{nome}")
    public ResponseEntity<List<ServicoResponse>> findByNomeServico(@PathVariable String nome)
    {
        return ResponseEntity.ok(service.findByNomeServico(nome));
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Servico> cadastrarServico(@Valid @RequestBody CriarServicoRequest request)
    {
        Servico servico = service.cadastrarServico(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(servico);
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<ServicoResponse> atualizarServico(@Valid @RequestBody AtualizarServicoRequest request, @PathVariable Long id)
    {
        ServicoResponse servico = service.updateServico(request, id);
        return ResponseEntity.ok(servico);
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativarServico(@PathVariable Long id)
    {
        service.ativarServico(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativarServico(@PathVariable Long id)
    {
        service.desativarServico(id);
        return ResponseEntity.noContent().build();
    }
}
