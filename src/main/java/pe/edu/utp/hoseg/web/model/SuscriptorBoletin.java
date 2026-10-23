package pe.edu.utp.hoseg.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Tabla suscriptor_boletin (09). Darse de baja no borra: marca activo = false,
 * y volver a suscribirse reactiva la misma fila (email único sin mayúsculas).
 */
@Entity
@Table(name = "suscriptor_boletin")
public class SuscriptorBoletin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "suscrito_en", nullable = false)
    private OffsetDateTime suscritoEn;

    @Column(name = "baja_en")
    private OffsetDateTime bajaEn;

    @PrePersist
    void alCrear() {
        if (suscritoEn == null) {
            suscritoEn = OffsetDateTime.now();
        }
    }

    /** activo y baja_en van juntos (suscriptor_baja_check). */
    public void reactivar() {
        activo = true;
        bajaEn = null;
        suscritoEn = OffsetDateTime.now();
    }

    public void darDeBaja() {
        activo = false;
        bajaEn = OffsetDateTime.now();
    }

    public Integer getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActivo() { return activo; }
    public OffsetDateTime getSuscritoEn() { return suscritoEn; }
    public OffsetDateTime getBajaEn() { return bajaEn; }
}
