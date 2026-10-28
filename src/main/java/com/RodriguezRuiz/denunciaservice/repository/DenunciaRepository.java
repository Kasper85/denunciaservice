package com.RodriguezRuiz.denunciaservice.repository;

import com.RodriguezRuiz.denunciaservice.entity.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, Integer> {

    @Query(value = "SELECT * FROM denuncias ORDER BY id DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Denuncia> findAllWithOffsetLimit(@Param("offset") int offset, @Param("limit") int limit);

    List<Denuncia> findByDniOrderByIdDesc(String dni);
}
