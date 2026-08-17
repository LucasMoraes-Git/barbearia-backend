package br.com.barbearia.back_end.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @ExceptionHandler(OperacaoInvalidaAgendamentoException.class)
    public ProblemDetail tratarOperacaoInvalida(OperacaoInvalidaAgendamentoException exception)
    {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problema.setTitle("Operação de agendamento inválida");
        problema.setProperty("codigo", "OPERACAO_AGENDAMENTO_INVALIDA");

        return problema;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarDadosInvalidos(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> erros =
                new LinkedHashMap<>();

        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(erro ->
                        erros.putIfAbsent(
                                erro.getField(),
                                erro.getDefaultMessage()
                        )
                );

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "Um ou mais campos estão inválidos."
                );

        problema.setTitle("Dados inválidos");
        problema.setProperty("codigo", "DADOS_INVALIDOS");
        problema.setProperty("erros", erros);

        return problema;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail tratarParametroAusente(
            MissingServletRequestParameterException exception
    ) {
        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "O parâmetro '"
                                + exception.getParameterName()
                                + "' é obrigatório."
                );

        problema.setTitle("Parâmetro obrigatório ausente");
        problema.setProperty(
                "codigo",
                "PARAMETRO_OBRIGATORIO_AUSENTE"
        );

        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail tratarCorpoInvalido(
            HttpMessageNotReadableException exception
    ) {
        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "O corpo da requisição está ausente ou possui um JSON inválido."
                );

        problema.setTitle("Corpo da requisição inválido");
        problema.setProperty(
                "codigo",
                "CORPO_REQUISICAO_INVALIDO"
        );

        return problema;
    }

}
