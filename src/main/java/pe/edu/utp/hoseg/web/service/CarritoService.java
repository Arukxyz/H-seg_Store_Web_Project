package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import pe.edu.utp.hoseg.web.dto.ItemCarrito;
import pe.edu.utp.hoseg.web.model.Producto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carrito de compras. Vive en la sesión HTTP (un bean por visitante); no hay
 * tabla en la base de datos hasta que el cliente confirma la compra.
 */
@Service
@SessionScope
public class CarritoService {

    private final List<ItemCarrito> items = new ArrayList<>();

    /** Agrega unidades de un producto (una talla = un código); si ya estaba, suma. */
    public void agregar(Producto producto, int cantidad) {
        if (cantidad <= 0) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            ItemCarrito item = items.get(i);
            if (item.codigoProducto().equals(producto.getCodigo())) {
                items.set(i, item.conCantidad(item.cantidad() + cantidad));
                return;
            }
        }
        items.add(new ItemCarrito(producto.getCodigo(), producto.getNombre(),
                producto.getTallaMostrada(), producto.getPrecio(), cantidad));
    }

    public void quitar(String codigoProducto) {
        items.removeIf(item -> item.codigoProducto().equals(codigoProducto));
    }

    public void vaciar() {
        items.clear();
    }

    public List<ItemCarrito> items() {
        return Collections.unmodifiableList(items);
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    /** Número que muestra el badge del carrito en la barra de navegación. */
    public int totalUnidades() {
        return items.stream().mapToInt(ItemCarrito::cantidad).sum();
    }

    public BigDecimal total() {
        return items.stream().map(ItemCarrito::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
