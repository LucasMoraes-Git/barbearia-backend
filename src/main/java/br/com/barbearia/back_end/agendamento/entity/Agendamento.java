package br.com.barbearia.back_end.agendamento.entity;

import br.com.barbearia.back_end.agendamento.enums.StatusAgendamentoEnum;
import br.com.barbearia.back_end.servico.entity.Servico;
import br.com.barbearia.back_end.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_AGENDAMENTO")
@Getter
@Setter

public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_AGENDAMENTO")
    private Long id;

    @JoinColumn(name = "ID_USUARIO")
    @ManyToOne
    private Usuario usuario;

    @JoinColumn(name = "ID_SERVICO")
    @ManyToOne
    private Servico servico;

    @Column(name = "DT_DATA_CRIADO")
    private LocalDateTime dataCriado;

    @Column(name = "DT_DATA_SERVICO")
    private LocalDateTime dataServico;

    @Enumerated(EnumType.STRING)
    @Column(name = "TX_STATUS_AGENDAMENTO")
    private StatusAgendamentoEnum statusAgendamento;

    @Column(name = "TX_OBSERVACAO")
    private String observacao;
}
