package br.com.barbearia.back_end.exception;

public class OperacaoInvalidaAgendamentoException extends RuntimeException {
    public OperacaoInvalidaAgendamentoException(String message) {
        super(message);
    }
}
