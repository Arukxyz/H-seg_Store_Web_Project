package pe.edu.utp.hoseg.web.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Tabla categoria (07): valores admitidos en producto.categoria. Las da de alta el escritorio. */
@Entity
@Immutable
@Table(name = "categoria")
public class Categoria {

    @Id
    private String nombre;

    public String getNombre() { return nombre; }
}
