package com.autobots.automanager.entidades;

import java.time.Instant;
import com.autobots.automanager.enumeracoes.TipoCredencial;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Credencial de acesso de um usuário ao sistema.
 *
 * <p>Cada forma de acesso é uma subclasse, gravada em tabela própria ({@link InheritanceType#JOINED}).
 */
@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Credencial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private Instant criacao;
    private Instant ultimoAcesso;
    @Column(nullable = false)
    private boolean inativo;

    /**
     * Informa a forma de acesso desta credencial.
     *
     * @return tipo correspondente à subclasse
     */
    public abstract TipoCredencial getTipo();
}
