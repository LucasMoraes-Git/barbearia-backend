package br.com.barbearia.back_end.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail tratarRecursoNaoEncontrado(RecursoNaoEncontradoException exception)
    {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problema.setTitle("Recurso não encontrado");
        problema.setProperty("codigo", "RECURSO_NAO_ENCONTRADO");

        return problema;
    }

    @ExceptionHandler(PrecoInadequadoException.class)
    public ProblemDetail tratarPrecoInadequado(PrecoInadequadoException exception)
    {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problema.setTitle("Preço inadequado");
        problema.setProperty("codigo", "PRECO_INADEQUADO");

        return problema;
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ProblemDetail tratarRecursoDuplicado(RecursoDuplicadoException exception)
    {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problema.setTitle("Recurso duplicado");
        problema.setProperty("codigo", "RECURSO_DUPLICADO");

        return problema;
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ProblemDetail tratarCredenciaisInvalidas(CredenciaisInvalidasException exception)
    {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        problema.setTitle("Falha na autenticação");
        problema.setProperty("codigo", "CREDENCIAIS_INVALIDAS");

        return problema;
    }

    @ExceptionHandler(AlteracaoInvalidaException.class)
    public ProblemDetail tratarRecursoRepetido(AlteracaoInvalidaException exception)
    {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());

        problema.setTitle("Alteração inválida");
        problema.setProperty("codigo", "ALTERACAO_INVALIDA");

        return problema;
    }

    @ExceptionHandler(HorarioIndisponivelException.class)
    public ProblemDetail tratarHorarioIndisponivel(HorarioIndisponivelException exception)
    {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());

        problema.setTitle("Horário indisponível");
        problema.setProperty("codigo", "HORARIO_INDISPONIVEL");

        return problema;
    }

    @ExceptionHandler(RecursoInativoException.class)
    public ProblemDetail tratarRecursoInativo(RecursoInativoException exception)
    {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());

        problema.setTitle("Recurso está inativo");
        problema.setProperty("codigo", "RECURSO_INATIVO");
        return problema;
    }
}
