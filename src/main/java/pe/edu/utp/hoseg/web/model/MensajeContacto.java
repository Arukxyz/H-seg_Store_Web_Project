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

import java.time.OffsetDateTime;

/** Tabla mensaje_contacto (09): formulario de contacto, atendido desde el panel. */
@Entity
@Table(name = "mensaje_contacto")
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 150)
    private String asunto;

    @Column(nullable = false)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoMensaje estado = EstadoMensaje.NUEVO;

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

    /** Estado, fecha y responsable van juntos (mensaje_contacto_atencion_check). */
    public void marcarAtendido(Usuario usuario) {
        estado = EstadoMensaje.ATENDIDO;
        atendidoEn = OffsetDateTime.now();
        usuarioAtiende = usuario;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public EstadoMensaje getEstado() { return estado; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public OffsetDateTime getAtendidoEn() { return atendidoEn; }
    public Usuario getUsuarioAtiende() { return usuarioAtiende; }
}
