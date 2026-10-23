package pe.edu.utp.hoseg.web.dto;

/**
 * Lo que genera una compra (carrito o pedido): "2 abrigos + 1 árbol".
 * También es el impacto acumulado de un cliente ("fidelidad").
 */
public record ImpactoCompra(long abrigos, long arboles) {

    public static final ImpactoCompra NINGUNO = new ImpactoCompra(0, 0);

    public boolean tieneImpacto() {
        return abrigos > 0 || arboles > 0;
    }

    /** "2 abrigos + 1 árbol", "1 abrigo", "3 árboles". */
    public String getTexto() {
        String a = abrigos == 1 ? "1 abrigo" : abrigos + " abrigos";
        String b = arboles == 1 ? "1 árbol" : arboles + " árboles";
        if (abrigos > 0 && arboles > 0) {
            return a + " + " + b;
        }
        return abrigos > 0 ? a : arboles > 0 ? b : "";
    }
}
