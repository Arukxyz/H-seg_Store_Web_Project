package pe.edu.utp.hoseg.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Tabla comunidad: destinos altoandinos de Cusco que reciben las donaciones. */
@Entity
@Table(name = "comunidad")
public class Comunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 80)
    private String distrito;

    @Column(length = 80)
    private String provincia;

    @Column(nullable = false, length = 80)
    private String region;

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDistrito() { return distrito; }
    public String getProvincia() { return provincia; }
    public String getRegion() { return region; }
}
