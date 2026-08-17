package br.com.barbearia.back_end.servico.dto;

public record ServicoResponse(
        Long id,
        String nome,
        String descricao,
        Double preco,
        Integer duracaoMinutos,
        Boolean ativo
) {
}
