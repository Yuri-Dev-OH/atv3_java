package com.autobots.automanager.repositorios;

import java.util.Optional;
import com.autobots.automanager.entidades.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepositorio extends JpaRepository<Veiculo, Long> {
    Optional<Veiculo> findByIdAndProprietarioId(long id, long proprietarioId);
}
