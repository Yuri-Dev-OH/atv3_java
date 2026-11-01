package com.autobots.automanager.repositorios;

import java.util.Optional;
import com.autobots.automanager.entidades.Mercadoria;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MercadoriaRepositorio extends JpaRepository<Mercadoria, Long> {
    Optional<Mercadoria> findByIdAndEmpresaId(long id, long empresaId);

    /** Bloqueia a mercadoria para que vendas simultâneas não baixem o mesmo estoque. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mercadoria m where m.id = :id and m.empresa.id = :empresaId")
    Optional<Mercadoria> bloquear(@Param("empresaId") long empresaId, @Param("id") long id);

    @Query("select m from Mercadoria m where m.empresa.id = :empresaId and (:fornecedorId is null or m.fornecedor.id = :fornecedorId)")
    Page<Mercadoria> listar(@Param("empresaId") long empresaId, @Param("fornecedorId") Long fornecedorId, Pageable pagina);

    boolean existsByFornecedorId(long fornecedorId);
}
