package com.autobots.automanager.servicos;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import com.autobots.automanager.dto.Dados.*;
import com.autobots.automanager.dto.Respostas.EmpresaResposta;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.modelo.*;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmpresaServico {
    private final EmpresaRepositorio repositorio;
    private final EmpresaSelecionador selecionador;
    private final EmpresaAtualizador atualizador;
    private final EnderecoAtualizador enderecoAtualizador;
    private final TelefoneAtualizador telefoneAtualizador;

    public EmpresaServico(EmpresaRepositorio repositorio, EmpresaSelecionador selecionador, EmpresaAtualizador atualizador,
            EnderecoAtualizador enderecoAtualizador, TelefoneAtualizador telefoneAtualizador) {
        this.repositorio = repositorio; this.selecionador = selecionador; this.atualizador = atualizador;
        this.enderecoAtualizador = enderecoAtualizador; this.telefoneAtualizador = telefoneAtualizador;
    }

    public Page<EmpresaResposta> listar(int pagina, int tamanho) {
        return repositorio.findAll(PageRequest.of(pagina, tamanho, Sort.by("id"))).map(EmpresaResposta::de);
    }
    public EmpresaResposta obter(long id) { return EmpresaResposta.de(selecionador.selecionar(id)); }

    @Transactional
    public EmpresaResposta cadastrar(EmpresaCadastro dados) {
        var empresa = new Empresa();
        atualizador.atualizar(empresa, new EmpresaDados(dados.razaoSocial(), dados.nomeFantasia()));
        empresa.setCadastro(Instant.now().truncatedTo(ChronoUnit.MICROS));
        if (dados.endereco() != null) {
            var endereco = new Endereco();
            enderecoAtualizador.atualizar(endereco, dados.endereco());
            empresa.setEndereco(endereco);
        }
        dados.telefones().forEach(telefone -> telefoneAtualizador.adicionar(empresa, telefone));
        return EmpresaResposta.de(repositorio.saveAndFlush(empresa));
    }

    @Transactional
    public EmpresaResposta atualizar(long id, EmpresaDados dados) {
        var empresa = selecionador.bloquear(id);
        atualizador.atualizar(empresa, dados);
        return EmpresaResposta.de(empresa);
    }

    /**
     * Exclui a empresa com seu endereço e telefones.
     *
     * <p>Uma empresa que ainda tem usuários, mercadorias, serviços ou vendas é recusada pelo banco (409), para que um
     * engano não apague todo o histórico da unidade.
     *
     * @param id empresa a excluir
     */
    @Transactional
    public void excluir(long id) {
        repositorio.delete(selecionador.bloquear(id));
        repositorio.flush();
    }
}
