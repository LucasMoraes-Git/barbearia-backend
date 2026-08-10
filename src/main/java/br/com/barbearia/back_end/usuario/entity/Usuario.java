package br.com.barbearia.back_end.usuario.entity;

import br.com.barbearia.back_end.usuario.enums.UsuarioPerfilEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBL_USUARIO")
@Getter
@Setter

public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private Long id;

    @Column(name = "TX_NOME")
    private String nome;

    @Column(name = "TX_EMAIL")
    private String email;

    @Column(name = "TX_TELEFONE")
    private String telefone;

    @Column(name = "TX_SENHA")
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "TX_USUARIO_PERFIL")
    private UsuarioPerfilEnum usuarioPerfil;

}
