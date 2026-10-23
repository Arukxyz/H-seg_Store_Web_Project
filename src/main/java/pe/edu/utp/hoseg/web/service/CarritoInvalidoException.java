package pe.edu.utp.hoseg.web.service;

import pe.edu.utp.hoseg.web.dto.ProblemaCarrito;

import java.util.List;

/** El carrito no se puede confirmar; trae un mensaje por línea para mostrarlo junto a cada una. */
public class CarritoInvalidoException extends NegocioException {

    private final List<ProblemaCarrito> problemas;

    public CarritoInvalidoException(List<ProblemaCarrito> problemas) {
        super(problemas.size() == 1 ? problemas.get(0).mensaje()
                : "Revisa tu carrito: hay " + problemas.size() + " productos con problemas.");
        this.problemas = List.copyOf(problemas);
    }

    public List<ProblemaCarrito> getProblemas() {
        return problemas;
    }
}
