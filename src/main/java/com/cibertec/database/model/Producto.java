package com.cibertec.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private Double stock;

    private Double precio;

    @ManyToOne
    private Categoria categoria;

    @ManyToOne
    private Proveedores proveedores;

    @Override
    public String toString() {
        return nombre;
    }
}
