package br.com.barbearia.back_end.agendamento.service;

import br.com.barbearia.back_end.agendamento.config.ConfiguracaoAgendaProperties;
import br.com.barbearia.back_end.agendamento.dto.DisponibilidadeAgendamentoResponse;
import br.com.barbearia.back_end.agendamento.dto.IntervaloLivreResponse;
import br.com.barbearia.back_end.agendamento.entity.Agendamento;
import br.com.barbearia.back_end.agendamento.mapper.AgendamentoMapper;
import br.com.barbearia.back_end.agendamento.repository.AgendamentoRepository;
import br.com.barbearia.back_end.exception.HorarioIndisponivelException;
import br.com.barbearia.back_end.exception.RecursoNaoEncontradoException;
import br.com.barbearia.back_end.servico.entity.Servico;
import br.com.barbearia.back_end.servico.repository.ServicoRepository;
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
public class DisponibilidadeAgendaService {

    @Autowired
    protected ServicoRepository servicoRepository;
    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    protected ConfiguracaoAgendaProperties configuracaoAgenda;


    @Autowired
    protected AgendamentoRepository agendamentoRepository;
    @Autowired
    protected AgendamentoMapper mapper;

    protected record Intervalo(
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
    }


    protected Intervalo obterHorarioDeFuncionamento(
            LocalDate data
    ) {
        ConfiguracaoAgendaProperties.HorarioFuncionamento horario =
                configuracaoAgenda.horarioPara(
                        data.getDayOfWeek()
                );

        LocalDateTime abertura = LocalDateTime.of(
                data,
                horario.abertura()
        );

        LocalDateTime fechamento = LocalDateTime.of(
                data,
                horario.fechamento()
        );

        return new Intervalo(
                abertura,
                fechamento
        );
    }


    protected LocalDateTime arredondarParaProximoMinuto(
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


    protected void validarPrecisaoDoHorario(
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


    protected void validarRegrasDaAgenda(
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
        validarPrecisaoDoHorario(inicio);

        LocalDate data = inicio.toLocalDate();

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

        Intervalo funcionamento = obterHorarioDeFuncionamento(data);

        LocalDateTime abertura = funcionamento.inicio();

        LocalDateTime fechamento = funcionamento.fim();

        if (inicio.isBefore(abertura) || fim.isAfter(fechamento)) {
            throw new HorarioIndisponivelException("O serviço deve ocorrer integralmente durante o horário de atendimento.");
        }

        if (configuracaoAgenda.intervaloHabilitado()) {
            LocalDateTime inicioIntervalo = LocalDateTime.of(data, configuracaoAgenda.intervaloInicio());

            LocalDateTime fimIntervalo = LocalDateTime.of(data, configuracaoAgenda.intervaloFim());

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

    protected List<IntervaloLivreResponse>
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

    protected List<Intervalo> calcularIntervalosLivres(
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

    protected List<Intervalo> criarIntervalosOcupados(
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

    protected DisponibilidadeAgendamentoResponse
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
}
