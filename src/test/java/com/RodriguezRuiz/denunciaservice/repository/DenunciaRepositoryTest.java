package com.RodriguezRuiz.denunciaservice.repository;

import com.RodriguezRuiz.denunciaservice.entity.Denuncia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DenunciaRepositoryTest {

    @Autowired
    private DenunciaRepository denunciaRepository;

    @BeforeEach
    void setUp() {
        denunciaRepository.deleteAll();
    }

    @Test
    void testFindAllWithOffsetLimit() {
        for (int i = 1; i <= 7; i++) {
            Denuncia d = new Denuncia();
            d.setDni("1234567" + (i % 10));
            d.setTipo("ROBO");
            d.setDireccion("Direccion " + i);
            d.setDescripcion("Descripcion " + i);
            d.setFecha(LocalDateTime.of(2026, 10, i, 10, 0, 0));
            d.setEstado(1);
            denunciaRepository.save(d);
        }

        List<Denuncia> pagina1 = denunciaRepository.findAllWithOffsetLimit(0, 5);
        assertEquals(5, pagina1.size());
        assertTrue(pagina1.get(0).getId() > pagina1.get(1).getId(), "Debe estar ordenado por id DESC");

        List<Denuncia> pagina2 = denunciaRepository.findAllWithOffsetLimit(5, 5);
        assertEquals(2, pagina2.size());
    }

    @Test
    void testFindByDniOrderByIdDesc() {
        Denuncia d1 = new Denuncia(null, "87654321", LocalDateTime.now(), "ASALTO", "Calle 1", "Desc 1", 1);
        Denuncia d2 = new Denuncia(null, "87654321", LocalDateTime.now(), "ROBO", "Calle 2", "Desc 2", 1);
        Denuncia d3 = new Denuncia(null, "11223344", LocalDateTime.now(), "OTRO", "Calle 3", "Desc 3", 1);

        denunciaRepository.save(d1);
        denunciaRepository.save(d2);
        denunciaRepository.save(d3);

        List<Denuncia> resultados = denunciaRepository.findByDniOrderByIdDesc("87654321");
        assertEquals(2, resultados.size());
        assertEquals("87654321", resultados.get(0).getDni());
        assertEquals("87654321", resultados.get(1).getDni());
        assertTrue(resultados.get(0).getId() > resultados.get(1).getId());
    }
}
