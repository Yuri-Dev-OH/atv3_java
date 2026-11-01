package com.autobots.automanager.entidades;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Item em estoque na empresa, opcionalmente associado ao usuário fornecedor. */
@Getter
@Setter
@Entity
public class Mercadoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false, updatable = false)
    private Empresa empresa;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Usuario fornecedor;
    @Column(length = 150, nullable = false)
    private String nome;
    @Column(length = 255)
    private String descricao;
    private LocalDate fabricacao;
    private LocalDate validade;
    @Column(nullable = false, updatable = false)
    private Instant cadastro;
    @Column(nullable = false)
    private long quantidade;
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal valor;
}
