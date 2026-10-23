package pe.edu.utp.hoseg.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Tabla cliente (compradores del e-commerce; no confundir con usuario, el
 * personal interno). apellido, direccion y password_hash los agrega
 * 06_web_clientes.sql; password_hash guarda BCrypt y queda NULL en los
 * clientes históricos de la semilla, que aún no pueden iniciar sesión.
 * terminos_aceptados_en, intentos_fallidos y bloqueado_hasta los agrega 08.
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_doc", length = 10)
    private TipoDocumento tipoDoc;

    @Column(name = "num_doc", length = 20)
    private String numDoc;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @Column(name = "password_hash", length = 72)
    private String passwordHash;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    /** NULL en los compradores históricos: aceptan al registrarse. */
    @Column(name = "terminos_aceptados_en")
    private OffsetDateTime terminosAceptadosEn;

    /** Lo incrementa y reinicia Postgres (ClienteRepository), nunca Java. */
    @Column(name = "intentos_fallidos", nullable = false, insertable = false, updatable = false)
    private int intentosFallidos;

    @Column(name = "bloqueado_hasta", insertable = false, updatable = false)
    private OffsetDateTime bloqueadoHasta;

    @PrePersist
    void alCrear() {
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
    }

    public boolean puedeIniciarSesion() {
        return passwordHash != null;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    /** "María G.": cómo firma sus reseñas, sin exponer el apellido completo. */
    public String getNombrePublico() {
        return nombre.split("\\s+")[0] + " " + apellido.charAt(0) + ".";
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public TipoDocumento getTipoDoc() { return tipoDoc; }
    public void setTipoDoc(TipoDocumento tipoDoc) { this.tipoDoc = tipoDoc; }
    public String getNumDoc() { return numDoc; }
    public void setNumDoc(String numDoc) { this.numDoc = numDoc; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public OffsetDateTime getTerminosAceptadosEn() { return terminosAceptadosEn; }
    public void setTerminosAceptadosEn(OffsetDateTime terminosAceptadosEn) { this.terminosAceptadosEn = terminosAceptadosEn; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public OffsetDateTime getBloqueadoHasta() { return bloqueadoHasta; }
}
