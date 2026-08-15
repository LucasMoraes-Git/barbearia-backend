package br.com.barbearia.back_end.agendamento.service;

import br.com.barbearia.back_end.agendamento.dto.AgendamentoResponse;
import br.com.barbearia.back_end.agendamento.dto.CriarAgendamentoRequest;
import br.com.barbearia.back_end.agendamento.entity.Agendamento;
import br.com.barbearia.back_end.agendamento.enums.StatusAgendamentoEnum;
import br.com.barbearia.back_end.agendamento.mapper.AgendamentoMapper;
import br.com.barbearia.back_end.agendamento.repository.AgendamentoRepository;
import br.com.barbearia.back_end.exception.HorarioIndisponivelException;
import br.com.barbearia.back_end.exception.RecursoInativoException;
import br.com.barbearia.back_end.exception.RecursoNaoEncontradoException;
import br.com.barbearia.back_end.servico.entity.Servico;
import br.com.barbearia.back_end.servico.repository.ServicoRepository;
import br.com.barbearia.back_end.usuario.entity.Usuario;
import br.com.barbearia.back_end.usuario.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Autowired
    private ServicoRepository servicoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AgendamentoMapper mapper;

    public List<AgendamentoResponse> listarTodosAgendamentos()
    {
        var agendamentos = agendamentoRepository.findAll();

        return agendamentos.stream().map(mapper::paraAgendamentoResponse).toList();
    }

    public List<AgendamentoResponse> listarAgendamentoPorStatus(StatusAgendamentoEnum status)
    {
        var agendamentos = agendamentoRepository.findByStatusAgendamento(status);
        return agendamentos.stream().map(mapper::paraAgendamentoResponse).toList();
    }

    @Transactional
    public AgendamentoResponse cadastrarAgendamento(Long usuarioId, CriarAgendamentoRequest request)
    {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        if(!Boolean.TRUE.equals(usuario.getAtivo()))
        {
            throw new RecursoInativoException("Não é possível agendar com a conta inativa.");
        }

        Servico servico = servicoRepository.findById(request.servicoId()).orElseThrow(() -> new RecursoNaoEncontradoException("Serviço desejado não encontrado"));

        if(!Boolean.TRUE.equals(servico.getAtivo()))
        {
            throw new RecursoInativoException("Não é possível agendar um serviço inativo.");
        }

        LocalDateTime inicio = request.dataServico();

        LocalDateTime fim = inicio.plusMinutes(servico.getDuracaoMinutos());

        boolean existeConflitoHorario = agendamentoRepository.existeConflitoDeHorario(inicio, fim);

        if(existeConflitoHorario)
        {
            throw new HorarioIndisponivelException("Existe um agendamento marcado para este horário.");
        }

        Agendamento agendamento = new Agendamento();

        agendamento.setUsuario(usuario);
        agendamento.setServico(servico);
        agendamento.setDataCriado(LocalDateTime.now());
        agendamento.setDataServico(inicio);
        agendamento.setStatusAgendamento(StatusAgendamentoEnum.PENDENTE);

        String observacao = request.observacao();

        if(observacao != null)
        {
            observacao = observacao.trim();

            if(observacao.isBlank())
            {
                observacao = null;
            }
        }

        agendamento.setObservacao(observacao);

        Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);

        return mapper.paraAgendamentoResponse(agendamentoSalvo);
    }
}
