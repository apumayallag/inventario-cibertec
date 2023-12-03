package com.cibertec.database.repository;

import com.cibertec.database.model.Movimientos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimientos, Long> {
}
