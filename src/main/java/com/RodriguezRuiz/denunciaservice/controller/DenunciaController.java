package com.RodriguezRuiz.denunciaservice.controller;

import com.RodriguezRuiz.denunciaservice.dto.DenunciaRequest;
import com.RodriguezRuiz.denunciaservice.dto.DenunciaResponse;
import com.RodriguezRuiz.denunciaservice.service.DenunciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/denuncias", "/denuncias/"})
public class DenunciaController {

    private final DenunciaService denunciaService;

    public DenunciaController(DenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public DenunciaResponse registrar(@Valid @RequestBody DenunciaRequest request) {
        return denunciaService.registrar(request);
    }

    @GetMapping({"", "/"})
    public List<DenunciaResponse> listar(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return denunciaService.listar(offset, limit);
    }

    @GetMapping({"/{id}", "/{id}/"})
    public DenunciaResponse obtenerPorId(@PathVariable Integer id) {
        return denunciaService.obtenerPorId(id);
    }

    @GetMapping({"/ciudadano/{dni}", "/ciudadano/{dni}/"})
    public List<DenunciaResponse> listarPorCiudadano(@PathVariable String dni) {
        return denunciaService.listarPorCiudadano(dni);
    }

    @PutMapping({"/{id}", "/{id}/"})
    public DenunciaResponse atender(@PathVariable Integer id) {
        return denunciaService.atender(id);
    }
}
