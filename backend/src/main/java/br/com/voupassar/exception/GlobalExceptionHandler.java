package br.com.voupassar.exception;

import br.com.voupassar.common.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Tratamento global de erros (TASK 3.1).
 *
 * <p>Regras:
 * <ul>
 *   <li>Resposta sempre no envelope {@link ApiError}.</li>
 *   <li>Nunca retorna stack trace ao cliente (AGENTS.md §15).</li>
 *   <li>500 loga com stack no servidor (com traceId) e devolve
 *       mensagem genérica.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private String traceId() {
    String id = MDC.get("traceId");
    return id != null ? id : "-";
  }

  private ResponseEntity<ApiError> build(
      HttpStatus status, String code, String message, List<String> details, HttpServletRequest req) {
    ApiError body =
        new ApiError(code, message, details, traceId(), OffsetDateTime.now(), req.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), null, req);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> badRequest(BadRequestException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), null, req);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<String> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .sorted()
            .toList();
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Requisição inválida.", details, req);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> constraint(ConstraintViolationException ex, HttpServletRequest req) {
    List<String> details =
        ex.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .sorted()
            .toList();
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Requisição inválida.", details, req);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ApiError> malformed(Exception ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Corpo ou parâmetro inválido.", null, req);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiError> noResource(NoResourceFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "Recurso não encontrado.", null, req);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> forbidden(AccessDeniedException ex, HttpServletRequest req) {
    return build(HttpStatus.FORBIDDEN, "FORBIDDEN", "Acesso negado.", null, req);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest req) {
    // Stack fica no log do servidor, com traceId — nunca no JSON.
    log.error("Erro inesperado traceId={} path={}", traceId(), req.getRequestURI(), ex);
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "Erro interno. Tente novamente.",
        null,
        req);
  }
}
