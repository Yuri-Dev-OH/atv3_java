package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import com.autobots.automanager.controles.DocumentoControle;
import com.autobots.automanager.dto.Respostas.DocumentoResposta;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

/** Adiciona links HATEOAS aos documentos de um usuário. */
@Component
public class AdicionadorLinkDocumento {

    /**
     * Envolve um documento com seus links.
     *
     * @param empresaId empresa do usuário
     * @param usuarioId usuário dono do documento
     * @param documento representação do documento
     * @return documento com {@code self} (ações de atualizar e excluir), {@code documentos} e {@code usuario}
     */
    public EntityModel<DocumentoResposta> adicionarLink(long empresaId, long usuarioId, DocumentoResposta documento) {
        long id = documento.id();
        return EntityModel.of(documento,
            linkTo(methodOn(DocumentoControle.class).obter(empresaId, usuarioId, id)).withSelfRel()
                .andAffordance(afford(methodOn(DocumentoControle.class).atualizar(empresaId, usuarioId, id, null)))
                .andAffordance(afford(methodOn(DocumentoControle.class).excluir(empresaId, usuarioId, id))),
            linkTo(methodOn(DocumentoControle.class).listar(empresaId, usuarioId)).withRel("documentos"),
            Relacoes.usuario(empresaId, usuarioId));
    }

    /**
     * Envolve a lista de documentos de um usuário.
     *
     * @param empresaId empresa do usuário
     * @param usuarioId usuário dono dos documentos
     * @param documentos documentos do usuário
     * @return coleção com {@code self} (ação de cadastrar) e {@code usuario}
     */
    public CollectionModel<EntityModel<DocumentoResposta>> adicionarLink(long empresaId, long usuarioId,
            List<DocumentoResposta> documentos) {
        return CollectionModel.of(documentos.stream().map(documento -> adicionarLink(empresaId, usuarioId, documento)).toList(),
            linkTo(methodOn(DocumentoControle.class).listar(empresaId, usuarioId)).withSelfRel()
                .andAffordance(afford(methodOn(DocumentoControle.class).cadastrar(empresaId, usuarioId, null))),
            Relacoes.usuario(empresaId, usuarioId));
    }
}
