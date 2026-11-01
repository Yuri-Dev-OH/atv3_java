package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.autobots.automanager.controles.VeiculoControle;
import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.dto.Respostas.VendaResposta;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

/** Adiciona links HATEOAS às vendas de uma empresa. */
@Component
public class AdicionadorLinkVenda {

    /**
     * Envolve uma venda com seus links.
     *
     * @param empresaId empresa da venda
     * @param venda representação da venda
     * @return venda com {@code self} (ações de atualizar e excluir), {@code vendas}, {@code empresa}, {@code cliente},
     *     {@code funcionario} e, se houver, {@code veiculo}
     */
    public EntityModel<VendaResposta> adicionarLink(long empresaId, VendaResposta venda) {
        long id = venda.id();
        long clienteId = venda.cliente().id();
        var modelo = EntityModel.of(venda,
            linkTo(methodOn(VendaControle.class).obter(empresaId, id)).withSelfRel()
                .andAffordance(afford(methodOn(VendaControle.class).atualizar(empresaId, id, null)))
                .andAffordance(afford(methodOn(VendaControle.class).excluir(empresaId, id))),
            linkTo(methodOn(VendaControle.class).listar(empresaId, null, null, null, null, null)).withRel("vendas"),
            Relacoes.empresa(empresaId),
            Relacoes.usuario(empresaId, clienteId, "cliente"),
            Relacoes.usuario(empresaId, venda.funcionario().id(), "funcionario"));
        if (venda.veiculo() != null) {
            modelo.add(linkTo(methodOn(VeiculoControle.class).obter(empresaId, clienteId, venda.veiculo().id())).withRel("veiculo"));
        }
        return modelo;
    }

    /**
     * Envolve uma página de vendas, preservando os filtros nos links de navegação.
     *
     * @param empresaId empresa da URL
     * @param clienteId filtro por cliente, ou {@code null}
     * @param funcionarioId filtro por funcionário, ou {@code null}
     * @param veiculoId filtro por veículo, ou {@code null}
     * @param pagina página consultada
     * @return modelo paginado com {@code empresa}; {@code self} anuncia o cadastro
     */
    public PagedModel<EntityModel<VendaResposta>> adicionarLink(long empresaId, Long clienteId, Long funcionarioId, Long veiculoId,
            Page<VendaResposta> pagina) {
        var modelo = Paginacao.paginar(pagina, venda -> adicionarLink(empresaId, venda), (numero, tamanho) ->
            methodOn(VendaControle.class).listar(empresaId, clienteId, funcionarioId, veiculoId, numero, tamanho));
        modelo.mapLink(IanaLinkRelations.SELF, self ->
            self.andAffordance(afford(methodOn(VendaControle.class).cadastrar(empresaId, null))));
        modelo.add(Relacoes.empresa(empresaId));
        return modelo;
    }
}
