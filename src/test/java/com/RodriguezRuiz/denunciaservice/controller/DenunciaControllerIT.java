package com.RodriguezRuiz.denunciaservice.controller;

import com.RodriguezRuiz.denunciaservice.entity.Denuncia;
import com.RodriguezRuiz.denunciaservice.repository.DenunciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DenunciaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DenunciaRepository denunciaRepository;

    @BeforeEach
    void setUp() {
        denunciaRepository.deleteAll();
    }

    @Test
    void testCrearDenunciaConYSinSlash() throws Exception {
        String json1 = """
                {
                  "dni": "45678912",
                  "tipo": "ROBO",
                  "direccion": "Av. Balta 123, Chiclayo",
                  "descripcion": "Me arrebataron el celular cerca del mercado"
                }
                """;

        mockMvc.perform(post("/denuncias/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.dni").value("45678912"))
                .andExpect(jsonPath("$.tipo").value("ROBO"))
                .andExpect(jsonPath("$.direccion").value("Av. Balta 123, Chiclayo"))
                .andExpect(jsonPath("$.descripcion").value("Me arrebataron el celular cerca del mercado"))
                .andExpect(jsonPath("$.estado").value(1))
                .andExpect(jsonPath("$.fecha").isNotEmpty());

        String json2 = """
                {
                  "dni": "87654321",
                  "tipo": "asALto",
                  "direccion": "Calle Real 456",
                  "descripcion": "Asalto a mano armada"
                }
                """;

        mockMvc.perform(post("/denuncias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ASALTO"))
                .andExpect(jsonPath("$.estado").value(1));
    }

    @Test
    void testCrearDenunciaDniInvalido() throws Exception {
        String json = """
                {
                  "dni": "12345",
                  "tipo": "ROBO",
                  "direccion": "Av. Balta 123",
                  "descripcion": "Descripcion de prueba"
                }
                """;

        mockMvc.perform(post("/denuncias/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("Datos inválidos"))
                .andExpect(jsonPath("$.detalles.dni").isNotEmpty());
    }

    @Test
    void testCrearDenunciaTipoInvalido() throws Exception {
        String json = """
                {
                  "dni": "12345678",
                  "tipo": "SECUESTRO",
                  "direccion": "Av. Balta 123",
                  "descripcion": "Descripcion de prueba"
                }
                """;

        mockMvc.perform(post("/denuncias/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("Datos inválidos"))
                .andExpect(jsonPath("$.detalles.tipo").isNotEmpty());
    }

    @Test
    void testListarPaginadoConSieteRegistros() throws Exception {
        for (int i = 1; i <= 7; i++) {
            Denuncia d = new Denuncia(null, "1234567" + (i % 10), LocalDateTime.now(), "ROBO", "Dir " + i, "Desc " + i, 1);
            denunciaRepository.save(d);
        }

        mockMvc.perform(get("/denuncias/?offset=0&limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));

        mockMvc.perform(get("/denuncias?offset=5&limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testListarPaginadoLimitInvalido() throws Exception {
        mockMvc.perform(get("/denuncias/?offset=0&limit=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/denuncias/?offset=0&limit=51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testListarPaginadoOffsetInvalido() throws Exception {
        mockMvc.perform(get("/denuncias/?offset=-1&limit=5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testObtenerPorIdExitosoYNotFound() throws Exception {
        Denuncia d = new Denuncia(null, "77889900", LocalDateTime.now(), "VANDALISMO", "Parque central", "Pintadas", 1);
        Denuncia saved = denunciaRepository.save(d);

        mockMvc.perform(get("/denuncias/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.dni").value("77889900"));

        mockMvc.perform(get("/denuncias/" + saved.getId() + "/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()));

        mockMvc.perform(get("/denuncias/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void testCiudadanoConResultadosYVacio() throws Exception {
        Denuncia d = new Denuncia(null, "98765432", LocalDateTime.now(), "ACOSO", "Av. Grau 500", "Acoso callejero", 1);
        denunciaRepository.save(d);

        mockMvc.perform(get("/denuncias/ciudadano/98765432"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dni").value("98765432"));

        mockMvc.perform(get("/denuncias/ciudadano/98765432/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/denuncias/ciudadano/00000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testCiudadanoDniInvalido() throws Exception {
        mockMvc.perform(get("/denuncias/ciudadano/1234567"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testAtenderDenunciaExitosoIdempotenteYNotFound() throws Exception {
        Denuncia d = new Denuncia(null, "44556677", LocalDateTime.now(), "OTRO", "Calle 10", "Ruido molesto", 1);
        Denuncia saved = denunciaRepository.save(d);

        mockMvc.perform(put("/denuncias/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.estado").value(0));

        // Idempotente: volver a llamar debe responder 200 con estado 0
        mockMvc.perform(put("/denuncias/" + saved.getId() + "/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.estado").value(0));

        mockMvc.perform(put("/denuncias/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
