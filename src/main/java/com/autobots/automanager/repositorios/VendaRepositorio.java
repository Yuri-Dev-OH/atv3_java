package com.autobots.automanager.repositorios;

import java.util.Optional;
import com.autobots.automanager.entidades.Venda;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VendaRepositorio extends JpaRepository<Venda, Long> {
    Optional<Venda> findByIdAndEmpresaId(long id, long empresaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Venda v where v.id = :id and v.empresa.id = :empresaId")
    Optional<Venda> bloquear(@Param("empresaId") long empresaId, @Param("id") long id);

    @Query("""
        select v from Venda v where v.empresa.id = :empresaId
          and (:clienteId is null or v.cliente.id = :clienteId)
          and (:funcionarioId is null or v.funcionario.id = :funcionarioId)
          and (:veiculoId is null or v.veiculo.id = :veiculoId)""")
    Page<Venda> listar(@Param("empresaId") long empresaId, @Param("clienteId") Long clienteId,
        @Param("funcionarioId") Long funcionarioId, @Param("veiculoId") Long veiculoId, Pageable pagina);

    boolean existsByClienteId(long clienteId);
    boolean existsByFuncionarioId(long funcionarioId);
}
