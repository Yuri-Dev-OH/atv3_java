package com.autobots.automanager.entidades;

import com.autobots.automanager.enumeracoes.TipoVeiculo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Veículo de um cliente. A placa é única por empresa ({@code empresaId} repete a empresa do proprietário). */
@Getter
@Setter
@Entity
public class Veiculo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 10, nullable = false)
    private TipoVeiculo tipo;
    @Column(length = 100, nullable = false)
    private String modelo;
    @Column(length = 7, nullable = false)
    private String placa;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proprietario_id", nullable = false, updatable = false)
    private Usuario proprietario;
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;
}
