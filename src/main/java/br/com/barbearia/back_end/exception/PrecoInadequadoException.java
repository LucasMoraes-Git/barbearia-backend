package br.com.barbearia.back_end.exception;

public class PrecoInadequadoException extends RuntimeException {
    public PrecoInadequadoException(String message) {
        super(message);
    }
}
