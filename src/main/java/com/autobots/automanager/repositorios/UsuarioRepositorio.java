package com.autobots.automanager.repositorios;

import java.util.Optional;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByIdAndEmpresaId(long id, long empresaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id and u.empresa.id = :empresaId")
    Optional<Usuario> bloquear(@Param("empresaId") long empresaId, @Param("id") long id);

    Page<Usuario> findByEmpresaId(long empresaId, Pageable pagina);

    @Query("select u from Usuario u where u.empresa.id = :empresaId and :perfil member of u.perfis")
    Page<Usuario> listarPorPerfil(@Param("empresaId") long empresaId, @Param("perfil") PerfilUsuario perfil, Pageable pagina);
}
