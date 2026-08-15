package br.com.barbearia.back_end.usuario.controller;

import br.com.barbearia.back_end.usuario.dto.*;
import br.com.barbearia.back_end.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/usuarios")
@RestController
public class UsuarioController {


    @Autowired
    private UsuarioService service;

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrarUsuario(@Valid @RequestBody CriarUsuarioRequest request)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrarUsuario(request));
    }

    @PatchMapping("/me/novo-nome")
    public ResponseEntity<Void> atualizarNome(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AlterarNomeUsuarioRequest request)
    {
        Long usuarioId = Long.valueOf(jwt.getClaimAsString("usuarioId"));

        service.atualizarNome(request, usuarioId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/novo-telefone")
    public ResponseEntity<Void> atualizarTelefone(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AlterarTelefoneUsuarioRequest request)
    {
        Long usuarioId = Long.valueOf(jwt.getClaimAsString("usuarioId"));

        service.atualizarTelefone(request, usuarioId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/nova-senha")
    public ResponseEntity<Void> atualizarSenha(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AlterarSenhaUsuarioRequest request)
    {
        Long usuarioId = Long.valueOf(jwt.getClaimAsString("usuarioId"));

        service.atualizarSenha(request, usuarioId);

        return ResponseEntity.noContent().build();
    }



}
