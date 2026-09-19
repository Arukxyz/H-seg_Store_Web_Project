package pe.edu.utp.hoseg.web.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla venta: el "pedido" del enunciado. La web la inserta con
 * origen=WEB y estado=PENDIENTE; el escritorio la confirma y descuenta stock.
 */
@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** WEB-000110, obtenido de seq_comprobante_web (nunca MAX + 1). */
    @Column(name = "codigo_comprobante", nullable = false, unique = true, length = 30)
    private String codigoComprobante;

    @Column(nullable = false)
    private OffsetDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    /** Usuario interno que confirmó la venta. Lo escribe el escritorio; la web no mapea usuario. */
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private OrigenVenta origen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoVenta estado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleVenta detalle) {
        detalle.setVenta(this);
        detalles.add(detalle);
    }

    public Integer getId() { return id; }
    public String getCodigoComprobante() { return codigoComprobante; }
    public void setCodigoComprobante(String codigoComprobante) { this.codigoComprobante = codigoComprobante; }
    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Integer getIdUsuario() { return idUsuario; }
    public OrigenVenta getOrigen() { return origen; }
    public void setOrigen(OrigenVenta origen) { this.origen = origen; }
    public EstadoVenta getEstado() { return estado; }
    public void setEstado(EstadoVenta estado) { this.estado = estado; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public List<DetalleVenta> getDetalles() { return detalles; }
}
