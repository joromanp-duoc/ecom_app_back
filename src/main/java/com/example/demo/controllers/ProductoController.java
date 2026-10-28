package com.example.demo.controllers;

import com.example.demo.models.Producto;
import com.example.demo.services.ProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("api/productos")
public class ProductoController {
    @Autowired
    private ProductoService productoService;

    @GetMapping
    public List<Producto> listaProductos () {
        return productoService.obtenerTodos();
    }

    @PostMapping
    public Producto creaProducto(@RequestBody Producto producto) {
        return productoService.guardarProducto(producto);
    }
    
    @GetMapping("/{id}")
    public Producto obtenerProducto(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable Long id){
        productoService.eliminarProducto(id);
    }
    
}
