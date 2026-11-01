package com.autobots.automanager.entidades;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Unidade comercial que presta serviços de manutenção e vende mercadorias.
 *
 * <p>Usuários, mercadorias, serviços e vendas apontam para a empresa. Ela não os carrega em coleções para que a
 * consulta de uma empresa não traga todo o seu movimento.
 */
@Getter
@Setter
@Entity
public class Empresa implements Titular {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String razaoSocial;
    @Column(length = 150)
    private String nomeFantasia;
    @Column(nullable = false, updatable = false)
    private Instant cadastro;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "endereco_id", unique = true)
    private Endereco endereco;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "empresa_id")
    @OrderBy("id")
    private List<Telefone> telefones = new ArrayList<>();
}
