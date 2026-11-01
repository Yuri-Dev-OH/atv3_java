package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.erros.RecursoNaoEncontrado;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import org.springframework.stereotype.Component;

/** Seleciona usuários sempre dentro da empresa da URL, impedindo acesso a usuários de outra empresa. */
@Component
public class UsuarioSelecionador {
    private final UsuarioRepositorio repositorio;
    public UsuarioSelecionador(UsuarioRepositorio repositorio) { this.repositorio = repositorio; }
    public Usuario selecionar(long empresaId, long id) {
        return repositorio.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RecursoNaoEncontrado("Usuário não encontrado nesta empresa."));
    }
    public Usuario bloquear(long empresaId, long id) {
        return repositorio.bloquear(empresaId, id)
            .orElseThrow(() -> new RecursoNaoEncontrado("Usuário não encontrado nesta empresa."));
    }
}
