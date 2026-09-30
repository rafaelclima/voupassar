package br.com.voupassar.exception;

/** 404 de negócio (ex.: edição/disciplina inexistente nas TASKs 3.2+). */
public class ResourceNotFoundException extends RuntimeException {

  private final String code;

  public ResourceNotFoundException(String message) {
    this("NOT_FOUND", message);
  }

  public ResourceNotFoundException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
