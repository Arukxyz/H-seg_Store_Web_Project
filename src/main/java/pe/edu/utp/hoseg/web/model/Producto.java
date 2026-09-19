package pe.edu.utp.hoseg.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Tabla producto. La PK es el código (HSG-CAS-001) y cada talla es una fila
 * distinta con el mismo nombre. El stock solo se lee desde la web: quien lo
 * descuenta es el escritorio al confirmar el pedido.
 */
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @Column(length = 30)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 80)
    private String marca;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Column(length = 80)
    private String coleccion;

    @Column(length = 10)
    private String talla;

    private String descripcion;

    @Column(name = "url_imagen")
    private String urlImagen;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "stock_comercial", nullable = false)
    private int stockComercial;

    @Column(name = "stock_comprometido", nullable = false)
    private int stockComprometido;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo;

    @Column(name = "aplica_triple_impacto", nullable = false)
    private boolean aplicaTripleImpacto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_compromiso", length = 10)
    private TipoCompromiso tipoCompromiso;

    @Column(name = "visible_web", nullable = false)
    private boolean visibleWeb;

    @Column(nullable = false)
    private boolean activo;

    /** Lo asigna la base (DEFAULT now()); el escritorio es quien da de alta productos. */
    @Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime creadoEn;

    /** Solo se registra donación si el producto tiene compromiso definido. */
    public boolean tieneCompromiso() {
        return aplicaTripleImpacto && tipoCompromiso != null;
    }

    public boolean estaDisponible() {
        return stockComercial > 0;
    }

    /** Talla mostrada al cliente: la semilla usa NULL y "Única" indistintamente. */
    public String getTallaMostrada() {
        return talla == null || talla.isBlank() ? "Única" : talla;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getColeccion() { return coleccion; }
    public void setColeccion(String coleccion) { this.coleccion = coleccion; }
    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public int getStockComercial() { return stockComercial; }
    public void setStockComercial(int stockComercial) { this.stockComercial = stockComercial; }
    public int getStockComprometido() { return stockComprometido; }
    public void setStockComprometido(int stockComprometido) { this.stockComprometido = stockComprometido; }
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    public boolean isAplicaTripleImpacto() { return aplicaTripleImpacto; }
    public void setAplicaTripleImpacto(boolean aplicaTripleImpacto) { this.aplicaTripleImpacto = aplicaTripleImpacto; }
    public TipoCompromiso getTipoCompromiso() { return tipoCompromiso; }
    public void setTipoCompromiso(TipoCompromiso tipoCompromiso) { this.tipoCompromiso = tipoCompromiso; }
    public boolean isVisibleWeb() { return visibleWeb; }
    public void setVisibleWeb(boolean visibleWeb) { this.visibleWeb = visibleWeb; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }
}
