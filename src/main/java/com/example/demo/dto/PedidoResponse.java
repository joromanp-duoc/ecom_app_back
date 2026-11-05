package com.example.demo.dto;

import java.math.BigDecimal;

public record PedidoResponse(Long id, BigDecimal total, String estado) {
}
