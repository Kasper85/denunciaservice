package com.RodriguezRuiz.denunciaservice.service;

import com.RodriguezRuiz.denunciaservice.dto.DenunciaRequest;
import com.RodriguezRuiz.denunciaservice.dto.DenunciaResponse;
import com.RodriguezRuiz.denunciaservice.entity.Denuncia;
import com.RodriguezRuiz.denunciaservice.exception.DenunciaNotFoundException;
import com.RodriguezRuiz.denunciaservice.repository.DenunciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DenunciaServiceTest {

    @Mock
    private DenunciaRepository denunciaRepository;

    @InjectMocks
    private DenunciaServiceImpl denunciaService;

    private Denuncia sampleDenuncia;

    @BeforeEach
    void setUp() {
        sampleDenuncia = new Denuncia(1, "12345678", LocalDateTime.now(), "ROBO", "Av. Brasil 100", "Robo de mochila", 1);
    }

    @Test
    void testRegistrar() {
        when(denunciaRepository.save(any(Denuncia.class))).thenAnswer(invocation -> {
            Denuncia d = invocation.getArgument(0);
            d.setId(1);
            return d;
        });

        DenunciaRequest req = new DenunciaRequest("12345678", null, "robo", "Av. Brasil 100", "Robo de mochila");
        DenunciaResponse resp = denunciaService.registrar(req);

        assertNotNull(resp);
        assertEquals(1, resp.id());
        assertEquals("ROBO", resp.tipo(), "El tipo debe normalizarse a mayúsculas");
        assertEquals(1, resp.estado());
        assertNotNull(resp.fecha());
    }

    @Test
    void testListarPaginadoValido() {
        when(denunciaRepository.findAllWithOffsetLimit(0, 5)).thenReturn(List.of(sampleDenuncia));

        List<DenunciaResponse> lista = denunciaService.listar(0, 5);
        assertEquals(1, lista.size());
        assertEquals(sampleDenuncia.getId(), lista.get(0).id());
    }

    @Test
    void testListarPaginadoOffsetInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> denunciaService.listar(-1, 5));
        assertTrue(ex.getMessage().contains("offset"));
    }

    @Test
    void testListarPaginadoLimitInvalido() {
        assertThrows(IllegalArgumentException.class, () -> denunciaService.listar(0, 0));
        assertThrows(IllegalArgumentException.class, () -> denunciaService.listar(0, 51));
    }

    @Test
    void testObtenerPorIdExitoso() {
        when(denunciaRepository.findById(1)).thenReturn(Optional.of(sampleDenuncia));

        DenunciaResponse resp = denunciaService.obtenerPorId(1);
        assertNotNull(resp);
        assertEquals(1, resp.id());
    }

    @Test
    void testObtenerPorIdNoExiste() {
        when(denunciaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(DenunciaNotFoundException.class, () -> denunciaService.obtenerPorId(99));
    }

    @Test
    void testListarPorCiudadanoValido() {
        when(denunciaRepository.findByDniOrderByIdDesc("12345678")).thenReturn(List.of(sampleDenuncia));

        List<DenunciaResponse> lista = denunciaService.listarPorCiudadano("12345678");
        assertEquals(1, lista.size());
    }

    @Test
    void testListarPorCiudadanoDniInvalido() {
        assertThrows(IllegalArgumentException.class, () -> denunciaService.listarPorCiudadano("123"));
        assertThrows(IllegalArgumentException.class, () -> denunciaService.listarPorCiudadano("123456789"));
        assertThrows(IllegalArgumentException.class, () -> denunciaService.listarPorCiudadano("abcdefgh"));
    }

    @Test
    void testAtenderDenuncia() {
        when(denunciaRepository.findById(1)).thenReturn(Optional.of(sampleDenuncia));
        when(denunciaRepository.save(any(Denuncia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DenunciaResponse resp = denunciaService.atender(1);
        assertEquals(0, resp.estado());
        verify(denunciaRepository).save(sampleDenuncia);
    }

    @Test
    void testAtenderDenunciaNoExiste() {
        when(denunciaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(DenunciaNotFoundException.class, () -> denunciaService.atender(99));
    }
}
