package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import com.autobots.automanager.controles.EmpresaTelefoneControle;
import com.autobots.automanager.controles.UsuarioTelefoneControle;
import com.autobots.automanager.dto.Respostas.TelefoneResposta;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

/** Adiciona links HATEOAS aos telefones de uma empresa ou de um usuário. */
@Component
public class AdicionadorLinkTelefone {

    /**
     * Envolve um telefone com seus links.
     *
     * @param dono empresa ou usuário dono do telefone
     * @param telefone representação do telefone
     * @return telefone com {@code self} (ações de atualizar e excluir), {@code telefones} e link para o dono
     */
    public EntityModel<TelefoneResposta> adicionarLink(Dono dono, TelefoneResposta telefone) {
        return EntityModel.of(telefone, self(dono, telefone.id()), colecao(dono).withRel("telefones"), Relacoes.dono(dono));
    }

    /**
     * Envolve a lista de telefones.
     *
     * @param dono empresa ou usuário dono dos telefones
     * @param telefones telefones do dono
     * @return coleção com {@code self} (ação de cadastrar) e link para o dono
     */
    public CollectionModel<EntityModel<TelefoneResposta>> adicionarLink(Dono dono, List<TelefoneResposta> telefones) {
        long empresaId = dono.empresaId();
        var cadastrar = dono.ehEmpresa()
            ? afford(methodOn(EmpresaTelefoneControle.class).cadastrar(empresaId, null))
            : afford(methodOn(UsuarioTelefoneControle.class).cadastrar(empresaId, dono.usuarioId(), null));
        return CollectionModel.of(telefones.stream().map(telefone -> adicionarLink(dono, telefone)).toList(),
            colecao(dono).withSelfRel().andAffordance(cadastrar), Relacoes.dono(dono));
    }

    private static Link colecao(Dono dono) {
        return dono.ehEmpresa() ? linkTo(methodOn(EmpresaTelefoneControle.class).listar(dono.empresaId())).withSelfRel()
            : linkTo(methodOn(UsuarioTelefoneControle.class).listar(dono.empresaId(), dono.usuarioId())).withSelfRel();
    }
    private static Link self(Dono dono, long id) {
        long empresaId = dono.empresaId();
        if (dono.ehEmpresa()) {
            return linkTo(methodOn(EmpresaTelefoneControle.class).obter(empresaId, id)).withSelfRel()
                .andAffordance(afford(methodOn(EmpresaTelefoneControle.class).atualizar(empresaId, id, null)))
                .andAffordance(afford(methodOn(EmpresaTelefoneControle.class).excluir(empresaId, id)));
        }
        long usuarioId = dono.usuarioId();
        return linkTo(methodOn(UsuarioTelefoneControle.class).obter(empresaId, usuarioId, id)).withSelfRel()
            .andAffordance(afford(methodOn(UsuarioTelefoneControle.class).atualizar(empresaId, usuarioId, id, null)))
            .andAffordance(afford(methodOn(UsuarioTelefoneControle.class).excluir(empresaId, usuarioId, id)));
    }
}
