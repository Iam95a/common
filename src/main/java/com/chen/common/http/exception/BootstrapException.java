package com.chen.common.http.exception;

public class BootstrapException extends RuntimeException {

  public BootstrapException() {
    super();
  }

  public BootstrapException(String message) {
    super(message);
  }

  public BootstrapException(String message, Throwable cause) {
    super(message, cause);
  }

  public BootstrapException(Throwable cause) {
    super(cause);
  }
}
