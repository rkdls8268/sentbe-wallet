package com.sentbe.global.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sentbe.global.status.ErrorStatus;
import com.sentbe.global.status.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

  private final Boolean isSuccess;
  private final String code;
  private final String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final T result;

  // 성공 - 기본 응답
  public static ResponseEntity<ApiResponse> onSuccess(SuccessStatus status) {
    return new ResponseEntity<>(
      new ApiResponse(true, status.getCode(), status.getMessage(), null),
      status.getHttpStatus());
  }

  // 성공 - 데이터 포함
  public static <T> ResponseEntity<ApiResponse> onSuccess(SuccessStatus status, T result) {
    return new ResponseEntity<>(
      new ApiResponse(true, status.getCode(), status.getMessage(), result),
      status.getHttpStatus());
  }

  // 실패
  public static ResponseEntity<ApiResponse> onFailure(ErrorStatus error) {
    return new ResponseEntity<>(
      new ApiResponse(false, error.getCode(), error.getMessage(), null),
      error.getHttpStatus());
  }

  public static ResponseEntity<ApiResponse> onFailure(ErrorStatus error, String message) {
    return new ResponseEntity<>(
      new ApiResponse(false, error.getCode(), error.getMessage(message), null),
      error.getHttpStatus());
  }

  public static ResponseEntity<ApiResponse> onFailure(ErrorStatus error, Object data) {
    return new ResponseEntity<>(
      new ApiResponse(false, error.getCode(), error.getMessage(), data),
      error.getHttpStatus());
  }
}
