package com.cibertec.database.manager;

import com.cibertec.database.model.Movimientos;
import com.cibertec.database.model.Producto;
import com.cibertec.database.repository.MovimientoRepository;
import com.cibertec.database.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovimientoManager {

    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public Movimientos save(Movimientos movimientos){
        movimientos.setFecha(new Date());
        movimientos = movimientoRepository.save(movimientos);

        Producto producto = movimientos.getProducto();
        producto.setStock(producto.getStock() + movimientos.getCantidad());
        productoRepository.save(producto);
        return movimientos;
    }

    public List<Movimientos> getAll(){
        return movimientoRepository.findAll();
    }

    public Page<Movimientos> getAll(Pageable pageRequest){
        return movimientoRepository.findAll(pageRequest);
    }

    public Optional<Movimientos> getById(Long id){
        return movimientoRepository.findById(id);
    }
}
