package br.com.barbearia.back_end.exception;

public class HorarioIndisponivelException extends RuntimeException {
    public HorarioIndisponivelException(String message) {
        super(message);
    }
}
