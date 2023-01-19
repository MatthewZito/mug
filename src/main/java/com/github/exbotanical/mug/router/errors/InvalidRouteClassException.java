package com.github.exbotanical.mug.router.errors;

/**
 * An exception for invalid route classes or methods during reflection of those passed to
 * `Router.use`.
 */
public class InvalidRouteClassException extends RuntimeException {
  public InvalidRouteClassException(final Throwable cause) {
    super("Invalid route class or handler method.", cause);
  }
}
