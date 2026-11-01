package com.autobots.automanager.servicos;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import com.autobots.automanager.dto.Dados.MercadoriaDados;
import com.autobots.automanager.dto.Respostas.MercadoriaResposta;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.erros.RecursoNaoEncontrado;
import com.autobots.automanager.erros.RegraNegocio;
import com.autobots.automanager.modelo.EmpresaSelecionador;
import com.autobots.automanager.modelo.MercadoriaAtualizador;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD do estoque de mercadorias de uma empresa. */
@Service
@Transactional(readOnly = true)
public class MercadoriaServico {
    private final MercadoriaRepositorio repositorio;
    private final UsuarioRepositorio usuarios;
    private final EmpresaSelecionador empresas;
    private final MercadoriaAtualizador atualizador;
    public MercadoriaServico(MercadoriaRepositorio repositorio, UsuarioRepositorio usuarios, EmpresaSelecionador empresas,
            MercadoriaAtualizador atualizador) {
        this.repositorio = repositorio; this.usuarios = usuarios; this.empresas = empresas; this.atualizador = atualizador;
    }
    private Mercadoria selecionar(long empresaId, long id) {
        return repositorio.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RecursoNaoEncontrado("Mercadoria não encontrada nesta empresa."));
    }

    /**
     * Lista as mercadorias da empresa.
     *
     * @param fornecedorId filtro opcional pelo fornecedor
     */
    public Page<MercadoriaResposta> listar(long empresaId, Long fornecedorId, int pagina, int tamanho) {
        empresas.selecionar(empresaId);
        return repositorio.listar(empresaId, fornecedorId, PageRequest.of(pagina, tamanho, Sort.by("id")))
            .map(MercadoriaResposta::de);
    }
    public MercadoriaResposta obter(long empresaId, long id) { return MercadoriaResposta.de(selecionar(empresaId, id)); }

    @Transactional
    public MercadoriaResposta cadastrar(long empresaId, MercadoriaDados dados) {
        var mercadoria = new Mercadoria();
        mercadoria.setEmpresa(empresas.selecionar(empresaId));
        mercadoria.setCadastro(Instant.now().truncatedTo(ChronoUnit.MICROS));
        preencher(mercadoria, empresaId, dados);
        return MercadoriaResposta.de(repositorio.saveAndFlush(mercadoria));
    }
    @Transactional
    public MercadoriaResposta atualizar(long empresaId, long id, MercadoriaDados dados) {
        var mercadoria = repositorio.bloquear(empresaId, id)
            .orElseThrow(() -> new RecursoNaoEncontrado("Mercadoria não encontrada nesta empresa."));
        preencher(mercadoria, empresaId, dados);
        repositorio.flush();
        return MercadoriaResposta.de(mercadoria);
    }
    /** Exclui a mercadoria; se ela já foi vendida, o banco recusa (409) para preservar o histórico. */
    @Transactional
    public void excluir(long empresaId, long id) {
        repositorio.delete(selecionar(empresaId, id));
        repositorio.flush();
    }

    private void preencher(Mercadoria mercadoria, long empresaId, MercadoriaDados dados) {
        atualizador.atualizar(mercadoria, dados);
        mercadoria.setFornecedor(dados.fornecedorId() == null ? null : fornecedor(empresaId, dados.fornecedorId()));
    }
    private Usuario fornecedor(long empresaId, long id) {
        var usuario = usuarios.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RegraNegocio("Fornecedor não encontrado nesta empresa."));
        if (!usuario.getPerfis().contains(PerfilUsuario.FORNECEDOR))
            throw new RegraNegocio("O usuário informado não tem o perfil FORNECEDOR.");
        return usuario;
    }
}
