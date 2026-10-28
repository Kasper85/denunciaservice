package com.RodriguezRuiz.denunciaservice.service;

import com.RodriguezRuiz.denunciaservice.dto.DenunciaRequest;
import com.RodriguezRuiz.denunciaservice.dto.DenunciaResponse;

import java.util.List;

public interface DenunciaService {

    DenunciaResponse registrar(DenunciaRequest request);

    List<DenunciaResponse> listar(int offset, int limit);

    DenunciaResponse obtenerPorId(Integer id);

    List<DenunciaResponse> listarPorCiudadano(String dni);

    DenunciaResponse atender(Integer id);
}
