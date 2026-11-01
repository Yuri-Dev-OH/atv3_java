package com.autobots.automanager.servicos;

import java.util.List;
import com.autobots.automanager.dto.Dados.DocumentoDados;
import com.autobots.automanager.dto.Respostas.DocumentoResposta;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.erros.RecursoNaoEncontrado;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.modelo.UsuarioSelecionador;
import com.autobots.automanager.repositorios.DocumentoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DocumentoServico {
    private final UsuarioSelecionador usuarios;
    private final DocumentoRepositorio repositorio;
    private final DocumentoAtualizador atualizador;
    public DocumentoServico(UsuarioSelecionador usuarios, DocumentoRepositorio repositorio, DocumentoAtualizador atualizador) {
        this.usuarios = usuarios; this.repositorio = repositorio; this.atualizador = atualizador;
    }
    private static Documento selecionar(Usuario usuario, long id) {
        return usuario.getDocumentos().stream().filter(item -> item.getId().equals(id)).findFirst()
            .orElseThrow(() -> new RecursoNaoEncontrado("Documento não encontrado para este usuário."));
    }
    public List<DocumentoResposta> listar(long empresaId, long usuarioId) {
        return usuarios.selecionar(empresaId, usuarioId).getDocumentos().stream().map(DocumentoResposta::de).toList();
    }
    public DocumentoResposta obter(long empresaId, long usuarioId, long id) {
        return DocumentoResposta.de(selecionar(usuarios.selecionar(empresaId, usuarioId), id));
    }
    @Transactional
    public DocumentoResposta cadastrar(long empresaId, long usuarioId, DocumentoDados dados) {
        var documento = atualizador.adicionar(usuarios.bloquear(empresaId, usuarioId), dados);
        repositorio.flush();
        return DocumentoResposta.de(documento);
    }
    @Transactional
    public DocumentoResposta atualizar(long empresaId, long usuarioId, long id, DocumentoDados dados) {
        var documento = selecionar(usuarios.bloquear(empresaId, usuarioId), id);
        atualizador.atualizar(documento, dados);
        repositorio.flush();
        return DocumentoResposta.de(documento);
    }
    @Transactional
    public void excluir(long empresaId, long usuarioId, long id) {
        var usuario = usuarios.bloquear(empresaId, usuarioId);
        usuario.getDocumentos().remove(selecionar(usuario, id));
    }
}
