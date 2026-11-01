package com.autobots.automanager.servicos;

import java.util.List;
import com.autobots.automanager.dto.Dados.EmailDados;
import com.autobots.automanager.dto.Respostas.EmailResposta;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.erros.RecursoNaoEncontrado;
import com.autobots.automanager.modelo.EmailAtualizador;
import com.autobots.automanager.modelo.UsuarioSelecionador;
import com.autobots.automanager.repositorios.EmailRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmailServico {
    private final UsuarioSelecionador usuarios;
    private final EmailRepositorio repositorio;
    private final EmailAtualizador atualizador;
    public EmailServico(UsuarioSelecionador usuarios, EmailRepositorio repositorio, EmailAtualizador atualizador) {
        this.usuarios = usuarios; this.repositorio = repositorio; this.atualizador = atualizador;
    }
    private static Email selecionar(Usuario usuario, long id) {
        return usuario.getEmails().stream().filter(item -> item.getId().equals(id)).findFirst()
            .orElseThrow(() -> new RecursoNaoEncontrado("E-mail não encontrado para este usuário."));
    }
    public List<EmailResposta> listar(long empresaId, long usuarioId) {
        return usuarios.selecionar(empresaId, usuarioId).getEmails().stream().map(EmailResposta::de).toList();
    }
    public EmailResposta obter(long empresaId, long usuarioId, long id) {
        return EmailResposta.de(selecionar(usuarios.selecionar(empresaId, usuarioId), id));
    }
    @Transactional
    public EmailResposta cadastrar(long empresaId, long usuarioId, EmailDados dados) {
        var email = atualizador.adicionar(usuarios.bloquear(empresaId, usuarioId), dados);
        repositorio.flush();
        return EmailResposta.de(email);
    }
    @Transactional
    public EmailResposta atualizar(long empresaId, long usuarioId, long id, EmailDados dados) {
        var email = selecionar(usuarios.bloquear(empresaId, usuarioId), id);
        atualizador.atualizar(email, dados);
        repositorio.flush();
        return EmailResposta.de(email);
    }
    @Transactional
    public void excluir(long empresaId, long usuarioId, long id) {
        var usuario = usuarios.bloquear(empresaId, usuarioId);
        usuario.getEmails().remove(selecionar(usuario, id));
    }
}
