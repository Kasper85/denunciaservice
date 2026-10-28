package com.RodriguezRuiz.denunciaservice.dto;

import com.RodriguezRuiz.denunciaservice.entity.Denuncia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record DenunciaRequest(
        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener exactamente 8 dígitos")
        String dni,

        LocalDateTime fecha,

        @NotBlank(message = "El tipo es obligatorio")
        @Pattern(regexp = "^(?i)(ROBO|ASALTO|VANDALISMO|ACOSO|OTRO)$", message = "El tipo debe ser uno de: ROBO, ASALTO, VANDALISMO, ACOSO, OTRO")
        String tipo,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
        String direccion,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
        String descripcion
) {
    public Denuncia toEntity() {
        Denuncia d = new Denuncia();
        d.setDni(this.dni != null ? this.dni.trim() : null);
        d.setFecha(this.fecha);
        d.setTipo(this.tipo != null ? this.tipo.trim().toUpperCase() : null);
        d.setDireccion(this.direccion != null ? this.direccion.trim() : null);
        d.setDescripcion(this.descripcion != null ? this.descripcion.trim() : null);
        d.setEstado(1);
        return d;
    }
}
