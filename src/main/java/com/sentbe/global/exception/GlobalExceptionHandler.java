package com.sentbe.global.exception;

import com.sentbe.global.response.ApiResponse;
import com.sentbe.global.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String EXCEPTION_ATTRIBUTE = "handledException";

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    storeException(e);

    return ApiResponse.onFailure(ErrorStatus.VALIDATION_ERROR, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(
    MethodArgumentNotValidException e) {
    storeException(e);

    String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
    return ApiResponse.onFailure(ErrorStatus.VALIDATION_ERROR, errorMessage);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse> handleException(Exception e) {
    storeException(e);

    return ApiResponse.onFailure((ErrorStatus.INTERNAL_SERVER_ERROR));
  }

  @ExceptionHandler(GeneralException.class)
  public ResponseEntity<ApiResponse> handleGeneralException(GeneralException e) {
    storeException(e);

    if (e.getData() != null) {
      return ApiResponse.onFailure(e.getErrorStatus(), e.getData());
    }

    return ApiResponse.onFailure(e.getErrorStatus(), e.getMessage());
  }



  private void storeException(Exception e) {
    try {
      ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

      if (attributes != null) {
        HttpServletRequest request = attributes.getRequest();
        request.setAttribute(EXCEPTION_ATTRIBUTE, e);
      }
    } catch (Exception ex) {
      log.error("Failed to save exception in GlobalExceptionHandler", ex);
    }
  }
}
