package br.com.voupassar.exception;

/** 401 de negócio (ex.: credenciais inválidas, refresh inválido/reusado na TASK 3.5). */
public class UnauthorizedException extends RuntimeException {

  private final String code;

  public UnauthorizedException(String message) {
    this("UNAUTHORIZED", message);
  }

  public UnauthorizedException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
