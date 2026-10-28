package com.RodriguezRuiz.denunciaservice.dto;

import com.RodriguezRuiz.denunciaservice.entity.Denuncia;
import com.RodriguezRuiz.denunciaservice.exception.DenunciaNotFoundException;
import com.RodriguezRuiz.denunciaservice.exception.ErrorResponse;
import com.RodriguezRuiz.denunciaservice.exception.GlobalExceptionHandler;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DtoAndExceptionTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testDenunciaRequestValido() {
        DenunciaRequest req = new DenunciaRequest("12345678", null, "ROBO", "Av. Peru 123", "Robo");
        var violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testDenunciaRequestInvalido() {
        DenunciaRequest req = new DenunciaRequest("123", null, "INVALIDO", "", "");
        var violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dni")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("tipo")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("direccion")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("descripcion")));
    }

    @Test
    void testDenunciaResponseFromEntity() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 28, 9, 15, 0);
        Denuncia denuncia = new Denuncia(1, "12345678", now, "ROBO", "Calle 1", "Desc", 1);
        DenunciaResponse res = DenunciaResponse.fromEntity(denuncia);

        assertEquals(1, res.id());
        assertEquals("12345678", res.dni());
        assertEquals(now, res.fecha());
        assertEquals("ROBO", res.tipo());
        assertEquals("Calle 1", res.direccion());
        assertEquals("Desc", res.descripcion());
        assertEquals(1, res.estado());
    }

    @Test
    void testGlobalExceptionHandlerNotFound() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        DenunciaNotFoundException ex = new DenunciaNotFoundException("Denuncia no encontrada con id: 5");
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("Denuncia no encontrada con id: 5", response.getBody().mensaje());
        assertNull(response.getBody().detalles());
    }
}
