package com.autobots.automanager.repositorios;

import java.util.Optional;
import com.autobots.automanager.entidades.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepositorio extends JpaRepository<Servico, Long> {
    Optional<Servico> findByIdAndEmpresaId(long id, long empresaId);
    Page<Servico> findByEmpresaId(long empresaId, Pageable pagina);
}
