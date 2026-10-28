package com.RodriguezRuiz.denunciaservice.dto;

import com.RodriguezRuiz.denunciaservice.entity.Denuncia;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record DenunciaResponse(
        Integer id,
        String dni,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime fecha,
        String tipo,
        String direccion,
        String descripcion,
        Integer estado
) {
    public static DenunciaResponse fromEntity(Denuncia denuncia) {
        if (denuncia == null) {
            return null;
        }
        return new DenunciaResponse(
                denuncia.getId(),
                denuncia.getDni(),
                denuncia.getFecha() != null ? denuncia.getFecha().truncatedTo(ChronoUnit.SECONDS) : null,
                denuncia.getTipo(),
                denuncia.getDireccion(),
                denuncia.getDescripcion(),
                denuncia.getEstado()
        );
    }
}
