package br.com.barbearia.back_end.servico.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBL_SERVICO")
@Getter
@Setter

public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SERVICO")
    private Long id;

    @Column(name = "TX_NOME")
    private String nome;

    @Column(name = "TX_DESCRICAO")
    private String descricao;

    @Column(name = "NR_PRECO")
    private Double preco;

    @Column(name = "NR_DURACAO_MINUTOS")
    private Integer duracaoMinutos;

    @Column(name = "ATIVO")
    private Boolean ativo = true;
}
