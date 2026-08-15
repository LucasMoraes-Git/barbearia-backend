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

@RequestMapping("/servicos")
@RestController
public class ServicoController {

    @Autowired
    private ServicoService service;


    @GetMapping("/pelo-id/{id}")
    public ResponseEntity<ServicoResponse> findByIdServico(@PathVariable Long id)
    {
        return ResponseEntity.ok(service.findByIdServico(id));
    }

    @GetMapping("/preco-abaixo-de/{preco}")
    public ResponseEntity<List<ServicoResponse>> findByPrecoServico(@PathVariable Double preco)
    {
        return ResponseEntity.ok(service.findByPrecoServico(preco));
    }

    @GetMapping("/preco-entre/{precoMenor}/{precoMaior}")
    public ResponseEntity<List<ServicoResponse>> findByPrecoBetweenPrecoMenorPrecoMaior(@PathVariable Double precoMenor, @PathVariable Double precoMaior)
    {
        return ResponseEntity.ok(service.findByPrecoBetweenPrecoMenorPrecoMaior(precoMenor, precoMaior));
    }

    @GetMapping("/ativo")
    public ResponseEntity<List<ServicoResponse>> findByAtivoServico()
    {
        return ResponseEntity.ok(service.findByAtivoServico());
    }

    @GetMapping("/com-nome/{nome}")
    public ResponseEntity<List<ServicoResponse>> findByNomeServico(@PathVariable String nome)
    {
        return ResponseEntity.ok(service.findByNomeServico(nome));
    }





}
