package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.autobots.automanager.controles.ServicoControle;
import com.autobots.automanager.dto.Respostas.ServicoResposta;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

/** Adiciona links HATEOAS ao catálogo de serviços de uma empresa. */
@Component
public class AdicionadorLinkServico {

    /**
     * Envolve um serviço com seus links.
     *
     * @param empresaId empresa que oferece o serviço
     * @param servico representação do serviço
     * @return serviço com {@code self} (ações de atualizar e excluir), {@code servicos} e {@code empresa}
     */
    public EntityModel<ServicoResposta> adicionarLink(long empresaId, ServicoResposta servico) {
        long id = servico.id();
        return EntityModel.of(servico,
            linkTo(methodOn(ServicoControle.class).obter(empresaId, id)).withSelfRel()
                .andAffordance(afford(methodOn(ServicoControle.class).atualizar(empresaId, id, null)))
                .andAffordance(afford(methodOn(ServicoControle.class).excluir(empresaId, id))),
            linkTo(methodOn(ServicoControle.class).listar(empresaId, null, null)).withRel("servicos"),
            Relacoes.empresa(empresaId));
    }

    /**
     * Envolve uma página de serviços.
     *
     * @param empresaId empresa da URL
     * @param pagina página consultada
     * @return modelo paginado com {@code empresa}; {@code self} anuncia o cadastro
     */
    public PagedModel<EntityModel<ServicoResposta>> adicionarLink(long empresaId, Page<ServicoResposta> pagina) {
        var modelo = Paginacao.paginar(pagina, servico -> adicionarLink(empresaId, servico), (numero, tamanho) ->
            methodOn(ServicoControle.class).listar(empresaId, numero, tamanho));
        modelo.mapLink(IanaLinkRelations.SELF, self ->
            self.andAffordance(afford(methodOn(ServicoControle.class).cadastrar(empresaId, null))));
        modelo.add(Relacoes.empresa(empresaId));
        return modelo;
    }
}
