package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.autobots.automanager.controles.*;
import com.autobots.automanager.dto.Respostas.EmpresaResposta;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

/**
 * Adiciona links HATEOAS às empresas.
 *
 * <p>A empresa é a porta de entrada para tudo o que pertence à unidade: endereço, telefones, usuários, mercadorias,
 * serviços e vendas.
 */
@Component
public class AdicionadorLinkEmpresa {

    /**
     * Envolve uma empresa com seus links.
     *
     * @param empresa representação da empresa
     * @return empresa com {@code self} (ações de atualizar e excluir), {@code empresas} e um link para cada recurso
     *     da empresa; {@code endereco} anuncia o cadastro quando ainda não existe
     */
    public EntityModel<EmpresaResposta> adicionarLink(EmpresaResposta empresa) {
        long id = empresa.id();
        Link endereco = linkTo(methodOn(EmpresaEnderecoControle.class).obter(id)).withRel("endereco");
        if (empresa.endereco() == null) {
            endereco = endereco.andAffordance(afford(methodOn(EmpresaEnderecoControle.class).cadastrar(id, null)));
        }
        return EntityModel.of(empresa,
            linkTo(methodOn(EmpresaControle.class).obter(id)).withSelfRel()
                .andAffordance(afford(methodOn(EmpresaControle.class).atualizar(id, null)))
                .andAffordance(afford(methodOn(EmpresaControle.class).excluir(id))),
            linkTo(methodOn(EmpresaControle.class).listar(null, null)).withRel("empresas"),
            endereco,
            linkTo(methodOn(EmpresaTelefoneControle.class).listar(id)).withRel("telefones"),
            linkTo(methodOn(UsuarioControle.class).listar(id, null, null, null)).withRel("usuarios"),
            linkTo(methodOn(MercadoriaControle.class).listar(id, null, null, null)).withRel("mercadorias"),
            linkTo(methodOn(ServicoControle.class).listar(id, null, null)).withRel("servicos"),
            linkTo(methodOn(VendaControle.class).listar(id, null, null, null, null, null)).withRel("vendas"));
    }

    /**
     * Envolve uma página de empresas.
     *
     * @param pagina página consultada
     * @return modelo paginado com links de navegação; {@code self} anuncia o cadastro
     */
    public PagedModel<EntityModel<EmpresaResposta>> adicionarLink(Page<EmpresaResposta> pagina) {
        var modelo = Paginacao.paginar(pagina, this::adicionarLink, (numero, tamanho) ->
            methodOn(EmpresaControle.class).listar(numero, tamanho));
        modelo.mapLink(IanaLinkRelations.SELF, self -> self.andAffordance(afford(methodOn(EmpresaControle.class).cadastrar(null))));
        return modelo;
    }
}
