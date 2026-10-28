package com.RodriguezRuiz.denunciaservice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class DenunciaTest {

    @Test
    void prePersistDeberiaAsignarFechaYEstadoPorDefecto() {
        Denuncia denuncia = new Denuncia();
        denuncia.setDni("12345678");
        denuncia.setTipo("ROBO");
        denuncia.setDireccion("Av. Principal 123");
        denuncia.setDescripcion("Robo de celular");

        denuncia.prePersist();

        assertNotNull(denuncia.getFecha());
        assertEquals(0, denuncia.getFecha().getNano(), "La fecha debe estar truncada a segundos");
        assertEquals(Integer.valueOf(1), denuncia.getEstado());
    }

    @Test
    void prePersistNoDeberiaSobrescribirFechaNiEstadoSiYaEstanDefinidos() {
        Denuncia denuncia = new Denuncia();
        LocalDateTime fechaManual = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
        denuncia.setFecha(fechaManual);
        denuncia.setEstado(0);

        denuncia.prePersist();

        assertEquals(fechaManual, denuncia.getFecha());
        assertEquals(Integer.valueOf(0), denuncia.getEstado());
    }
}
