package pe.edu.utp.hoseg.web.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Una página de la grilla de /tienda ({@code ?pagina=2&porPagina=12}).
 *
 * <p>Se pagina en memoria y no con {@code Pageable}: la tienda muestra modelos (filas de
 * {@code producto} agrupadas por {@code nombre}, una por talla), y el filtro "solo
 * disponibles" y el orden por precio se calculan sobre el modelo completo. Paginar en la
 * base cortaría tallas, no modelos.</p>
 *
 * @param productos      tarjetas de esta página
 * @param pagina         página actual, desde 1 (ya ajustada al rango válido)
 * @param porPagina      tamaño de página, uno de {@link #TAMANOS}
 * @param totalProductos modelos que cumplen los filtros, en todas las páginas
 * @param totalPaginas   al menos 1, aunque no haya resultados
 */
public record PaginaCatalogo(
        List<TarjetaProducto> productos,
        int pagina,
        int porPagina,
        int totalProductos,
        int totalPaginas) {

    /** Tamaños que ofrece el selector: múltiplos de 3 para llenar filas completas de la grilla. */
    public static final List<Integer> TAMANOS = List.of(6, 12, 18);
    public static final int POR_PAGINA_DEFECTO = 12;

    /** Corta la página pedida; valores fuera de rango se corrigen en vez de fallar. */
    public static PaginaCatalogo de(List<TarjetaProducto> todos, Integer pagina, Integer porPagina) {
        int tamano = porPagina != null && TAMANOS.contains(porPagina) ? porPagina : POR_PAGINA_DEFECTO;
        int paginas = Math.max(1, (todos.size() + tamano - 1) / tamano);
        int actual = pagina == null ? 1 : Math.min(Math.max(pagina, 1), paginas);
        int desde = (actual - 1) * tamano;
        int hasta = Math.min(desde + tamano, todos.size());
        return new PaginaCatalogo(List.copyOf(todos.subList(desde, hasta)), actual, tamano, todos.size(), paginas);
    }

    public boolean vacia() {
        return productos.isEmpty();
    }

    /** Posición del primer producto mostrado ("Mostrando 13–24 de 30"). */
    public int desde() {
        return totalProductos == 0 ? 0 : (pagina - 1) * porPagina + 1;
    }

    public int hasta() {
        return (pagina - 1) * porPagina + productos.size();
    }

    public boolean hayAnterior() {
        return pagina > 1;
    }

    public boolean haySiguiente() {
        return pagina < totalPaginas;
    }

    /**
     * Números del paginador: la primera, la última y las vecinas de la actual.
     * Un 0 marca un salto ("…"). Ej.: página 6 de 10 → 1, 0, 5, 6, 7, 0, 10.
     * Un salto nunca reemplaza a un solo número: en su lugar se muestra ese número.
     */
    public List<Integer> numeros() {
        List<Integer> numeros = new ArrayList<>();
        for (int n = 1; n <= totalPaginas; n++) {
            boolean extremo = n == 1 || n == totalPaginas;
            boolean vecina = Math.abs(n - pagina) <= 1;
            boolean tapaHueco = (n == 2 && pagina == 4) || (n == totalPaginas - 1 && pagina == totalPaginas - 3);
            if (extremo || vecina || tapaHueco) {
                numeros.add(n);
            } else if (numeros.get(numeros.size() - 1) != 0) {
                numeros.add(0);
            }
        }
        return numeros;
    }
}
