package com.autobots.automanager.entidades;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Venda de mercadorias e serviços a um cliente, atendida por um funcionário e opcionalmente ligada a um veículo.
 *
 * <p>Os itens guardam o preço da época; alterar o catálogo depois não muda vendas já registradas.
 */
@Getter
@Setter
@Entity
public class Venda {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false, updatable = false)
    private Empresa empresa;
    @Column(length = 50, nullable = false)
    private String identificacao;
    @Column(nullable = false, updatable = false)
    private Instant cadastro;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Usuario funcionario;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;
    @ElementCollection
    @CollectionTable(name = "venda_mercadoria", joinColumns = @JoinColumn(name = "venda_id"))
    @OrderColumn(name = "posicao")
    private List<ItemMercadoria> mercadorias = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "venda_servico", joinColumns = @JoinColumn(name = "venda_id"))
    @OrderColumn(name = "posicao")
    private List<ItemServico> servicos = new ArrayList<>();
}
