package com.sentbe.cash.domain.exception;

public class DuplicateRequestInProgressException extends RuntimeException {
  public DuplicateRequestInProgressException(String message) {
    super(message);
  }
}