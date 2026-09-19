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
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Tabla lote_donacion. Lo crea y despacha el escritorio; la web solo lo lee
 * para decir a qué comunidad llegó una donación.
 */
@Entity
@Table(name = "lote_donacion")
public class LoteDonacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_lote", nullable = false, unique = true, length = 30)
    private String codigoLote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_comunidad", nullable = false)
    private Comunidad comunidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ong", nullable = false)
    private Ong ong;

    /** Usuario interno responsable; la web no mapea usuario. */
    @Column(name = "id_usuario_responsable")
    private Integer idUsuarioResponsable;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_despacho")
    private OffsetDateTime fechaDespacho;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoLote estado;

    private String observaciones;

    public Integer getId() { return id; }
    public String getCodigoLote() { return codigoLote; }
    public Comunidad getComunidad() { return comunidad; }
    public Ong getOng() { return ong; }
    public Integer getIdUsuarioResponsable() { return idUsuarioResponsable; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public OffsetDateTime getFechaDespacho() { return fechaDespacho; }
    public EstadoLote getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }
}
