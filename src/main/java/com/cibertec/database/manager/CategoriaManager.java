package com.cibertec.database.manager;

import com.cibertec.database.model.Categoria;
import com.cibertec.database.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriaManager {

    private final CategoriaRepository categoriaRepository;

    public Categoria save(Categoria categoria){
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> getAll(){
        return categoriaRepository.findAll();
    }

    public Page<Categoria> getAll(Pageable pageRequest){
        return categoriaRepository.findAll(pageRequest);
    }

    public Optional<Categoria> getById(Long id){
        return categoriaRepository.findById(id);
    }
}
