package com.chen.common.http.exception;

public class BootstrapNoPermissionException extends BootstrapException {

  public BootstrapNoPermissionException() {
    super();
  }

  public BootstrapNoPermissionException(String message) {
    super(message);
  }

  public BootstrapNoPermissionException(String message, Throwable cause) {
    super(message, cause);
  }

  public BootstrapNoPermissionException(Throwable cause) {
    super(cause);
  }
}
