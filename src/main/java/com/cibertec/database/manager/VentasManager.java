package com.cibertec.database.manager;

import com.cibertec.database.model.Producto;
import com.cibertec.database.model.Ventas;
import com.cibertec.database.repository.ProductoRepository;
import com.cibertec.database.repository.VentasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VentasManager {

    private final VentasRepository ventasRepository;
    private final ProductoRepository productoRepository;

    public Ventas save(Ventas ventas){
        Producto producto = ventas.getProducto();
        producto.setStock(producto.getStock() - ventas.getCantidad());
        ventas = ventasRepository.save(ventas);

        productoRepository.save(producto);
        return ventas;
    }

    public List<Ventas> getAll(){
        return ventasRepository.findAll();
    }

    public Page<Ventas> getAll(Pageable pageRequest){
        return ventasRepository.findAll(pageRequest);
    }

    public Optional<Ventas> getById(Long id){
        return ventasRepository.findById(id);
    }
}
