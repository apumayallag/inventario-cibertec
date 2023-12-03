package com.cibertec.database.manager;

import com.cibertec.database.model.Producto;
import com.cibertec.database.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoManager {

    private final ProductoRepository productoRepository;

    public Producto save(Producto producto){
        return productoRepository.save(producto);
    }

    public List<Producto> getAll(){
        return productoRepository.findAll();
    }

    public Page<Producto> getAll(Pageable pageRequest){
        return productoRepository.findAll(pageRequest);
    }

    public Optional<Producto> getById(Long id){
        return productoRepository.findById(id);
    }
}
