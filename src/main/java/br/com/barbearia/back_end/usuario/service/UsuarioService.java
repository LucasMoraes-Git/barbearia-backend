package br.com.barbearia.back_end.usuario.service;

import br.com.barbearia.back_end.exception.RecursoDuplicadoException;
import br.com.barbearia.back_end.usuario.dto.CriarUsuarioRequest;
import br.com.barbearia.back_end.usuario.dto.UsuarioResponse;
import br.com.barbearia.back_end.usuario.entity.Usuario;
import br.com.barbearia.back_end.usuario.enums.UsuarioPerfilEnum;
import br.com.barbearia.back_end.usuario.mapper.UsuarioMapper;
import br.com.barbearia.back_end.usuario.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private UsuarioMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse cadastrarUsuario(CriarUsuarioRequest request)
    {
        String emailFormatado = request.email().toLowerCase(Locale.ROOT).trim();
        String telefoneFormatado = request.telefone().trim();
        verificarDuplicidade(emailFormatado, telefoneFormatado);

        Usuario usuario = mapper.paraEntidadeUsuario(request);

        usuario.setNome(request.nome().trim().toUpperCase(Locale.ROOT));
        usuario.setEmail(emailFormatado);
        usuario.setTelefone(telefoneFormatado);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setUsuarioPerfil(UsuarioPerfilEnum.CLIENTE);
        usuario.setAtivo(true);

        Usuario usuarioSalvo = repository.save(usuario);

        return mapper.paraUsuarioResponse(usuarioSalvo);
    }

    public void verificarDuplicidade(String email, String telefone)
    {
        if (repository.existsByEmailIgnoreCase(email))
        {
            throw new RecursoDuplicadoException("Existe uma conta com este e-mail.");
        }
        if(repository.existsByTelefone(telefone))
        {
            throw new RecursoDuplicadoException("Existe uma conta com este telefone.");
        }
    }



}
