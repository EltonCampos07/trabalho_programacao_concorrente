package br.com.up.fabrica_automoveis.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class VehicleFactoryExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> entityNotFound(IllegalArgumentException e, HttpServletRequest request){
    Map<String, Object> responseBody = new LinkedHashMap<>();
    responseBody.put("timestamp", Instant.now());
    responseBody.put("status", HttpStatus.BAD_REQUEST.value());
    responseBody.put("message", e.getMessage());
    responseBody.put("path", request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
  }
}
