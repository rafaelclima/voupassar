package br.com.voupassar.exception;

/** 409 de negócio (ex.: e-mail já cadastrado na TASK 3.5). */
public class ConflictException extends RuntimeException {

  private final String code;

  public ConflictException(String message) {
    this("CONFLICT", message);
  }

  public ConflictException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
