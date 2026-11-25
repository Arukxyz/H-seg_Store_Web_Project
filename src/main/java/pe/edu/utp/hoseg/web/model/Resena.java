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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Tabla resena (08). Nace de una línea de venta pagada: el producto y el
 * cliente salen de detalle_venta → venta. Se publica al instante y el
 * administrador web puede ocultarla.
 */
@Entity
@Table(name = "resena")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_detalle_venta", nullable = false, unique = true)
    private DetalleVenta detalleVenta;

    /** SMALLINT en la base (1 a 5). */
    @Column(nullable = false)
    private short calificacion;

    @Column(length = 100)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String comentario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoResena estado;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;

    @Column(name = "oculta_en")
    private OffsetDateTime ocultaEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_modera")
    private Usuario usuarioModera;

    @PrePersist
    void alCrear() {
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
    }

    public void ocultar(Usuario moderador) {
        estado = EstadoResena.OCULTA;
        ocultaEn = OffsetDateTime.now();
        usuarioModera = moderador;
    }

    public void publicar(Usuario moderador) {
        estado = EstadoResena.PUBLICADA;
        ocultaEn = null;
        usuarioModera = moderador;
    }

    public Integer getId() { return id; }
    public DetalleVenta getDetalleVenta() { return detalleVenta; }
    public void setDetalleVenta(DetalleVenta detalleVenta) { this.detalleVenta = detalleVenta; }
    public int getCalificacion() { return calificacion; }
    public void setCalificacion(int calificacion) { this.calificacion = (short) calificacion; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public EstadoResena getEstado() { return estado; }
    public void setEstado(EstadoResena estado) { this.estado = estado; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public OffsetDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(OffsetDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
    public OffsetDateTime getOcultaEn() { return ocultaEn; }
    public Usuario getUsuarioModera() { return usuarioModera; }
}
