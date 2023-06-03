package io.github.augustoravazoli.bookapi;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
class GlobalExceptionHandler {

  private static record ErrorResponse(String message, List<ErrorDetails> details) {}

  private static record ErrorDetails(String field, String message) {}

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handle(MethodArgumentNotValidException ex) {
    var details = ex.getBindingResult().getAllErrors()
      .stream()
      .map(error -> new ErrorDetails(
        error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName(),
        error.getDefaultMessage()
      ))
      .toList();
    return ResponseEntity
      .unprocessableEntity()
      .body(new ErrorResponse("Validation errors on your request", details));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<?> handle(ResponseStatusException ex) {
    return ResponseEntity
      .status(ex.getStatusCode())
      .body(new ErrorResponse(ex.getReason(), null));
  }

}
