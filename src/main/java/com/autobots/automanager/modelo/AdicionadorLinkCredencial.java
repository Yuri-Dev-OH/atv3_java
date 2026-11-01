package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import com.autobots.automanager.controles.CredencialControle;
import com.autobots.automanager.dto.Respostas.CredencialResposta;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

/** Adiciona links HATEOAS às credenciais de um usuário. */
@Component
public class AdicionadorLinkCredencial {

    /**
     * Envolve uma credencial com seus links.
     *
     * @param empresaId empresa do usuário
     * @param usuarioId usuário dono da credencial
     * @param credencial representação da credencial
     * @return credencial com {@code self} (ações de atualizar e excluir), {@code credenciais} e {@code usuario}
     */
    public EntityModel<CredencialResposta> adicionarLink(long empresaId, long usuarioId, CredencialResposta credencial) {
        long id = credencial.id();
        return EntityModel.of(credencial,
            linkTo(methodOn(CredencialControle.class).obter(empresaId, usuarioId, id)).withSelfRel()
                .andAffordance(afford(methodOn(CredencialControle.class).atualizar(empresaId, usuarioId, id, null)))
                .andAffordance(afford(methodOn(CredencialControle.class).excluir(empresaId, usuarioId, id))),
            linkTo(methodOn(CredencialControle.class).listar(empresaId, usuarioId)).withRel("credenciais"),
            Relacoes.usuario(empresaId, usuarioId));
    }

    /**
     * Envolve a lista de credenciais de um usuário.
     *
     * @param empresaId empresa do usuário
     * @param usuarioId usuário dono das credenciais
     * @param credenciais credenciais do usuário
     * @return coleção com {@code self} (ação de cadastrar) e {@code usuario}
     */
    public CollectionModel<EntityModel<CredencialResposta>> adicionarLink(long empresaId, long usuarioId,
            List<CredencialResposta> credenciais) {
        return CollectionModel.of(credenciais.stream().map(credencial -> adicionarLink(empresaId, usuarioId, credencial)).toList(),
            linkTo(methodOn(CredencialControle.class).listar(empresaId, usuarioId)).withSelfRel()
                .andAffordance(afford(methodOn(CredencialControle.class).cadastrar(empresaId, usuarioId, null))),
            Relacoes.usuario(empresaId, usuarioId));
    }
}
