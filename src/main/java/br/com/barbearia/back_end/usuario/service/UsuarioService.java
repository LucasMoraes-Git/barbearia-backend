package br.com.barbearia.back_end.usuario.service;

import br.com.barbearia.back_end.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository ur;



}
