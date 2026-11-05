package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CrearPedidoRequest(
        @NotEmpty(message = "El carrito no puede estar vacío")
        List<@Valid ItemPedidoRequest> items) {
}
