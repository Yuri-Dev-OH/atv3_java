package com.autobots.automanager.entidades;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Mercadoria vendida, com nome, quantidade e preço registrados no momento da venda. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ItemMercadoria {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mercadoria_id", nullable = false)
    private Mercadoria mercadoria;
    @Column(length = 150, nullable = false)
    private String nome;
    @Column(nullable = false)
    private int quantidade;
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal valorUnitario;
}
