package com.example.demo.services;

import com.example.demo.models.Producto;
import com.example.demo.repositories.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }
    public Producto guardarProducto (Producto producto) {
        if (producto.getPrecio() == null ||
            producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "El precio debe ser mayor a 0"
            );
        }

        if (producto.getStock() == null ||
            producto.getStock() <= 0) {
            throw new IllegalArgumentException(
                "El stock debe ser mayor a 0"
            );
        }
        return productoRepository.save(producto);
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }
    public void eliminarProducto (Long id) {
        productoRepository.deleteById(id);
    }

}