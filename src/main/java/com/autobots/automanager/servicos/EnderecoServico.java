package com.autobots.automanager.servicos;

import com.autobots.automanager.dto.Dados.EnderecoDados;
import com.autobots.automanager.dto.Respostas.EnderecoResposta;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Titular;
import com.autobots.automanager.erros.Conflito;
import com.autobots.automanager.erros.RecursoNaoEncontrado;
import com.autobots.automanager.modelo.Dono;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.modelo.TitularSelecionador;
import com.autobots.automanager.repositorios.EnderecoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD do endereço de uma empresa ou de um usuário. */
@Service
@Transactional(readOnly = true)
public class EnderecoServico {
    private final TitularSelecionador titulares;
    private final EnderecoRepositorio repositorio;
    private final EnderecoAtualizador atualizador;

    public EnderecoServico(TitularSelecionador titulares, EnderecoRepositorio repositorio, EnderecoAtualizador atualizador) {
        this.titulares = titulares; this.repositorio = repositorio; this.atualizador = atualizador;
    }
    private static Endereco selecionar(Titular titular, Dono dono) {
        if (titular.getEndereco() == null) throw new RecursoNaoEncontrado("Endereço não encontrado " + dono.descricao() + ".");
        return titular.getEndereco();
    }
    public EnderecoResposta obter(Dono dono) { return EnderecoResposta.de(selecionar(titulares.selecionar(dono), dono)); }

    @Transactional
    public EnderecoResposta cadastrar(Dono dono, EnderecoDados dados) {
        var titular = titulares.bloquear(dono);
        if (titular.getEndereco() != null) throw new Conflito("O endereço já existe. Utilize PUT para atualizá-lo.");
        var endereco = new Endereco();
        atualizador.atualizar(endereco, dados);
        titular.setEndereco(endereco);
        repositorio.flush();
        return EnderecoResposta.de(endereco);
    }
    @Transactional
    public EnderecoResposta atualizar(Dono dono, EnderecoDados dados) {
        var endereco = selecionar(titulares.bloquear(dono), dono);
        atualizador.atualizar(endereco, dados);
        return EnderecoResposta.de(endereco);
    }
    @Transactional
    public void excluir(Dono dono) {
        var titular = titulares.bloquear(dono);
        selecionar(titular, dono);
        titular.setEndereco(null);
    }
}
