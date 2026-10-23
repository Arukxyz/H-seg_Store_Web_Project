package pe.edu.utp.hoseg.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Tabla contenido_web (09): bloques de texto e imagen que el administrador
 * cambia sin tocar código. La clave la conoce la plantilla que lo muestra
 * ('home.banner', 'nosotros.historia', 'legal.terminos', 'cafe.arboles'…).
 */
@Entity
@Table(name = "contenido_web")
public class ContenidoWeb {

    /** Clave del bloque usada como cifra de árboles sembrados por el café. */
    public static final String CAFE_ARBOLES = "cafe.arboles";

    @Id
    @Column(length = 60)
    private String clave;

    @Column(length = 200)
    private String titulo;

    private String cuerpo;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @PrePersist
    @PreUpdate
    void alGuardar() {
        actualizadoEn = OffsetDateTime.now();
    }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getCuerpo() { return cuerpo; }
    public void setCuerpo(String cuerpo) { this.cuerpo = cuerpo; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public OffsetDateTime getActualizadoEn() { return actualizadoEn; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
