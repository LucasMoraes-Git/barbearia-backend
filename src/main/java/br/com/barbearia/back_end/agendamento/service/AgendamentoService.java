package br.com.barbearia.back_end.agendamento.service;

import br.com.barbearia.back_end.agendamento.config.ConfiguracaoAgendaProperties;
import br.com.barbearia.back_end.agendamento.dto.*;
import br.com.barbearia.back_end.agendamento.entity.Agendamento;
import br.com.barbearia.back_end.agendamento.enums.StatusAgendamentoEnum;
import br.com.barbearia.back_end.agendamento.mapper.AgendamentoMapper;
import br.com.barbearia.back_end.agendamento.repository.AgendamentoRepository;
import br.com.barbearia.back_end.exception.HorarioIndisponivelException;
import br.com.barbearia.back_end.exception.OperacaoInvalidaAgendamentoException;
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

    private Agendamento buscarAgendamentoPorId(Long agendamentoId)
    {
        return agendamentoRepository.findById(agendamentoId).orElseThrow(() -> new RecursoNaoEncontradoException("Não existe um agendamento com id " + agendamentoId));
    }

    private void exigirStatus(Agendamento agendamento, StatusAgendamentoEnum statusEsperado, String operacao)
    {
        StatusAgendamentoEnum statusAtual = agendamento.getStatusAgendamento();

        if(statusEsperado != statusAtual)
        {
            throw new OperacaoInvalidaAgendamentoException("Não é possível " + operacao + " um agendamento com status " + statusAtual + ".");
        }
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

    @Transactional
    public AgendamentoResponse reagendarAgendamento(
            Long agendamentoId,
            Long usuarioId,
            ReagendarAgendamentoRequest request
    ) {
        Agendamento agendamento =
                agendamentoRepository
                        .findByIdAndUsuarioId(
                                agendamentoId,
                                usuarioId
                        )
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Agendamento não encontrado."
                                )
                        );

        if (
                !Boolean.TRUE.equals(
                        agendamento
                                .getUsuario()
                                .getAtivo()
                )
        ) {
            throw new RecursoInativoException(
                    "A conta está inativa."
            );
        }

        StatusAgendamentoEnum statusAtual =
                agendamento.getStatusAgendamento();

        boolean permiteReagendamento =
                statusAtual == StatusAgendamentoEnum.PENDENTE
                        || statusAtual == StatusAgendamentoEnum.CONFIRMADO;

        if (!permiteReagendamento) {
            throw new OperacaoInvalidaAgendamentoException(
                    "O agendamento com status "
                            + statusAtual
                            + " não pode ser reagendado."
            );
        }

        LocalDateTime agora = LocalDateTime.now(
                configuracaoAgenda.fusoHorario()
        );

        if (!agendamento.getDataServico().isAfter(agora)) {
            throw new OperacaoInvalidaAgendamentoException(
                    "Não é possível reagendar um agendamento que já começou."
            );
        }

        Servico servico = agendamento.getServico();

        if (!Boolean.TRUE.equals(servico.getAtivo())) {
            throw new RecursoInativoException(
                    "O serviço deste agendamento está inativo."
            );
        }

        LocalDateTime novoInicio =
                request.novaDataServico();

        if (
                novoInicio.equals(
                        agendamento.getDataServico()
                )
        ) {
            throw new OperacaoInvalidaAgendamentoException(
                    "O novo horário é igual ao horário atual."
            );
        }

        LocalDateTime novoFim =
                novoInicio.plusMinutes(
                        servico.getDuracaoMinutos()
                );

        disponibilidadeService.validarRegrasDaAgenda(
                novoInicio,
                novoFim
        );

        boolean existeConflito =
                agendamentoRepository
                        .existeConflitoDeHorarioExceto(
                                agendamentoId,
                                novoInicio,
                                novoFim
                        );

        if (existeConflito) {
            throw new HorarioIndisponivelException(
                    "Existe outro agendamento que ocupa o novo intervalo."
            );
        }

        agendamento.setDataServico(novoInicio);

        agendamento.setStatusAgendamento(
                StatusAgendamentoEnum.PENDENTE
        );

        return mapper.paraAgendamentoResponse(
                agendamento
        );
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

    @Transactional
    public void cancelarAgendamento(Long agendamentoId, Long usuarioId)
    {
        Agendamento agendamento = agendamentoRepository.findByIdAndUsuarioId(agendamentoId, usuarioId).orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado"));

        if(!Boolean.TRUE.equals(agendamento.getUsuario().getAtivo()))
        {
            throw new RecursoInativoException("A conta está inativa.");
        }

        StatusAgendamentoEnum status = agendamento.getStatusAgendamento();

        boolean permiteCancelamento = status == StatusAgendamentoEnum.PENDENTE || status == StatusAgendamentoEnum.CONFIRMADO;

        if(!permiteCancelamento)
        {
            throw new OperacaoInvalidaAgendamentoException("O agendamento com status " + status + " não pode ser cancelado");
        }

        LocalDateTime agora = LocalDateTime.now(
                configuracaoAgenda.fusoHorario()
        );

        if (!agendamento.getDataServico().isAfter(agora))
        {
            throw new OperacaoInvalidaAgendamentoException("Não é possível cancelar um agendamento que já começou.");
        }

        agendamento.setStatusAgendamento(StatusAgendamentoEnum.CANCELADO);
    }

    @Transactional
    public void confirmarAgendamento(Long agendamentoId)
    {
        Agendamento agendamento = buscarAgendamentoPorId(agendamentoId);

        exigirStatus(agendamento, StatusAgendamentoEnum.PENDENTE, "confirmar");

        LocalDateTime agora = LocalDateTime.now(configuracaoAgenda.fusoHorario());

        if (!agendamento.getDataServico().isAfter(agora))
        {
            throw new OperacaoInvalidaAgendamentoException("Não é possível confirmar um agendamento que já começou.");
        }

        agendamento.setStatusAgendamento(StatusAgendamentoEnum.CONFIRMADO);
    }

    @Transactional
    public void recusarAgendamento(Long agendamentoId)
    {
        Agendamento agendamento = buscarAgendamentoPorId(agendamentoId);

        exigirStatus(agendamento, StatusAgendamentoEnum.PENDENTE, "recusar");

        agendamento.setStatusAgendamento(StatusAgendamentoEnum.RECUSADO);
    }

    @Transactional
    public void concluirAgendamento(Long agendamentoId)
    {
        Agendamento agendamento = buscarAgendamentoPorId(agendamentoId);

        exigirStatus(agendamento, StatusAgendamentoEnum.CONFIRMADO, "concluir");

        LocalDateTime fimDoServico = agendamento.getDataServico().plusMinutes(agendamento.getServico().getDuracaoMinutos());

        LocalDateTime agora = LocalDateTime.now(configuracaoAgenda.fusoHorario());

        if (agora.isBefore(fimDoServico))
        {
            throw new OperacaoInvalidaAgendamentoException("O agendamento não pode ser concluído antes do término previsto.");
        }

        agendamento.setStatusAgendamento(StatusAgendamentoEnum.CONCLUIDO);
    }




}