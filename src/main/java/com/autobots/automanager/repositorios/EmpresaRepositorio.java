package com.autobots.automanager.repositorios;

import java.util.Optional;
import com.autobots.automanager.entidades.Empresa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpresaRepositorio extends JpaRepository<Empresa, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Empresa e where e.id = :id")
    Optional<Empresa> bloquearPorId(@Param("id") long id);
}
