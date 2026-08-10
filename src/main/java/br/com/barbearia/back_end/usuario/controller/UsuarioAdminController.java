package br.com.barbearia.back_end.usuario.controller;

import br.com.barbearia.back_end.usuario.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin/usuarios")
@RestController
public class UsuarioAdminController {

    @Autowired
    private UsuarioService us;
}
