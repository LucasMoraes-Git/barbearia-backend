package br.com.barbearia.back_end.auth.service;

import br.com.barbearia.back_end.auth.dto.LoginRequest;
import br.com.barbearia.back_end.auth.dto.LoginResponse;
import br.com.barbearia.back_end.exception.CredenciaisInvalidasException;
import br.com.barbearia.back_end.security.TokenService;
import br.com.barbearia.back_end.usuario.entity.Usuario;
import br.com.barbearia.back_end.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request)
    {
        String emailFormatado = request.email().trim().toLowerCase(Locale.ROOT);

        Usuario usuario = repository.findByEmailIgnoreCase(emailFormatado).orElseThrow(this::credenciaisInvalidas);

        if(!Boolean.TRUE.equals(usuario.getAtivo()))
        {
            throw credenciaisInvalidas();
        }

        boolean senhaCorreta = passwordEncoder.matches(request.senha(), usuario.getSenha());

        if(!senhaCorreta)
        {
            throw credenciaisInvalidas();
        }

        String token = tokenService.gerarToken(usuario);

        return new LoginResponse(token, "Bearer", tokenService.getExpiracaoSegundos());


    }

    private CredenciaisInvalidasException credenciaisInvalidas()
    {
        return new CredenciaisInvalidasException("E-mail ou senha inválidos");
    }
}
