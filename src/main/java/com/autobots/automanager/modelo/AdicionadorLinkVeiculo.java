package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import com.autobots.automanager.controles.VeiculoControle;
import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.dto.Respostas.VeiculoResposta;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

/** Adiciona links HATEOAS aos veículos de um cliente. */
@Component
public class AdicionadorLinkVeiculo {

    /**
     * Envolve um veículo com seus links.
     *
     * @param empresaId empresa do proprietário
     * @param usuarioId cliente proprietário
     * @param veiculo representação do veículo
     * @return veículo com {@code self} (ações de atualizar e excluir), {@code veiculos}, {@code proprietario} e
     *     {@code vendas} (vendas e serviços feitos neste veículo)
     */
    public EntityModel<VeiculoResposta> adicionarLink(long empresaId, long usuarioId, VeiculoResposta veiculo) {
        long id = veiculo.id();
        return EntityModel.of(veiculo,
            linkTo(methodOn(VeiculoControle.class).obter(empresaId, usuarioId, id)).withSelfRel()
                .andAffordance(afford(methodOn(VeiculoControle.class).atualizar(empresaId, usuarioId, id, null)))
                .andAffordance(afford(methodOn(VeiculoControle.class).excluir(empresaId, usuarioId, id))),
            linkTo(methodOn(VeiculoControle.class).listar(empresaId, usuarioId)).withRel("veiculos"),
            Relacoes.usuario(empresaId, usuarioId, "proprietario"),
            linkTo(methodOn(VendaControle.class).listar(empresaId, null, null, id, null, null)).withRel("vendas"));
    }

    /**
     * Envolve a lista de veículos de um cliente.
     *
     * @param empresaId empresa do proprietário
     * @param usuarioId cliente proprietário
     * @param veiculos veículos do cliente
     * @return coleção com {@code self} (ação de cadastrar) e {@code proprietario}
     */
    public CollectionModel<EntityModel<VeiculoResposta>> adicionarLink(long empresaId, long usuarioId, List<VeiculoResposta> veiculos) {
        return CollectionModel.of(veiculos.stream().map(veiculo -> adicionarLink(empresaId, usuarioId, veiculo)).toList(),
            linkTo(methodOn(VeiculoControle.class).listar(empresaId, usuarioId)).withSelfRel()
                .andAffordance(afford(methodOn(VeiculoControle.class).cadastrar(empresaId, usuarioId, null))),
            Relacoes.usuario(empresaId, usuarioId, "proprietario"));
    }
}
