package com.autobots.automanager.entidades;

import java.time.LocalDate;
import com.autobots.automanager.enumeracoes.TipoDocumento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Documento de um usuário.
 *
 * <p>{@code empresaId} repete a empresa do usuário para que o banco garanta tipo e número únicos por empresa: a mesma
 * pessoa pode ser cliente de duas unidades.
 */
@Getter
@Setter
@Entity
public class Documento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 30, nullable = false)
    private TipoDocumento tipo;
    @Column(length = 50, nullable = false)
    private String numero;
    private LocalDate dataEmissao;
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;
}
