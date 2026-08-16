package br.com.barbearia.back_end.agendamento.service;

import br.com.barbearia.back_end.agendamento.config.ConfiguracaoAgendaProperties;
import br.com.barbearia.back_end.agendamento.dto.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoService {

    @Autowired
    private ServicoRepository servicoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Autowired
    private AgendamentoMapper mapper;
    @Autowired
    private DisponibilidadeAgendaService disponibilidadeService;
    @Autowired
    private ConfiguracaoAgendaProperties configuracaoAgenda;

    private record Intervalo(
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
    }

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
        LocalDateTime fim = inicio.plusMinutes(
                servico.getDuracaoMinutos()
        );

        disponibilidadeService.validarRegrasDaAgenda(inicio, fim);

        boolean existeConflitoHorario =
                agendamentoRepository
                        .existeConflitoDeHorario(
                                inicio,
                                fim
                        );

        if(existeConflitoHorario)
        {
            throw new HorarioIndisponivelException("Existe um agendamento marcado para este horário.");
        }

        Agendamento agendamento = new Agendamento();

        agendamento.setUsuario(usuario);
        agendamento.setServico(servico);
        agendamento.setDataCriado(LocalDateTime.now(configuracaoAgenda.fusoHorario()));
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

    public List<AgendamentoResponse> buscarAgendamentosIdUsuario(Long idUsuario)
    {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new RecursoNaoEncontradoException("Não há usuário com id " + idUsuario + "."));

        if(!Boolean.TRUE.equals(usuario.getAtivo()))
        {
            throw new RecursoInativoException("A conta está inativa.");
        }

        List<Agendamento> agendamentos = agendamentoRepository.findByUsuarioIdOrderByDataServicoDesc(idUsuario);

        return agendamentos.stream().map(mapper::paraAgendamentoResponse).toList();
    }

    @Transactional
    public DisponibilidadeAgendamentoResponse
    buscarDisponibilidade(
            LocalDate data,
            Long servicoId
    ) {
        Servico servico = servicoRepository
                .findByIdAndAtivoTrue(servicoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Serviço ativo não encontrado."
                        )
                );


        DisponibilidadeAgendaService.Intervalo funcionamento =
                disponibilidadeService.obterHorarioDeFuncionamento(data);

        LocalDateTime abertura =
                funcionamento.inicio();

        LocalDateTime fechamento =
                funcionamento.fim();

        List<Agendamento> agendamentos =
                agendamentoRepository
                        .buscarAgendamentosNoPeriodo(
                                abertura,
                                fechamento
                        );

        List<DisponibilidadeAgendaService.Intervalo> intervalosOcupados =
                disponibilidadeService.criarIntervalosOcupados(
                        data,
                        agendamentos
                );

        List<DisponibilidadeAgendaService.Intervalo> intervalosLivres =
                disponibilidadeService.calcularIntervalosLivres(
                        abertura,
                        fechamento,
                        intervalosOcupados
                );

        List<IntervaloLivreResponse> disponibilidades =
                disponibilidadeService.filtrarIntervalosQueComportamServico(
                        intervalosLivres,
                        servico
                );

        return disponibilidadeService.criarRespostaDisponibilidade(
                servico,
                data,
                disponibilidades
        );
    }

}