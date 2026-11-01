package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.controles.UsuarioControle;
import org.springframework.hateoas.Link;

/** Links para os recursos "pai" (empresa e usuário), repetidos em vários {@code AdicionadorLink*}. */
public final class Relacoes {
    private Relacoes() {}

    public static Link empresa(long empresaId) {
        return linkTo(methodOn(EmpresaControle.class).obter(empresaId)).withRel("empresa");
    }
    public static Link usuario(long empresaId, long usuarioId, String relacao) {
        return linkTo(methodOn(UsuarioControle.class).obter(empresaId, usuarioId)).withRel(relacao);
    }
    public static Link usuario(long empresaId, long usuarioId) { return usuario(empresaId, usuarioId, "usuario"); }

    /**
     * Link para o titular de um endereço ou telefone.
     *
     * @param dono empresa ou usuário da URL
     * @return relação {@code empresa} ou {@code usuario}
     */
    public static Link dono(Dono dono) {
        return dono.ehEmpresa() ? empresa(dono.empresaId()) : usuario(dono.empresaId(), dono.usuarioId());
    }
}
