package br.com.barbearia.back_end.usuario.service;

import br.com.barbearia.back_end.exception.AlteracaoInvalidaException;
import br.com.barbearia.back_end.exception.RecursoDuplicadoException;
import br.com.barbearia.back_end.exception.RecursoNaoEncontradoException;
import br.com.barbearia.back_end.usuario.dto.*;
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

    public UsuarioResponse buscarMeuPerfil(Long id)
    {
        Usuario usuario = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário com id " + id + " não encontrado."));
        UsuarioResponse response = mapper.paraUsuarioResponse(usuario);
        if(!Boolean.TRUE.equals(response.ativo()))
        {
            throw new RecursoNaoEncontradoException("Usuário com id " + id + " foi desativado.");
        }
        return response;
    }

    @Transactional
    public void ativarUsuarioPorEmail(String email)
    {
        Usuario usuario = repository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT)).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário com este e-mail não encontrado"));
        usuario.setAtivo(true);
    }

    @Transactional
    public void desativarUsuarioPorEmail(String email)
    {
        Usuario usuario = repository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT)).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário com este e-mail não encontrado"));
        usuario.setAtivo(false);
    }

    @Transactional
    public void atualizarNome(AlterarNomeUsuarioRequest request, Long id)
    {
        Usuario usuario = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário com este id não encontrado"));
        if(usuario.getNome().equals(request.nome()))
        {
            throw new AlteracaoInvalidaException("O novo nome inserido é o mesmo do antigo");
        }
        usuario.setNome(request.nome().trim().toUpperCase(Locale.ROOT));
    }

    @Transactional
    public void atualizarTelefone(AlterarTelefoneUsuarioRequest request, Long id)
    {
        Usuario usuario = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário com este id não encontrado"));

        if(usuario.getTelefone().equals(request.telefone()))
        {
            throw new AlteracaoInvalidaException("O novo telefone inserido é o mesmo do antigo");
        }

        if(repository.existsByTelefone(request.telefone()))
        {
            throw new AlteracaoInvalidaException("Este número pertence a outro usuário");
        }

        usuario.setTelefone(request.telefone());
    }

    @Transactional
    public void atualizarSenha(AlterarSenhaUsuarioRequest request, Long id)
    {
        Usuario usuario = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário com este id não encontrado"));

        if(!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha()))
        {
            throw new AlteracaoInvalidaException("A senha atual fornecida é incorreta");
        }

        if(passwordEncoder.matches(request.novaSenha(), usuario.getSenha()))
        {
            throw new AlteracaoInvalidaException("A senha nova é igual a antiga");
        }

        if(!request.novaSenha().equals(request.confirmacaoNovaSenha()))
        {
            throw new AlteracaoInvalidaException("A senha de confirmação é diferente da nova senha");
        }

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
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
