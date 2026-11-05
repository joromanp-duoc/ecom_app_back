package com.example.demo.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class PedidoErrorHandler {

    @ExceptionHandler(StockInsuficienteException.class)
    ResponseEntity<Map<String, String>> stock(StockInsuficienteException ex) {
        return ResponseEntity.status(409)
                .body(Map.of("error", mensaje(ex, "Stock insuficiente")));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    ResponseEntity<Map<String, String>> inexistente(EntityNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(Map.of("error", mensaje(ex, "Producto inexistente")));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> invalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", mensaje(ex, "Solicitud inválida")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errores.putIfAbsent(
                error.getField(),
                error.getDefaultMessage() == null
                        ? "Valor inválido"
                        : error.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", errores.values().stream().findFirst()
                .orElse("Datos inválidos"));
        body.put("errores", errores);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> bodyInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "El cuerpo de la solicitud no es un JSON válido"));
    }

    private String mensaje(Exception ex, String porDefecto) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? porDefecto
                : ex.getMessage();
    }
}
