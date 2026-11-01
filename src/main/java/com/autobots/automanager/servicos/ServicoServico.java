package com.autobots.automanager.servicos;

import com.autobots.automanager.dto.Dados.ServicoDados;
import com.autobots.automanager.dto.Respostas.ServicoResposta;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.erros.RecursoNaoEncontrado;
import com.autobots.automanager.modelo.EmpresaSelecionador;
import com.autobots.automanager.modelo.ServicoAtualizador;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD do catálogo de serviços (entidade {@link Servico}) de uma empresa. */
@Service
@Transactional(readOnly = true)
public class ServicoServico {
    private final ServicoRepositorio repositorio;
    private final EmpresaSelecionador empresas;
    private final ServicoAtualizador atualizador;
    public ServicoServico(ServicoRepositorio repositorio, EmpresaSelecionador empresas, ServicoAtualizador atualizador) {
        this.repositorio = repositorio; this.empresas = empresas; this.atualizador = atualizador;
    }
    private Servico selecionar(long empresaId, long id) {
        return repositorio.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RecursoNaoEncontrado("Serviço não encontrado nesta empresa."));
    }
    public Page<ServicoResposta> listar(long empresaId, int pagina, int tamanho) {
        empresas.selecionar(empresaId);
        return repositorio.findByEmpresaId(empresaId, PageRequest.of(pagina, tamanho, Sort.by("id"))).map(ServicoResposta::de);
    }
    public ServicoResposta obter(long empresaId, long id) { return ServicoResposta.de(selecionar(empresaId, id)); }

    @Transactional
    public ServicoResposta cadastrar(long empresaId, ServicoDados dados) {
        var servico = new Servico();
        servico.setEmpresa(empresas.selecionar(empresaId));
        atualizador.atualizar(servico, dados);
        return ServicoResposta.de(repositorio.saveAndFlush(servico));
    }
    @Transactional
    public ServicoResposta atualizar(long empresaId, long id, ServicoDados dados) {
        var servico = selecionar(empresaId, id);
        atualizador.atualizar(servico, dados);
        repositorio.flush();
        return ServicoResposta.de(servico);
    }
    /** Exclui o serviço; se ele já foi vendido, o banco recusa (409) para preservar o histórico. */
    @Transactional
    public void excluir(long empresaId, long id) {
        repositorio.delete(selecionar(empresaId, id));
        repositorio.flush();
    }
}
