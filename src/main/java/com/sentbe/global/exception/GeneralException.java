package com.sentbe.global.exception;


import com.sentbe.global.status.ErrorStatus;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

  private final ErrorStatus errorStatus;
  private final Object data;

  public GeneralException(ErrorStatus errorStatus) {
    super(errorStatus.getMessage());
    this.errorStatus = errorStatus;
    this.data = null;
  }
}
