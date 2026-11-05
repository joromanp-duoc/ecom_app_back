package com.example.demo.services;

import com.example.demo.dto.CrearPedidoRequest;
import com.example.demo.dto.ItemPedidoRequest;
import com.example.demo.dto.PedidoResponse;
import com.example.demo.exceptions.StockInsuficienteException;
import com.example.demo.models.DetallePedido;
import com.example.demo.models.Pedido;
import com.example.demo.models.Producto;
import com.example.demo.repositories.DetallePedidoRepository;
import com.example.demo.repositories.PedidoRepository;
import com.example.demo.repositories.ProductoRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PedidoService {
    private final PedidoRepository pedidos;
    private final DetallePedidoRepository detalles;
    private final ProductoRepository productos;

    public PedidoService(PedidoRepository pedidos,
            DetallePedidoRepository detalles,
            ProductoRepository productos) {
        this.pedidos = pedidos;
        this.detalles = detalles;
        this.productos = productos;
    }

    @Transactional
    public PedidoResponse crear(CrearPedidoRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        Map<Long, Integer> cantidades = new TreeMap<>();
        for (ItemPedidoRequest item : request.items()) {
            if (item == null || item.productoId() == null
                    || item.cantidad() == null || item.cantidad() <= 0) {
                throw new IllegalArgumentException("Item de pedido inválido");
            }

            try {
                cantidades.merge(item.productoId(), item.cantidad(), Math::addExact);
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException(
                        "La cantidad solicitada es demasiado grande");
            }
        }

        Pedido pedido = new Pedido();
        pedidos.save(pedido);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : cantidades.entrySet()) {
            Producto producto = productos.findByIdForUpdate(entry.getKey())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto inexistente con id " + entry.getKey()));

            int cantidad = entry.getValue();
            if (producto.getStock() == null || producto.getStock() < cantidad) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para el producto " + producto.getNombre());
            }

            producto.setStock(producto.getStock() - cantidad);

            BigDecimal precio = producto.getPrecio();
            if (precio == null) {
                throw new IllegalArgumentException(
                        "El producto no tiene un precio válido");
            }

            total = total.add(precio.multiply(BigDecimal.valueOf(cantidad)));
            detalles.save(new DetallePedido(pedido, producto, cantidad, precio));
        }

        pedido.setTotal(total);
        return new PedidoResponse(pedido.getId(), total, pedido.getEstado());
    }
}
