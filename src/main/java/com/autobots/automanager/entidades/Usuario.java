package com.autobots.automanager.entidades;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Pessoa ou organização associada a uma empresa como cliente, funcionário ou fornecedor.
 *
 * <p>Substitui o {@code Cliente} da ATVII: um cliente agora é um usuário com o perfil {@code CLIENTE}.
 */
@Getter
@Setter
@Entity
public class Usuario implements Titular {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false, updatable = false)
    private Empresa empresa;
    @Column(nullable = false, length = 150)
    private String nome;
    @Column(length = 150)
    private String nomeSocial;
    private LocalDate dataNascimento;
    @Column(nullable = false, updatable = false)
    private Instant dataCadastro;
    @ElementCollection
    @CollectionTable(name = "usuario_perfil", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "perfil", length = 20, nullable = false)
    private Set<PerfilUsuario> perfis = new HashSet<>();
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "endereco_id", unique = true)
    private Endereco endereco;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "usuario_id")
    @OrderBy("id")
    private List<Telefone> telefones = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "usuario_id")
    @OrderBy("id")
    private List<Documento> documentos = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "usuario_id")
    @OrderBy("id")
    private List<Email> emails = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "usuario_id")
    @OrderBy("id")
    private List<Credencial> credenciais = new ArrayList<>();
    @OneToMany(mappedBy = "proprietario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Veiculo> veiculos = new ArrayList<>();
}
