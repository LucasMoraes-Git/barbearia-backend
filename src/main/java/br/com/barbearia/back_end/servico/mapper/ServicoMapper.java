package br.com.barbearia.back_end.servico.mapper;

import br.com.barbearia.back_end.servico.dto.AtualizarServicoRequest;
import br.com.barbearia.back_end.servico.dto.CriarServicoRequest;
import br.com.barbearia.back_end.servico.dto.ServicoResponse;
import br.com.barbearia.back_end.servico.entity.Servico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")

public interface ServicoMapper {

    ServicoResponse servicoResponseDTO(Servico response);

    Servico criarParaEntidade(CriarServicoRequest request);

    @Mapping(target = "id", ignore = true)
    void atualizarParaEntidade(AtualizarServicoRequest request, @MappingTarget Servico servico);

}
