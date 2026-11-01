package com.autobots.automanager.servicos;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import com.autobots.automanager.dto.Dados.*;
import com.autobots.automanager.dto.Respostas.UsuarioResposta;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.erros.Conflito;
import com.autobots.automanager.modelo.*;
import com.autobots.automanager.repositorios.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsuarioServico {
    private final UsuarioRepositorio repositorio;
    private final VendaRepositorio vendas;
    private final MercadoriaRepositorio mercadorias;
    private final EmpresaSelecionador empresas;
    private final UsuarioSelecionador selecionador;
    private final UsuarioAtualizador atualizador;
    private final EnderecoAtualizador enderecoAtualizador;
    private final TelefoneAtualizador telefoneAtualizador;
    private final DocumentoAtualizador documentoAtualizador;
    private final EmailAtualizador emailAtualizador;

    public UsuarioServico(UsuarioRepositorio repositorio, VendaRepositorio vendas, MercadoriaRepositorio mercadorias,
            EmpresaSelecionador empresas, UsuarioSelecionador selecionador, UsuarioAtualizador atualizador,
            EnderecoAtualizador enderecoAtualizador, TelefoneAtualizador telefoneAtualizador,
            DocumentoAtualizador documentoAtualizador, EmailAtualizador emailAtualizador) {
        this.repositorio = repositorio; this.vendas = vendas; this.mercadorias = mercadorias; this.empresas = empresas;
        this.selecionador = selecionador; this.atualizador = atualizador; this.enderecoAtualizador = enderecoAtualizador;
        this.telefoneAtualizador = telefoneAtualizador; this.documentoAtualizador = documentoAtualizador;
        this.emailAtualizador = emailAtualizador;
    }

    /**
     * Lista os usuários da empresa.
     *
     * @param empresaId empresa da URL
     * @param perfil filtro opcional por perfil
     * @param pagina página, a partir de zero
     * @param tamanho itens por página
     * @return página de usuários ordenada por id
     */
    public Page<UsuarioResposta> listar(long empresaId, PerfilUsuario perfil, int pagina, int tamanho) {
        empresas.selecionar(empresaId);
        var pedido = PageRequest.of(pagina, tamanho, Sort.by("id"));
        var usuarios = perfil == null ? repositorio.findByEmpresaId(empresaId, pedido)
            : repositorio.listarPorPerfil(empresaId, perfil, pedido);
        return usuarios.map(UsuarioResposta::de);
    }
    public UsuarioResposta obter(long empresaId, long id) { return UsuarioResposta.de(selecionador.selecionar(empresaId, id)); }

    @Transactional
    public UsuarioResposta cadastrar(long empresaId, UsuarioCadastro dados) {
        var usuario = new Usuario();
        usuario.setEmpresa(empresas.selecionar(empresaId));
        atualizador.atualizar(usuario, new UsuarioDados(dados.nome(), dados.nomeSocial(), dados.dataNascimento(), dados.perfis()));
        usuario.setDataCadastro(Instant.now().truncatedTo(ChronoUnit.MICROS));
        if (dados.endereco() != null) {
            var endereco = new Endereco();
            enderecoAtualizador.atualizar(endereco, dados.endereco());
            usuario.setEndereco(endereco);
        }
        dados.telefones().forEach(telefone -> telefoneAtualizador.adicionar(usuario, telefone));
        dados.documentos().forEach(documento -> documentoAtualizador.adicionar(usuario, documento));
        dados.emails().forEach(email -> emailAtualizador.adicionar(usuario, email));
        return UsuarioResposta.de(repositorio.saveAndFlush(usuario));
    }

    /**
     * Atualiza nome, nome social, nascimento e perfis. Contatos, documentos e credenciais têm rotas próprias.
     *
     * <p>Um perfil só pode ser retirado se nada depender dele: veículos e compras exigem {@code CLIENTE}, vendas
     * atendidas exigem {@code FUNCIONARIO} e mercadorias fornecidas exigem {@code FORNECEDOR}.
     */
    @Transactional
    public UsuarioResposta atualizar(long empresaId, long id, UsuarioDados dados) {
        var usuario = selecionador.bloquear(empresaId, id);
        var retirados = new HashSet<>(usuario.getPerfis());
        retirados.removeAll(dados.perfis());
        if (retirados.contains(PerfilUsuario.CLIENTE) && (!usuario.getVeiculos().isEmpty() || vendas.existsByClienteId(id)))
            throw new Conflito("O usuário possui veículos ou compras e precisa manter o perfil CLIENTE.");
        if (retirados.contains(PerfilUsuario.FUNCIONARIO) && vendas.existsByFuncionarioId(id))
            throw new Conflito("O usuário atendeu vendas e precisa manter o perfil FUNCIONARIO.");
        if (retirados.contains(PerfilUsuario.FORNECEDOR) && mercadorias.existsByFornecedorId(id))
            throw new Conflito("O usuário fornece mercadorias e precisa manter o perfil FORNECEDOR.");
        atualizador.atualizar(usuario, dados);
        repositorio.flush();
        return UsuarioResposta.de(usuario);
    }

    /**
     * Exclui o usuário com contatos, documentos, credenciais e veículos.
     *
     * <p>Usuários citados em vendas ou como fornecedores de mercadorias são recusados pelo banco (409).
     */
    @Transactional
    public void excluir(long empresaId, long id) {
        repositorio.delete(selecionador.bloquear(empresaId, id));
        repositorio.flush();
    }
}
