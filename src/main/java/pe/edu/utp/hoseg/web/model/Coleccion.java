package pe.edu.utp.hoseg.web.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Tabla coleccion (07): valores admitidos en producto.coleccion. Las da de alta el escritorio. */
@Entity
@Immutable
@Table(name = "coleccion")
public class Coleccion {

    @Id
    private String nombre;

    public String getNombre() { return nombre; }
}
