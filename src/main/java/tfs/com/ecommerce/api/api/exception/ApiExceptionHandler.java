package tfs.com.ecommerce.api.api.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tfs.com.ecommerce.api.domain.exception.EstoqueInsuficienteException;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Recurso não encontrado.");
        problemDetail.setTitle("Recurso não localizado");
        problemDetail.setType(URI.create("https://ecommerce.com/erros/recurso-nao-encontrado"));
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.");
        problemDetail.setTitle("Dados inválidos");
        problemDetail.setType(URI.create("https://ecommerce.com/erros/dados-invalidos"));

        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        problemDetail.setProperty("invalidParams", errors);
        return problemDetail;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        // Tratamento da concorrência que discutimos (Race Condition)
        log.warn("Condição de corrida evitada no banco de dados. Um usuário tentou comprar um item que já havia sido modificado.");
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "O estoque deste produto acabou de ser atualizado por outro usuário. Tente novamente.");
        problemDetail.setTitle("Conflito de Atualização (Concorrência)");
        problemDetail.setType(URI.create("https://ecommerce.com/erros/conflito-concorrencia"));
        return problemDetail;
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ProblemDetail handleEstoqueInsuficiente(EstoqueInsuficienteException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setTitle("Regra de Negócio Violada");
        problemDetail.setType(URI.create("https://ecommerce.com/erros/regra-de-negocio"));
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUncaughtException(Exception ex) {
        log.error("Erro interno não tratado pelo sistema", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno inesperado no servidor.");
        problemDetail.setTitle("Erro Interno de Servidor");
        problemDetail.setType(URI.create("https://ecommerce.com/erros/erro-interno"));
        return problemDetail;
    }
}
