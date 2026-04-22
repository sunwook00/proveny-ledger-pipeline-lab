package com.example.provenylab.web;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<Map<String,Object>> badRequest(IllegalArgumentException e){return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",e.getMessage()));}
  @ExceptionHandler(Exception.class) public ResponseEntity<Map<String,Object>> internal(Exception e){return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",e.getClass().getSimpleName(),"message",String.valueOf(e.getMessage())));}
}
