package br.com.barbearia.back_end.usuario.dto;

import br.com.barbearia.back_end.usuario.enums.UsuarioPerfilEnum;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        UsuarioPerfilEnum usuarioPerfil,
        Boolean ativo
) {
}
