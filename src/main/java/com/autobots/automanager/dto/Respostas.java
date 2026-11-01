package com.autobots.automanager.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.enumeracoes.*;
import org.springframework.hateoas.server.core.Relation;

/**
 * Representações devolvidas pela API.
 *
 * <p>{@link Relation} define os nomes usados pelo HAL em {@code _embedded}, evitando nomes gerados como
 * {@code documentoRespostaList}. Senhas nunca fazem parte das respostas.
 */
public final class Respostas {
    private Respostas() {}
    private static <E, R> List<R> mapear(List<E> itens, java.util.function.Function<E, R> conversor) {
        return itens.stream().map(conversor).toList();
    }

    @Relation(itemRelation = "documento", collectionRelation = "documentos")
    public record DocumentoResposta(Long id, TipoDocumento tipo, String numero, LocalDate dataEmissao) {
        public static DocumentoResposta de(Documento d) {
            return new DocumentoResposta(d.getId(), d.getTipo(), d.getNumero(), d.getDataEmissao());
        }
    }
    @Relation(itemRelation = "telefone", collectionRelation = "telefones")
    public record TelefoneResposta(Long id, String ddd, String numero) {
        public static TelefoneResposta de(Telefone t) { return new TelefoneResposta(t.getId(), t.getDdd(), t.getNumero()); }
    }
    @Relation(itemRelation = "endereco", collectionRelation = "enderecos")
    public record EnderecoResposta(Long id, String estado, String cidade, String bairro, String rua, String numero,
                                   String codigoPostal, String informacoesAdicionais) {
        public static EnderecoResposta de(Endereco e) {
            return e == null ? null : new EnderecoResposta(e.getId(), e.getEstado(), e.getCidade(), e.getBairro(),
                e.getRua(), e.getNumero(), e.getCodigoPostal(), e.getInformacoesAdicionais());
        }
    }
    @Relation(itemRelation = "email", collectionRelation = "emails")
    public record EmailResposta(Long id, String endereco) {
        public static EmailResposta de(Email e) { return new EmailResposta(e.getId(), e.getEndereco()); }
    }
    @Relation(itemRelation = "empresa", collectionRelation = "empresas")
    public record EmpresaResposta(Long id, String razaoSocial, String nomeFantasia, Instant cadastro,
                                  EnderecoResposta endereco, List<TelefoneResposta> telefones) {
        public static EmpresaResposta de(Empresa e) {
            return new EmpresaResposta(e.getId(), e.getRazaoSocial(), e.getNomeFantasia(), e.getCadastro(),
                EnderecoResposta.de(e.getEndereco()), mapear(e.getTelefones(), TelefoneResposta::de));
        }
    }
    @Relation(itemRelation = "usuario", collectionRelation = "usuarios")
    public record UsuarioResposta(Long id, String nome, String nomeSocial, LocalDate dataNascimento, Instant dataCadastro,
                                  List<PerfilUsuario> perfis, EnderecoResposta endereco, List<TelefoneResposta> telefones,
                                  List<DocumentoResposta> documentos, List<EmailResposta> emails) {
        public static UsuarioResposta de(Usuario u) {
            return new UsuarioResposta(u.getId(), u.getNome(), u.getNomeSocial(), u.getDataNascimento(), u.getDataCadastro(),
                u.getPerfis().stream().sorted().toList(), EnderecoResposta.de(u.getEndereco()),
                mapear(u.getTelefones(), TelefoneResposta::de), mapear(u.getDocumentos(), DocumentoResposta::de),
                mapear(u.getEmails(), EmailResposta::de));
        }
        public boolean possui(PerfilUsuario perfil) { return perfis.contains(perfil); }
    }
    @Relation(itemRelation = "credencial", collectionRelation = "credenciais")
    public record CredencialResposta(Long id, TipoCredencial tipo, String nomeUsuario, Long codigo, Instant criacao,
                                     Instant ultimoAcesso, boolean inativo) {
        public static CredencialResposta de(Credencial c) {
            String nomeUsuario = c instanceof CredencialUsuarioSenha s ? s.getNomeUsuario() : null;
            Long codigo = c instanceof CredencialCodigoBarra b ? b.getCodigo() : null;
            return new CredencialResposta(c.getId(), c.getTipo(), nomeUsuario, codigo, c.getCriacao(), c.getUltimoAcesso(),
                c.isInativo());
        }
    }
    @Relation(itemRelation = "veiculo", collectionRelation = "veiculos")
    public record VeiculoResposta(Long id, TipoVeiculo tipo, String modelo, String placa) {
        public static VeiculoResposta de(Veiculo v) { return new VeiculoResposta(v.getId(), v.getTipo(), v.getModelo(), v.getPlaca()); }
    }
    @Relation(itemRelation = "mercadoria", collectionRelation = "mercadorias")
    public record MercadoriaResposta(Long id, String nome, String descricao, LocalDate fabricacao, LocalDate validade,
                                     Instant cadastro, long quantidade, BigDecimal valor, Long fornecedorId) {
        public static MercadoriaResposta de(Mercadoria m) {
            return new MercadoriaResposta(m.getId(), m.getNome(), m.getDescricao(), m.getFabricacao(), m.getValidade(),
                m.getCadastro(), m.getQuantidade(), m.getValor(), m.getFornecedor() == null ? null : m.getFornecedor().getId());
        }
    }
    @Relation(itemRelation = "servico", collectionRelation = "servicos")
    public record ServicoResposta(Long id, String nome, String descricao, BigDecimal valor) {
        public static ServicoResposta de(Servico s) { return new ServicoResposta(s.getId(), s.getNome(), s.getDescricao(), s.getValor()); }
    }

    /** Usuário citado em uma venda (cliente ou funcionário). */
    public record ParticipanteResposta(Long id, String nome) {
        public static ParticipanteResposta de(Usuario u) { return new ParticipanteResposta(u.getId(), u.getNome()); }
    }
    /** Item vendido; {@code id} é o da mercadoria ou do serviço no catálogo. */
    public record ItemResposta(Long id, String nome, int quantidade, BigDecimal valorUnitario, BigDecimal subtotal) {
        static ItemResposta de(Long id, String nome, int quantidade, BigDecimal valorUnitario) {
            return new ItemResposta(id, nome, quantidade, valorUnitario, valorUnitario.multiply(BigDecimal.valueOf(quantidade)));
        }
    }
    @Relation(itemRelation = "venda", collectionRelation = "vendas")
    public record VendaResposta(Long id, String identificacao, Instant cadastro, ParticipanteResposta cliente,
                                ParticipanteResposta funcionario, VeiculoResposta veiculo, List<ItemResposta> mercadorias,
                                List<ItemResposta> servicos, BigDecimal valorTotal) {
        public static VendaResposta de(Venda v) {
            var mercadorias = mapear(v.getMercadorias(), i ->
                ItemResposta.de(i.getMercadoria().getId(), i.getNome(), i.getQuantidade(), i.getValorUnitario()));
            var servicos = mapear(v.getServicos(), i ->
                ItemResposta.de(i.getServico().getId(), i.getNome(), i.getQuantidade(), i.getValorUnitario()));
            var total = java.util.stream.Stream.concat(mercadorias.stream(), servicos.stream())
                .map(ItemResposta::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            return new VendaResposta(v.getId(), v.getIdentificacao(), v.getCadastro(), ParticipanteResposta.de(v.getCliente()),
                ParticipanteResposta.de(v.getFuncionario()), v.getVeiculo() == null ? null : VeiculoResposta.de(v.getVeiculo()),
                mercadorias, servicos, total);
        }
    }
}
