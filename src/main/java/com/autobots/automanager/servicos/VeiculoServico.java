package com.autobots.automanager.servicos;

import java.util.List;
import com.autobots.automanager.dto.Dados.VeiculoDados;
import com.autobots.automanager.dto.Respostas.VeiculoResposta;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.erros.RecursoNaoEncontrado;
import com.autobots.automanager.erros.RegraNegocio;
import com.autobots.automanager.modelo.UsuarioSelecionador;
import com.autobots.automanager.modelo.VeiculoAtualizador;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD dos veículos de um cliente. Somente usuários com perfil {@code CLIENTE} podem ter veículos. */
@Service
@Transactional(readOnly = true)
public class VeiculoServico {
    private final UsuarioSelecionador usuarios;
    private final VeiculoRepositorio repositorio;
    private final VeiculoAtualizador atualizador;
    public VeiculoServico(UsuarioSelecionador usuarios, VeiculoRepositorio repositorio, VeiculoAtualizador atualizador) {
        this.usuarios = usuarios; this.repositorio = repositorio; this.atualizador = atualizador;
    }
    private static Veiculo selecionar(Usuario usuario, long id) {
        return usuario.getVeiculos().stream().filter(item -> item.getId().equals(id)).findFirst()
            .orElseThrow(() -> new RecursoNaoEncontrado("Veículo não encontrado para este usuário."));
    }
    public List<VeiculoResposta> listar(long empresaId, long usuarioId) {
        return usuarios.selecionar(empresaId, usuarioId).getVeiculos().stream().map(VeiculoResposta::de).toList();
    }
    public VeiculoResposta obter(long empresaId, long usuarioId, long id) {
        return VeiculoResposta.de(selecionar(usuarios.selecionar(empresaId, usuarioId), id));
    }
    @Transactional
    public VeiculoResposta cadastrar(long empresaId, long usuarioId, VeiculoDados dados) {
        var usuario = usuarios.bloquear(empresaId, usuarioId);
        if (!usuario.getPerfis().contains(PerfilUsuario.CLIENTE))
            throw new RegraNegocio("Somente usuários com perfil CLIENTE podem ter veículos.");
        var veiculo = new Veiculo();
        veiculo.setProprietario(usuario);
        veiculo.setEmpresaId(empresaId);
        atualizador.atualizar(veiculo, dados);
        usuario.getVeiculos().add(veiculo);
        repositorio.flush();
        return VeiculoResposta.de(veiculo);
    }
    @Transactional
    public VeiculoResposta atualizar(long empresaId, long usuarioId, long id, VeiculoDados dados) {
        var veiculo = selecionar(usuarios.bloquear(empresaId, usuarioId), id);
        atualizador.atualizar(veiculo, dados);
        repositorio.flush();
        return VeiculoResposta.de(veiculo);
    }
    /** Exclui o veículo; se ele constar em vendas, o banco recusa (409). */
    @Transactional
    public void excluir(long empresaId, long usuarioId, long id) {
        var usuario = usuarios.bloquear(empresaId, usuarioId);
        usuario.getVeiculos().remove(selecionar(usuario, id));
        repositorio.flush();
    }
}
