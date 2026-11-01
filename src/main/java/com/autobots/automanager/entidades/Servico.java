package com.autobots.automanager.entidades;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Serviço oferecido pela empresa, como alinhamento ou troca de rodas. */
@Getter
@Setter
@Entity
public class Servico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false, updatable = false)
    private Empresa empresa;
    @Column(length = 150, nullable = false)
    private String nome;
    @Column(length = 255)
    private String descricao;
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal valor;
}
