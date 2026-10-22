package pe.edu.utp.hoseg.web.dto;

/**
 * Un destino del menú principal. La misma lista alimenta la barra superior,
 * el óvalo móvil y la columna "Navegar" del footer (AtributosGlobales).
 *
 * @param seccion  clave que marca el enlace activo (y data-seccion para navbar.js)
 * @param etiqueta texto visible
 * @param ruta     ruta de contexto, puede llevar ancla (/#impacto)
 * @param icono    clase de Bootstrap Icons; null si no va en el óvalo móvil
 */
public record EnlaceNavegacion(String seccion, String etiqueta, String ruta, String icono) {

    /** El óvalo móvil solo muestra los destinos con icono (cabe en 360 px). */
    public boolean enOvalo() {
        return icono != null;
    }
}
