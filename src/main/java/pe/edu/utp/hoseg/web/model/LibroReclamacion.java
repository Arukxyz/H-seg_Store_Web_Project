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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Tabla libro_reclamaciones (09): hoja de reclamación virtual, obligatoria
 * para tiendas en línea en Perú. El número de hoja (LR-000001) lo genera la
 * base con seq_reclamacion; Hibernate lo lee después del insert.
 */
@Entity
@Table(name = "libro_reclamaciones")
public class LibroReclamacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Generated
    @Column(name = "numero_hoja", nullable = false, unique = true, length = 20, insertable = false, updatable = false)
    private String numeroHoja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoReclamo tipo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_doc", nullable = false, length = 10)
    private TipoDocumento tipoDoc;

    @Column(name = "num_doc", nullable = false, length = 20)
    private String numDoc;

    @Column(nullable = false, length = 255)
    private String domicilio;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_bien", nullable = false, length = 10)
    private TipoBien tipoBien;

    @Column(name = "monto_reclamado", precision = 10, scale = 2)
    private BigDecimal montoReclamado;

    @Column(name = "descripcion_bien", nullable = false, length = 255)
    private String descripcionBien;

    @Column(nullable = false)
    private String detalle;

    @Column(name = "pedido_consumidor", nullable = false)
    private String pedidoConsumidor;

    /** Código WEB-nnnnnn opcional; la base exige que exista en venta. */
    @Column(name = "codigo_comprobante", length = 30)
    private String codigoComprobante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoReclamo estado = EstadoReclamo.REGISTRADO;

    private String respuesta;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "atendido_en")
    private OffsetDateTime atendidoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_atiende")
    private Usuario usuarioAtiende;

    @PrePersist
    void alCrear() {
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
    }

    /** Atender es responder: estado, respuesta y fecha van juntos (libro_reclamaciones_atencion_check). */
    public void responder(String respuesta, Usuario usuario) {
        this.respuesta = respuesta;
        estado = EstadoReclamo.ATENDIDO;
        atendidoEn = OffsetDateTime.now();
        usuarioAtiende = usuario;
    }

    public Integer getId() { return id; }
    public String getNumeroHoja() { return numeroHoja; }
    public TipoReclamo getTipo() { return tipo; }
    public void setTipo(TipoReclamo tipo) { this.tipo = tipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoDocumento getTipoDoc() { return tipoDoc; }
    public void setTipoDoc(TipoDocumento tipoDoc) { this.tipoDoc = tipoDoc; }
    public String getNumDoc() { return numDoc; }
    public void setNumDoc(String numDoc) { this.numDoc = numDoc; }
    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public TipoBien getTipoBien() { return tipoBien; }
    public void setTipoBien(TipoBien tipoBien) { this.tipoBien = tipoBien; }
    public BigDecimal getMontoReclamado() { return montoReclamado; }
    public void setMontoReclamado(BigDecimal montoReclamado) { this.montoReclamado = montoReclamado; }
    public String getDescripcionBien() { return descripcionBien; }
    public void setDescripcionBien(String descripcionBien) { this.descripcionBien = descripcionBien; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getPedidoConsumidor() { return pedidoConsumidor; }
    public void setPedidoConsumidor(String pedidoConsumidor) { this.pedidoConsumidor = pedidoConsumidor; }
    public String getCodigoComprobante() { return codigoComprobante; }
    public void setCodigoComprobante(String codigoComprobante) { this.codigoComprobante = codigoComprobante; }
    public EstadoReclamo getEstado() { return estado; }
    public String getRespuesta() { return respuesta; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public OffsetDateTime getAtendidoEn() { return atendidoEn; }
    public Usuario getUsuarioAtiende() { return usuarioAtiende; }
}
