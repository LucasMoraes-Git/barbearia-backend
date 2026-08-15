package br.com.barbearia.back_end.usuario.controller;

import br.com.barbearia.back_end.usuario.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin/usuarios")
@RestController
public class UsuarioAdminController {

    @Autowired
    private UsuarioService service;

    @PatchMapping("/{email}/ativar")
    public ResponseEntity<Void> ativarUsuario(@PathVariable String email)
    {
        service.ativarUsuarioPorEmail(email);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{email}/desativar")
    public ResponseEntity<Void> desativarUsuario(@PathVariable String email)
    {
        service.desativarUsuarioPorEmail(email);

        return ResponseEntity.noContent().build();
    }

}
