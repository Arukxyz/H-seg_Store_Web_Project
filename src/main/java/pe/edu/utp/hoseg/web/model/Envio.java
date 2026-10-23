package pe.edu.utp.hoseg.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Tabla envio (08): cómo llega el pedido al cliente. La web la crea junto con
 * la venta; los datos de entrega son una foto tomada al comprar. El costo va
 * aparte de venta.total. El escritorio mueve el estado al confirmar o anular.
 */
@Entity
@Table(name = "envio")
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_venta", nullable = false, unique = true)
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoEnvio tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoEnvio estado;

    @Column(name = "nombre_receptor", nullable = false, length = 150)
    private String nombreReceptor;

    @Column(name = "telefono_receptor", length = 20)
    private String telefonoReceptor;

    @Column(length = 255)
    private String direccion;

    @Column(length = 255)
    private String referencia;

    @Column(length = 80)
    private String distrito;

    @Column(length = 80)
    private String provincia;

    @Column(length = 80)
    private String departamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(length = 80)
    private String courier;

    @Column(name = "codigo_seguimiento", length = 60)
    private String codigoSeguimiento;

    @Column(name = "fecha_despacho")
    private OffsetDateTime fechaDespacho;

    @Column(name = "fecha_entrega")
    private OffsetDateTime fechaEntrega;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    void alCrear() {
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
    }

    public Integer getId() { return id; }
    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
    public TipoEnvio getTipo() { return tipo; }
    public void setTipo(TipoEnvio tipo) { this.tipo = tipo; }
    public EstadoEnvio getEstado() { return estado; }
    public void setEstado(EstadoEnvio estado) { this.estado = estado; }
    public String getNombreReceptor() { return nombreReceptor; }
    public void setNombreReceptor(String nombreReceptor) { this.nombreReceptor = nombreReceptor; }
    public String getTelefonoReceptor() { return telefonoReceptor; }
    public void setTelefonoReceptor(String telefonoReceptor) { this.telefonoReceptor = telefonoReceptor; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
    public String getCourier() { return courier; }
    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public OffsetDateTime getFechaDespacho() { return fechaDespacho; }
    public OffsetDateTime getFechaEntrega() { return fechaEntrega; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }
}
