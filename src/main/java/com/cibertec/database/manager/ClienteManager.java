package com.cibertec.database.manager;

import com.cibertec.database.model.Cliente;
import com.cibertec.database.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteManager {

    private final ClienteRepository clienteRepository;

    public Cliente save(Cliente cliente){
        return clienteRepository.save(cliente);
    }

    public List<Cliente> getAll(){
        return clienteRepository.findAll();
    }

    public Page<Cliente> getAll(Pageable pageRequest){
        return clienteRepository.findAll(pageRequest);
    }

    public Optional<Cliente> getById(Long id){
        return clienteRepository.findById(id);
    }
}
