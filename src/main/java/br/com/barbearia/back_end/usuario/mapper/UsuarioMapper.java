package br.com.barbearia.back_end.usuario.mapper;

import br.com.barbearia.back_end.usuario.dto.CriarUsuarioRequest;
import br.com.barbearia.back_end.usuario.dto.UsuarioResponse;
import br.com.barbearia.back_end.usuario.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "usuarioPerfil", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    Usuario paraEntidadeUsuario(CriarUsuarioRequest request);

    UsuarioResponse paraUsuarioResponse(Usuario usuario);



}
