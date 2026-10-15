package gescazone.demo.application.service;

import gescazone.demo.domain.repository.CatalogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** Valores vigentes de los catálogos (roles, tipos de documento, estados…). */
@Service
public class CatalogoService {

    @Autowired
    private CatalogoRepository catalogoRepository;

    public Map<String, List<String>> todos() {
        return catalogoRepository.todos();
    }
}
