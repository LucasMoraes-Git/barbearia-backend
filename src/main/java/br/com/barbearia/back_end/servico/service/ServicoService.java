package br.com.barbearia.back_end.servico.service;

import br.com.barbearia.back_end.exception.PrecoInadequadoException;
import br.com.barbearia.back_end.exception.RecursoNaoEncontradoException;
import br.com.barbearia.back_end.servico.dto.AtualizarServicoRequest;
import br.com.barbearia.back_end.servico.dto.CriarServicoRequest;
import br.com.barbearia.back_end.servico.dto.ServicoResponse;
import br.com.barbearia.back_end.servico.entity.Servico;
import br.com.barbearia.back_end.servico.mapper.ServicoMapper;
import br.com.barbearia.back_end.servico.repository.ServicoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicoService {

    @Autowired
    private ServicoRepository repository;

    @Autowired
    private ServicoMapper mapper;

    public ServicoResponse cadastrarServico(CriarServicoRequest request)
    {
        Servico servico = mapper.criarParaEntidade(request);
        Servico servicoCriado = repository.save(servico);
        return mapper.servicoResponseDTO(servicoCriado);
    }

    public ServicoResponse findByIdServico(Long id)
    {
        var servico = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Serviço com ID " + id + " não encontrado"));
        return mapper.servicoResponseDTO(servico);
    }

    public ServicoResponse findByIdAndAtivoTrue(Long id)
    {
        var servico = repository.findByIdAndAtivoTrue(id).orElseThrow(() -> new RecursoNaoEncontradoException("Serviço com ID " + id + " não encontrado"));
        return mapper.servicoResponseDTO(servico);
    }

    public List<ServicoResponse> findByPrecoServico(Double preco)
    {
        if (preco < 0)
        {
            throw new PrecoInadequadoException("Preço " + preco + " inserido é inadequado");
        }

        var servicos = repository.findByPrecoServicoAtivo(preco);

        if (servicos.isEmpty())
        {
            throw new RecursoNaoEncontradoException("Serviços com preço abaixo de " + preco + " não foram encontrados.");
        }
        return servicos.stream().map(servico -> mapper.servicoResponseDTO(servico)).toList();
    }

    public List<ServicoResponse> findByPrecoBetweenPrecoMenorPrecoMaior(Double precoMenor, Double precoMaior)
    {
        if (precoMenor < 0) {
            throw new PrecoInadequadoException(
                    "Preço mínimo " + precoMenor + " deve ser maior ou igual a zero."
            );
        }

        if (precoMaior < 0) {
            throw new PrecoInadequadoException(
                    "Preço máximo " + precoMaior + " deve ser maior ou igual a zero."
            );
        }

        if (precoMenor > precoMaior) {
            throw new PrecoInadequadoException(
                    "O preço mínimo não pode ser maior que o preço máximo."
            );
        }

        var servicos = repository.findByPrecoBetweenPrecoMenorPrecoMaior(precoMenor, precoMaior);

        if (servicos.isEmpty())
        {
            throw new RecursoNaoEncontradoException("Serviços neste intervalo de preço não foram encontrados.");
        }
        return servicos.stream().map(servico -> mapper.servicoResponseDTO(servico)).toList();
    }

    public List<ServicoResponse> findByAtivoServico()
    {
        var servicos = repository.findByAtivoServico();

        if (servicos.isEmpty())
        {
            throw new RecursoNaoEncontradoException("Serviços ativos não foram encontrados.");
        }
        return servicos.stream().map(mapper::servicoResponseDTO).toList();
    }

    public List<ServicoResponse> findByInativoServico()
    {
        var servicos = repository.findByInativoServico();

        if (servicos.isEmpty())
        {
            throw new RecursoNaoEncontradoException("Serviços inativos não foram encontrados.");
        }
        return servicos.stream().map(mapper::servicoResponseDTO).toList();
    }

    public List<ServicoResponse> findByNomeServico(String nome)
    {
        var servicos = repository.findByNomeServico(nome);

        if(servicos.isEmpty())
        {
            throw new RecursoNaoEncontradoException("Serviços com o nome " + nome + " não foram encontrados.");
        }

        return servicos.stream().map(mapper::servicoResponseDTO).toList();
    }

    public List<ServicoResponse> findByNomeServicoAtivo(String nome)
    {
        var servicos = repository.findByNomeServicoAtivo(nome);

        if(servicos.isEmpty())
        {
            throw new RecursoNaoEncontradoException("Serviços ativos com o nome " + nome + " não foram encontrados.");
        }

        return servicos.stream().map(mapper::servicoResponseDTO).toList();
    }

    @Transactional
    public ServicoResponse updateServico(AtualizarServicoRequest servicoUpdate, Long id)
    {
        var servico = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Serviço com ID " + id + " não encontrado"));
        mapper.atualizarParaEntidade(servicoUpdate, servico);
        return mapper.servicoResponseDTO(servico);
    }

    @Transactional
    public void ativarServico(Long id)
    {
        var servico = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Serviço com ID " + id + " não encontrado"));
        servico.setAtivo(true);
    }

    @Transactional
    public void desativarServico(Long id)
    {
        var servico = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Serviço com ID " + id + " não encontrado"));
        servico.setAtivo(false);
    }

}

