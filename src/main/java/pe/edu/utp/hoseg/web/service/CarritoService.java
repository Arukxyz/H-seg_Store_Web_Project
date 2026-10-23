package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import pe.edu.utp.hoseg.web.dto.ImpactoCompra;
import pe.edu.utp.hoseg.web.dto.ItemCarrito;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carrito de compras. Vive en la sesión HTTP (un bean por visitante); no hay
 * tabla en la base de datos hasta que el cliente confirma la compra. Al
 * iniciar sesión se conserva (Spring Security migra la sesión).
 * Serializable para que Tomcat conserve el carrito al reiniciar el servidor.
 */
@Service
@SessionScope
public class CarritoService implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ArrayList<ItemCarrito> items = new ArrayList<>();

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
        TipoCompromiso compromiso = producto.tieneCompromiso() ? producto.getTipoCompromiso() : null;
        items.add(new ItemCarrito(producto.getCodigo(), producto.getNombre(), producto.getTallaMostrada(),
                producto.getPrecio(), cantidad, compromiso, producto.getUrlImagen()));
    }

    /** Fija la cantidad de una línea (botones − / +); 0 o menos la quita. */
    public void actualizar(String codigoProducto, int cantidad) {
        if (cantidad <= 0) {
            quitar(codigoProducto);
            return;
        }
        items.replaceAll(item -> item.codigoProducto().equals(codigoProducto) ? item.conCantidad(cantidad) : item);
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

    /** "Tu impacto con esta compra: 2 abrigos + 1 árbol" (1 por unidad). */
    public ImpactoCompra impacto() {
        long abrigos = 0;
        long arboles = 0;
        for (ItemCarrito item : items) {
            if (item.tipoCompromiso() == TipoCompromiso.ABRIGO) {
                abrigos += item.cantidad();
            } else if (item.tipoCompromiso() == TipoCompromiso.ARBOL) {
                arboles += item.cantidad();
            }
        }
        return new ImpactoCompra(abrigos, arboles);
    }
}
