package com.autobots.automanager.servicos;

import java.util.List;
import com.autobots.automanager.dto.Dados.TelefoneDados;
import com.autobots.automanager.dto.Respostas.TelefoneResposta;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Titular;
import com.autobots.automanager.erros.RecursoNaoEncontrado;
import com.autobots.automanager.modelo.Dono;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.modelo.TitularSelecionador;
import com.autobots.automanager.repositorios.TelefoneRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD dos telefones de uma empresa ou de um usuário. */
@Service
@Transactional(readOnly = true)
public class TelefoneServico {
    private final TitularSelecionador titulares;
    private final TelefoneRepositorio repositorio;
    private final TelefoneAtualizador atualizador;

    public TelefoneServico(TitularSelecionador titulares, TelefoneRepositorio repositorio, TelefoneAtualizador atualizador) {
        this.titulares = titulares; this.repositorio = repositorio; this.atualizador = atualizador;
    }
    private static Telefone selecionar(Titular titular, Dono dono, long id) {
        return titular.getTelefones().stream().filter(item -> item.getId().equals(id)).findFirst()
            .orElseThrow(() -> new RecursoNaoEncontrado("Telefone não encontrado " + dono.descricao() + "."));
    }
    public List<TelefoneResposta> listar(Dono dono) {
        return titulares.selecionar(dono).getTelefones().stream().map(TelefoneResposta::de).toList();
    }
    public TelefoneResposta obter(Dono dono, long id) {
        return TelefoneResposta.de(selecionar(titulares.selecionar(dono), dono, id));
    }
    @Transactional
    public TelefoneResposta cadastrar(Dono dono, TelefoneDados dados) {
        var telefone = atualizador.adicionar(titulares.bloquear(dono), dados);
        repositorio.flush();
        return TelefoneResposta.de(telefone);
    }
    @Transactional
    public TelefoneResposta atualizar(Dono dono, long id, TelefoneDados dados) {
        var titular = titulares.bloquear(dono);
        var telefone = selecionar(titular, dono, id);
        atualizador.verificarDuplicado(titular, dados, telefone);
        atualizador.atualizar(telefone, dados);
        repositorio.flush();
        return TelefoneResposta.de(telefone);
    }
    @Transactional
    public void excluir(Dono dono, long id) {
        var titular = titulares.bloquear(dono);
        titular.getTelefones().remove(selecionar(titular, dono, id));
    }
}
