package br.com.barbearia.back_end.agendamento.mapper;

import br.com.barbearia.back_end.agendamento.dto.AgendamentoResponse;
import br.com.barbearia.back_end.agendamento.entity.Agendamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface AgendamentoMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioNome", source = "usuario.nome")
    @Mapping(target = "usuarioEmail", source = "usuario.email")
    @Mapping(target = "usuarioTelefone", source = "usuario.telefone")
    @Mapping(target = "servicoId", source = "servico.id")
    @Mapping(target = "servicoNome", source = "servico.nome")
    @Mapping(target = "status", source = "statusAgendamento")
    AgendamentoResponse paraAgendamentoResponse(Agendamento agendamento);


}
