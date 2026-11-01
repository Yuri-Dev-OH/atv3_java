package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import com.autobots.automanager.controles.EmailControle;
import com.autobots.automanager.dto.Respostas.EmailResposta;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

/** Adiciona links HATEOAS aos e-mails de um usuário. */
@Component
public class AdicionadorLinkEmail {

    /**
     * Envolve um e-mail com seus links.
     *
     * @param empresaId empresa do usuário
     * @param usuarioId usuário dono do e-mail
     * @param email representação do e-mail
     * @return e-mail com {@code self} (ações de atualizar e excluir), {@code emails} e {@code usuario}
     */
    public EntityModel<EmailResposta> adicionarLink(long empresaId, long usuarioId, EmailResposta email) {
        long id = email.id();
        return EntityModel.of(email,
            linkTo(methodOn(EmailControle.class).obter(empresaId, usuarioId, id)).withSelfRel()
                .andAffordance(afford(methodOn(EmailControle.class).atualizar(empresaId, usuarioId, id, null)))
                .andAffordance(afford(methodOn(EmailControle.class).excluir(empresaId, usuarioId, id))),
            linkTo(methodOn(EmailControle.class).listar(empresaId, usuarioId)).withRel("emails"),
            Relacoes.usuario(empresaId, usuarioId));
    }

    /**
     * Envolve a lista de e-mails de um usuário.
     *
     * @param empresaId empresa do usuário
     * @param usuarioId usuário dono dos e-mails
     * @param emails e-mails do usuário
     * @return coleção com {@code self} (ação de cadastrar) e {@code usuario}
     */
    public CollectionModel<EntityModel<EmailResposta>> adicionarLink(long empresaId, long usuarioId, List<EmailResposta> emails) {
        return CollectionModel.of(emails.stream().map(email -> adicionarLink(empresaId, usuarioId, email)).toList(),
            linkTo(methodOn(EmailControle.class).listar(empresaId, usuarioId)).withSelfRel()
                .andAffordance(afford(methodOn(EmailControle.class).cadastrar(empresaId, usuarioId, null))),
            Relacoes.usuario(empresaId, usuarioId));
    }
}
