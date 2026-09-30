package br.com.voupassar.exception;

/** 400 de negócio (ex.: filtro/paginação inválidos nas TASKs 3.2+). */
public class BadRequestException extends RuntimeException {

  private final String code;

  public BadRequestException(String message) {
    this("BAD_REQUEST", message);
  }

  public BadRequestException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
