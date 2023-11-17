package com.cibertec.database.manager;

import com.cibertec.database.model.Proveedores;
import com.cibertec.database.repository.ProveedoresRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProveedoresManager {

    private final ProveedoresRepository proveedoresRepository;

    public Proveedores save(Proveedores proveedores){
        return proveedoresRepository.save(proveedores);
    }

    public List<Proveedores> getAll(){
        return proveedoresRepository.findAll();
    }

    public Page<Proveedores> getAll(Pageable pageRequest){
        return proveedoresRepository.findAll(pageRequest);
    }

    public Optional<Proveedores> getById(Long id){
        return proveedoresRepository.findById(id);
    }
}
