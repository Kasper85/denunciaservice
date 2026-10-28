package com.RodriguezRuiz.denunciaservice.service;

import com.RodriguezRuiz.denunciaservice.dto.DenunciaRequest;
import com.RodriguezRuiz.denunciaservice.dto.DenunciaResponse;
import com.RodriguezRuiz.denunciaservice.entity.Denuncia;
import com.RodriguezRuiz.denunciaservice.exception.DenunciaNotFoundException;
import com.RodriguezRuiz.denunciaservice.repository.DenunciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DenunciaServiceImpl implements DenunciaService {

    private final DenunciaRepository denunciaRepository;

    public DenunciaServiceImpl(DenunciaRepository denunciaRepository) {
        this.denunciaRepository = denunciaRepository;
    }

    @Override
    @Transactional
    public DenunciaResponse registrar(DenunciaRequest request) {
        Denuncia denuncia = request.toEntity();
        if (denuncia.getFecha() == null) {
            denuncia.setFecha(LocalDateTime.now(ZoneId.of("America/Lima")).truncatedTo(ChronoUnit.SECONDS));
        } else {
            denuncia.setFecha(denuncia.getFecha().truncatedTo(ChronoUnit.SECONDS));
        }
        denuncia.setEstado(1);
        Denuncia guardada = denunciaRepository.save(denuncia);
        return DenunciaResponse.fromEntity(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenunciaResponse> listar(int offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("El parámetro offset no puede ser negativo");
        }
        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException("El parámetro limit debe estar entre 1 y 50");
        }
        return denunciaRepository.findAllWithOffsetLimit(offset, limit)
                .stream()
                .map(DenunciaResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DenunciaResponse obtenerPorId(Integer id) {
        return denunciaRepository.findById(id)
                .map(DenunciaResponse::fromEntity)
                .orElseThrow(() -> new DenunciaNotFoundException("Denuncia no encontrada con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenunciaResponse> listarPorCiudadano(String dni) {
        if (dni == null || !dni.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos");
        }
        return denunciaRepository.findByDniOrderByIdDesc(dni)
                .stream()
                .map(DenunciaResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public DenunciaResponse atender(Integer id) {
        Denuncia denuncia = denunciaRepository.findById(id)
                .orElseThrow(() -> new DenunciaNotFoundException("Denuncia no encontrada con id: " + id));
        denuncia.setEstado(0);
        Denuncia actualizada = denunciaRepository.save(denuncia);
        return DenunciaResponse.fromEntity(actualizada);
    }
}
