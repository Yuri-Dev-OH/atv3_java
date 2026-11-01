package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.autobots.automanager.controles.MercadoriaControle;
import com.autobots.automanager.dto.Respostas.MercadoriaResposta;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

/** Adiciona links HATEOAS às mercadorias de uma empresa. */
@Component
public class AdicionadorLinkMercadoria {

    /**
     * Envolve uma mercadoria com seus links.
     *
     * @param empresaId empresa dona do estoque
     * @param mercadoria representação da mercadoria
     * @return mercadoria com {@code self} (ações de atualizar e excluir), {@code mercadorias}, {@code empresa} e, se
     *     houver, {@code fornecedor}
     */
    public EntityModel<MercadoriaResposta> adicionarLink(long empresaId, MercadoriaResposta mercadoria) {
        long id = mercadoria.id();
        var modelo = EntityModel.of(mercadoria,
            linkTo(methodOn(MercadoriaControle.class).obter(empresaId, id)).withSelfRel()
                .andAffordance(afford(methodOn(MercadoriaControle.class).atualizar(empresaId, id, null)))
                .andAffordance(afford(methodOn(MercadoriaControle.class).excluir(empresaId, id))),
            linkTo(methodOn(MercadoriaControle.class).listar(empresaId, null, null, null)).withRel("mercadorias"),
            Relacoes.empresa(empresaId));
        if (mercadoria.fornecedorId() != null) modelo.add(Relacoes.usuario(empresaId, mercadoria.fornecedorId(), "fornecedor"));
        return modelo;
    }

    /**
     * Envolve uma página de mercadorias, preservando o filtro de fornecedor nos links de navegação.
     *
     * @param empresaId empresa da URL
     * @param fornecedorId filtro aplicado, ou {@code null}
     * @param pagina página consultada
     * @return modelo paginado com {@code empresa}; {@code self} anuncia o cadastro
     */
    public PagedModel<EntityModel<MercadoriaResposta>> adicionarLink(long empresaId, Long fornecedorId, Page<MercadoriaResposta> pagina) {
        var modelo = Paginacao.paginar(pagina, mercadoria -> adicionarLink(empresaId, mercadoria), (numero, tamanho) ->
            methodOn(MercadoriaControle.class).listar(empresaId, fornecedorId, numero, tamanho));
        modelo.mapLink(IanaLinkRelations.SELF, self ->
            self.andAffordance(afford(methodOn(MercadoriaControle.class).cadastrar(empresaId, null))));
        modelo.add(Relacoes.empresa(empresaId));
        return modelo;
    }
}
