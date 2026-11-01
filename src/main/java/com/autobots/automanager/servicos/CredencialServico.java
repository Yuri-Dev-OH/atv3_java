package com.autobots.automanager.servicos;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.autobots.automanager.dto.Dados.CredencialDados;
import com.autobots.automanager.dto.Respostas.CredencialResposta;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.enumeracoes.TipoCredencial;
import com.autobots.automanager.erros.RecursoNaoEncontrado;
import com.autobots.automanager.erros.RegraNegocio;
import com.autobots.automanager.modelo.UsuarioSelecionador;
import com.autobots.automanager.repositorios.CredencialRepositorio;
import com.autobots.automanager.seguranca.CodificadorSenha;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD das credenciais de acesso de um usuário.
 *
 * <p>A senha é recebida em texto puro somente na entrada, gravada como hash e nunca devolvida. A autenticação em si
 * pertence à "atualização de segurança", fora do escopo da ATVIII; por isso {@code ultimoAcesso} ainda não é preenchido.
 */
@Service
@Transactional(readOnly = true)
public class CredencialServico {
    private final UsuarioSelecionador usuarios;
    private final CredencialRepositorio repositorio;
    private final CodificadorSenha codificador;
    public CredencialServico(UsuarioSelecionador usuarios, CredencialRepositorio repositorio, CodificadorSenha codificador) {
        this.usuarios = usuarios; this.repositorio = repositorio; this.codificador = codificador;
    }
    private static Credencial selecionar(Usuario usuario, long id) {
        return usuario.getCredenciais().stream().filter(item -> item.getId().equals(id)).findFirst()
            .orElseThrow(() -> new RecursoNaoEncontrado("Credencial não encontrada para este usuário."));
    }
    public List<CredencialResposta> listar(long empresaId, long usuarioId) {
        return usuarios.selecionar(empresaId, usuarioId).getCredenciais().stream().map(CredencialResposta::de).toList();
    }
    public CredencialResposta obter(long empresaId, long usuarioId, long id) {
        return CredencialResposta.de(selecionar(usuarios.selecionar(empresaId, usuarioId), id));
    }

    @Transactional
    public CredencialResposta cadastrar(long empresaId, long usuarioId, CredencialDados dados) {
        var usuario = usuarios.bloquear(empresaId, usuarioId);
        Credencial credencial;
        if (dados.tipo() == TipoCredencial.USUARIO_SENHA) {
            if (dados.senha() == null) throw new RegraNegocio("Informe a senha da credencial.");
            credencial = new CredencialUsuarioSenha();
        } else {
            credencial = new CredencialCodigoBarra();
        }
        credencial.setCriacao(Instant.now().truncatedTo(ChronoUnit.MICROS));
        preencher(credencial, dados);
        usuario.getCredenciais().add(credencial);
        repositorio.flush();
        return CredencialResposta.de(credencial);
    }

    /** Atualiza a credencial; o tipo não muda e a senha omitida é mantida. */
    @Transactional
    public CredencialResposta atualizar(long empresaId, long usuarioId, long id, CredencialDados dados) {
        var credencial = selecionar(usuarios.bloquear(empresaId, usuarioId), id);
        if (credencial.getTipo() != dados.tipo())
            throw new RegraNegocio("O tipo da credencial não pode ser alterado. Exclua-a e cadastre outra.");
        preencher(credencial, dados);
        repositorio.flush();
        return CredencialResposta.de(credencial);
    }

    @Transactional
    public void excluir(long empresaId, long usuarioId, long id) {
        var usuario = usuarios.bloquear(empresaId, usuarioId);
        usuario.getCredenciais().remove(selecionar(usuario, id));
    }

    private void preencher(Credencial credencial, CredencialDados dados) {
        credencial.setInativo(dados.inativo());
        if (credencial instanceof CredencialUsuarioSenha senha) {
            senha.setNomeUsuario(dados.nomeUsuario());
            if (dados.senha() != null) senha.setSenha(codificador.codificar(dados.senha()));
        } else if (credencial instanceof CredencialCodigoBarra codigo) {
            codigo.setCodigo(dados.codigo());
        }
    }
}
