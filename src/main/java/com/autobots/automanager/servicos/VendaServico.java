package com.autobots.automanager.servicos;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import com.autobots.automanager.dto.Dados.ItemDados;
import com.autobots.automanager.dto.Dados.VendaDados;
import com.autobots.automanager.dto.Respostas.VendaResposta;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.erros.Conflito;
import com.autobots.automanager.erros.RecursoNaoEncontrado;
import com.autobots.automanager.erros.RegraNegocio;
import com.autobots.automanager.modelo.EmpresaSelecionador;
import com.autobots.automanager.repositorios.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro de vendas de mercadorias e serviços.
 *
 * <p>Regras: cliente com perfil {@code CLIENTE} e funcionário com perfil {@code FUNCIONARIO}, ambos da empresa; veículo,
 * quando informado, do próprio cliente; itens do catálogo da empresa, sem repetição. A venda baixa o estoque das
 * mercadorias; atualizá-la devolve o estoque anterior antes de baixar o novo e excluí-la devolve o estoque. As
 * mercadorias são bloqueadas em ordem de id, evitando que vendas simultâneas vendam o mesmo item.
 */
@Service
@Transactional(readOnly = true)
public class VendaServico {
    private final VendaRepositorio repositorio;
    private final UsuarioRepositorio usuarios;
    private final VeiculoRepositorio veiculos;
    private final MercadoriaRepositorio mercadorias;
    private final ServicoRepositorio servicos;
    private final EmpresaSelecionador empresas;

    public VendaServico(VendaRepositorio repositorio, UsuarioRepositorio usuarios, VeiculoRepositorio veiculos,
            MercadoriaRepositorio mercadorias, ServicoRepositorio servicos, EmpresaSelecionador empresas) {
        this.repositorio = repositorio; this.usuarios = usuarios; this.veiculos = veiculos; this.mercadorias = mercadorias;
        this.servicos = servicos; this.empresas = empresas;
    }

    /**
     * Lista as vendas da empresa, com filtros opcionais.
     *
     * @param clienteId somente compras deste cliente
     * @param funcionarioId somente vendas atendidas por este funcionário
     * @param veiculoId somente serviços e vendas deste veículo
     */
    public Page<VendaResposta> listar(long empresaId, Long clienteId, Long funcionarioId, Long veiculoId, int pagina, int tamanho) {
        empresas.selecionar(empresaId);
        return repositorio.listar(empresaId, clienteId, funcionarioId, veiculoId, PageRequest.of(pagina, tamanho, Sort.by("id")))
            .map(VendaResposta::de);
    }
    public VendaResposta obter(long empresaId, long id) {
        return VendaResposta.de(repositorio.findByIdAndEmpresaId(id, empresaId).orElseThrow(VendaServico::naoEncontrada));
    }

    @Transactional
    public VendaResposta cadastrar(long empresaId, VendaDados dados) {
        var venda = new Venda();
        venda.setEmpresa(empresas.selecionar(empresaId));
        venda.setCadastro(Instant.now().truncatedTo(ChronoUnit.MICROS));
        preencher(venda, empresaId, dados);
        return VendaResposta.de(repositorio.saveAndFlush(venda));
    }
    @Transactional
    public VendaResposta atualizar(long empresaId, long id, VendaDados dados) {
        var venda = bloquear(empresaId, id);
        devolverEstoque(venda, empresaId);
        preencher(venda, empresaId, dados);
        repositorio.flush();
        return VendaResposta.de(venda);
    }
    /** Cancela a venda e devolve as mercadorias ao estoque. */
    @Transactional
    public void excluir(long empresaId, long id) {
        var venda = bloquear(empresaId, id);
        devolverEstoque(venda, empresaId);
        repositorio.delete(venda);
        repositorio.flush();
    }

    private Venda bloquear(long empresaId, long id) {
        return repositorio.bloquear(empresaId, id).orElseThrow(VendaServico::naoEncontrada);
    }
    private static RecursoNaoEncontrado naoEncontrada() { return new RecursoNaoEncontrado("Venda não encontrada nesta empresa."); }

    private void preencher(Venda venda, long empresaId, VendaDados dados) {
        semRepeticao(dados.mercadorias(), "Mercadoria repetida na venda. Informe a quantidade total em um único item.");
        semRepeticao(dados.servicos(), "Serviço repetido na venda. Informe a quantidade total em um único item.");
        venda.setIdentificacao(dados.identificacao());
        var cliente = participante(empresaId, dados.clienteId(), PerfilUsuario.CLIENTE, "Cliente");
        venda.setCliente(cliente);
        venda.setFuncionario(participante(empresaId, dados.funcionarioId(), PerfilUsuario.FUNCIONARIO, "Funcionário"));
        venda.setVeiculo(dados.veiculoId() == null ? null : veiculos.findByIdAndProprietarioId(dados.veiculoId(), cliente.getId())
            .orElseThrow(() -> new RegraNegocio("Veículo não encontrado entre os veículos do cliente.")));
        venda.getMercadorias().clear();
        for (var item : ordenados(dados.mercadorias())) {
            var mercadoria = mercadorias.bloquear(empresaId, item.id())
                .orElseThrow(() -> new RegraNegocio("Mercadoria " + item.id() + " não encontrada nesta empresa."));
            if (mercadoria.getQuantidade() < item.quantidade())
                throw new Conflito("Estoque insuficiente de '" + mercadoria.getNome() + "': disponível "
                    + mercadoria.getQuantidade() + ", pedido " + item.quantidade() + ".");
            mercadoria.setQuantidade(mercadoria.getQuantidade() - item.quantidade());
            venda.getMercadorias().add(new ItemMercadoria(mercadoria, mercadoria.getNome(), item.quantidade(), mercadoria.getValor()));
        }
        venda.getServicos().clear();
        for (var item : ordenados(dados.servicos())) {
            var servico = servicos.findByIdAndEmpresaId(item.id(), empresaId)
                .orElseThrow(() -> new RegraNegocio("Serviço " + item.id() + " não encontrado nesta empresa."));
            venda.getServicos().add(new ItemServico(servico, servico.getNome(), item.quantidade(), servico.getValor()));
        }
    }
    private void devolverEstoque(Venda venda, long empresaId) {
        venda.getMercadorias().stream().map(item -> item.getMercadoria().getId()).sorted().distinct()
            .forEach(id -> mercadorias.bloquear(empresaId, id));
        venda.getMercadorias().forEach(item ->
            item.getMercadoria().setQuantidade(item.getMercadoria().getQuantidade() + item.getQuantidade()));
    }
    private Usuario participante(long empresaId, long id, PerfilUsuario perfil, String papel) {
        var usuario = usuarios.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RegraNegocio(papel + " não encontrado nesta empresa."));
        if (!usuario.getPerfis().contains(perfil))
            throw new RegraNegocio(papel + " informado não tem o perfil " + perfil + ".");
        return usuario;
    }
    private static List<ItemDados> ordenados(List<ItemDados> itens) {
        return itens.stream().sorted(Comparator.comparing(ItemDados::id)).toList();
    }
    private static void semRepeticao(List<ItemDados> itens, String mensagem) {
        var ids = new HashSet<Long>();
        if (!itens.stream().allMatch(item -> ids.add(item.id()))) throw new RegraNegocio(mensagem);
    }
}
