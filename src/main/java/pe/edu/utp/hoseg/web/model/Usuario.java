package pe.edu.utp.hoseg.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;

/**
 * Tabla usuario: personal interno de Höség. La administra el escritorio; la
 * web solo la lee para el login del panel /admin. El bloqueo por intentos lo
 * actualiza UsuarioRepository con SQL, con las mismas reglas del escritorio.
 */
@Entity
@Immutable
@Table(name = "usuario")
public class Usuario {

    @Id
    private Integer id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** SHA-256(salt + contraseña) en hexadecimal, igual que HashUtil del escritorio. */
    @Column(name = "password_hash", nullable = false, length = 64)
    private String passwordHash;

    @Column(nullable = false, length = 32)
    private String salt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private OffsetDateTime bloqueadoHasta;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt() { return salt; }
    public RolUsuario getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public OffsetDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }
}
