package br.com.barbearia.back_end.agendamento.service;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AgendamentoService {

    @Autowired
    private ServicoRepository servicoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConfiguracaoAgendaProperties configuracaoAgenda;


    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Autowired
    private AgendamentoMapper mapper;

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

        validarRegrasDaAgenda(inicio, fim);

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


    private LocalDateTime arredondarParaProximoMinuto(
            LocalDateTime horario
    ) {
        LocalDateTime semSegundos = horario
                .withSecond(0)
                .withNano(0);

        if (horario.equals(semSegundos)) {
            return semSegundos;
        }

        return semSegundos.plusMinutes(1);
    }


    private void validarPrecisaoDoHorario(
            LocalDateTime horario
    ) {
        if (
                horario.getSecond() != 0
                        || horario.getNano() != 0
        ) {
            throw new HorarioIndisponivelException(
                    "O horário deve ser informado com precisão de minutos."
            );
        }
    }


    private void validarRegrasDaAgenda(
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
        validarPrecisaoDoHorario(inicio);

        LocalDate data = inicio.toLocalDate();

        if (
                !configuracaoAgenda
                        .diasAtendimento()
                        .contains(data.getDayOfWeek())
        ) {
            throw new HorarioIndisponivelException(
                    "A barbearia não atende no dia informado."
            );
        }

        LocalDateTime agora = LocalDateTime.now(
                configuracaoAgenda.fusoHorario()
        );

        LocalDateTime limiteMinimo =
                arredondarParaProximoMinuto(
                        agora.plus(
                                configuracaoAgenda
                                        .antecedenciaMinima()
                        )
                );

        LocalDateTime limiteMaximo =
                agora.plus(
                                configuracaoAgenda
                                        .antecedenciaMaxima()
                        )
                        .withSecond(0)
                        .withNano(0);

        if (inicio.isBefore(limiteMinimo)) {
            throw new HorarioIndisponivelException(
                    "O agendamento não respeita a antecedência mínima."
            );
        }

        if (inicio.isAfter(limiteMaximo)) {
            throw new HorarioIndisponivelException(
                    "O agendamento ultrapassa a antecedência máxima."
            );
        }

        LocalDateTime abertura = LocalDateTime.of(
                data,
                configuracaoAgenda.horarioAbertura()
        );

        LocalDateTime fechamento = LocalDateTime.of(
                data,
                configuracaoAgenda.horarioFechamento()
        );

        if (
                inicio.isBefore(abertura)
                        || fim.isAfter(fechamento)
        ) {
            throw new HorarioIndisponivelException(
                    "O serviço deve ocorrer integralmente durante o horário de atendimento."
            );
        }

        if (configuracaoAgenda.intervaloHabilitado()) {
            LocalDateTime inicioIntervalo =
                    LocalDateTime.of(
                            data,
                            configuracaoAgenda.intervaloInicio()
                    );

            LocalDateTime fimIntervalo =
                    LocalDateTime.of(
                            data,
                            configuracaoAgenda.intervaloFim()
                    );

            boolean invadeIntervalo =
                    inicio.isBefore(fimIntervalo)
                            && fim.isAfter(inicioIntervalo);

            if (invadeIntervalo) {
                throw new HorarioIndisponivelException(
                        "O serviço entra no intervalo da barbearia."
                );
            }
        }
    }

    private List<IntervaloLivreResponse>
    filtrarIntervalosQueComportamServico(
            List<Intervalo> intervalosLivres,
            Servico servico
    ) {
        List<IntervaloLivreResponse> respostas =
                new ArrayList<>();

        LocalDateTime agora = LocalDateTime.now(
                configuracaoAgenda.fusoHorario()
        );

        LocalDateTime limiteMinimo =
                arredondarParaProximoMinuto(
                        agora.plus(
                                configuracaoAgenda
                                        .antecedenciaMinima()
                        )
                );

        LocalDateTime limiteMaximo =
                agora.plus(
                                configuracaoAgenda
                                        .antecedenciaMaxima()
                        )
                        .withSecond(0)
                        .withNano(0);

        for (Intervalo livre : intervalosLivres) {
            LocalDateTime primeiroInicio =
                    livre.inicio().isAfter(limiteMinimo)
                            ? livre.inicio()
                            : limiteMinimo;

            LocalDateTime ultimoInicio =
                    livre.fim().minusMinutes(
                            servico.getDuracaoMinutos()
                    );

            if (ultimoInicio.isAfter(limiteMaximo)) {
                ultimoInicio = limiteMaximo;
            }

            if (!primeiroInicio.isAfter(ultimoInicio)) {
                respostas.add(
                        new IntervaloLivreResponse(
                                primeiroInicio,
                                ultimoInicio,
                                livre.fim()
                        )
                );
            }
        }

        return respostas;
    }

    private List<Intervalo> calcularIntervalosLivres(
            LocalDateTime abertura,
            LocalDateTime fechamento,
            List<Intervalo> ocupados
    ) {
        List<Intervalo> livres =
                new ArrayList<>();

        LocalDateTime cursor = abertura;

        for (Intervalo ocupado : ocupados) {
            LocalDateTime inicioOcupado =
                    ocupado.inicio().isBefore(abertura)
                            ? abertura
                            : ocupado.inicio();

            LocalDateTime fimOcupado =
                    ocupado.fim().isAfter(fechamento)
                            ? fechamento
                            : ocupado.fim();

            boolean foraDoFuncionamento =
                    !fimOcupado.isAfter(abertura)
                            || !inicioOcupado.isBefore(fechamento);

            if (foraDoFuncionamento) {
                continue;
            }

            if (inicioOcupado.isAfter(cursor)) {
                livres.add(
                        new Intervalo(
                                cursor,
                                inicioOcupado
                        )
                );
            }

            if (fimOcupado.isAfter(cursor)) {
                cursor = fimOcupado;
            }

            if (!cursor.isBefore(fechamento)) {
                break;
            }
        }

        if (cursor.isBefore(fechamento)) {
            livres.add(
                    new Intervalo(
                            cursor,
                            fechamento
                    )
            );
        }

        return livres;
    }

    private List<Intervalo> criarIntervalosOcupados(
            LocalDate data,
            List<Agendamento> agendamentos
    ) {
        List<Intervalo> intervalos =
                new ArrayList<>();

        for (Agendamento agendamento : agendamentos) {
            LocalDateTime inicio =
                    agendamento.getDataServico();

            LocalDateTime fim = inicio.plusMinutes(
                    agendamento
                            .getServico()
                            .getDuracaoMinutos()
            );

            intervalos.add(
                    new Intervalo(inicio, fim)
            );
        }

        if (configuracaoAgenda.intervaloHabilitado()) {
            LocalDateTime inicioIntervalo =
                    LocalDateTime.of(
                            data,
                            configuracaoAgenda.intervaloInicio()
                    );

            LocalDateTime fimIntervalo =
                    LocalDateTime.of(
                            data,
                            configuracaoAgenda.intervaloFim()
                    );

            intervalos.add(
                    new Intervalo(
                            inicioIntervalo,
                            fimIntervalo
                    )
            );
        }

        intervalos.sort(
                Comparator.comparing(Intervalo::inicio)
        );

        return intervalos;
    }

    private DisponibilidadeAgendamentoResponse
    criarRespostaDisponibilidade(
            Servico servico,
            LocalDate data,
            List<IntervaloLivreResponse> intervalos
    ) {
        return new DisponibilidadeAgendamentoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDuracaoMinutos(),
                data,
                intervalos
        );
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

        if (
                !configuracaoAgenda
                        .diasAtendimento()
                        .contains(data.getDayOfWeek())
        ) {
            return criarRespostaDisponibilidade(
                    servico,
                    data,
                    List.of()
            );
        }

        LocalDateTime abertura = LocalDateTime.of(
                data,
                configuracaoAgenda.horarioAbertura()
        );

        LocalDateTime fechamento = LocalDateTime.of(
                data,
                configuracaoAgenda.horarioFechamento()
        );

        List<Agendamento> agendamentos =
                agendamentoRepository
                        .buscarAgendamentosNoPeriodo(
                                abertura,
                                fechamento
                        );

        List<Intervalo> intervalosOcupados =
                criarIntervalosOcupados(
                        data,
                        agendamentos
                );

        List<Intervalo> intervalosLivres =
                calcularIntervalosLivres(
                        abertura,
                        fechamento,
                        intervalosOcupados
                );

        List<IntervaloLivreResponse> disponibilidades =
                filtrarIntervalosQueComportamServico(
                        intervalosLivres,
                        servico
                );

        return criarRespostaDisponibilidade(
                servico,
                data,
                disponibilidades
        );
    }


}