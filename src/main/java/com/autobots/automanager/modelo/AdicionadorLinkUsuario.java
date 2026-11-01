package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.autobots.automanager.controles.*;
import com.autobots.automanager.dto.Respostas.UsuarioResposta;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

/**
 * Adiciona links HATEOAS aos usuários.
 *
 * <p>Os links dependem dos perfis: só clientes recebem {@code veiculos} e {@code compras}, só funcionários recebem
 * {@code vendas} (atendidas) e só fornecedores recebem {@code mercadorias} (fornecidas).
 */
@Component
public class AdicionadorLinkUsuario {

    /**
     * Envolve um usuário com seus links.
     *
     * @param empresaId empresa do usuário
     * @param usuario representação do usuário
     * @return usuário com {@code self} (ações de atualizar e excluir), {@code usuarios}, {@code empresa}, sub-recursos
     *     e links conforme os perfis
     */
    public EntityModel<UsuarioResposta> adicionarLink(long empresaId, UsuarioResposta usuario) {
        long id = usuario.id();
        Link endereco = linkTo(methodOn(UsuarioEnderecoControle.class).obter(empresaId, id)).withRel("endereco");
        if (usuario.endereco() == null) {
            endereco = endereco.andAffordance(afford(methodOn(UsuarioEnderecoControle.class).cadastrar(empresaId, id, null)));
        }
        var modelo = EntityModel.of(usuario,
            linkTo(methodOn(UsuarioControle.class).obter(empresaId, id)).withSelfRel()
                .andAffordance(afford(methodOn(UsuarioControle.class).atualizar(empresaId, id, null)))
                .andAffordance(afford(methodOn(UsuarioControle.class).excluir(empresaId, id))),
            linkTo(methodOn(UsuarioControle.class).listar(empresaId, null, null, null)).withRel("usuarios"),
            Relacoes.empresa(empresaId),
            endereco,
            linkTo(methodOn(UsuarioTelefoneControle.class).listar(empresaId, id)).withRel("telefones"),
            linkTo(methodOn(DocumentoControle.class).listar(empresaId, id)).withRel("documentos"),
            linkTo(methodOn(EmailControle.class).listar(empresaId, id)).withRel("emails"),
            linkTo(methodOn(CredencialControle.class).listar(empresaId, id)).withRel("credenciais"));
        if (usuario.possui(PerfilUsuario.CLIENTE)) {
            modelo.add(linkTo(methodOn(VeiculoControle.class).listar(empresaId, id)).withRel("veiculos"));
            modelo.add(linkTo(methodOn(VendaControle.class).listar(empresaId, id, null, null, null, null)).withRel("compras"));
        }
        if (usuario.possui(PerfilUsuario.FUNCIONARIO)) {
            modelo.add(linkTo(methodOn(VendaControle.class).listar(empresaId, null, id, null, null, null)).withRel("vendas"));
        }
        if (usuario.possui(PerfilUsuario.FORNECEDOR)) {
            modelo.add(linkTo(methodOn(MercadoriaControle.class).listar(empresaId, id, null, null)).withRel("mercadorias"));
        }
        return modelo;
    }

    /**
     * Envolve uma página de usuários, preservando o filtro de perfil nos links de navegação.
     *
     * @param empresaId empresa da URL
     * @param perfil filtro aplicado, ou {@code null}
     * @param pagina página consultada
     * @return modelo paginado com {@code empresa}; {@code self} anuncia o cadastro
     */
    public PagedModel<EntityModel<UsuarioResposta>> adicionarLink(long empresaId, PerfilUsuario perfil, Page<UsuarioResposta> pagina) {
        var modelo = Paginacao.paginar(pagina, usuario -> adicionarLink(empresaId, usuario), (numero, tamanho) ->
            methodOn(UsuarioControle.class).listar(empresaId, perfil, numero, tamanho));
        modelo.mapLink(IanaLinkRelations.SELF, self ->
            self.andAffordance(afford(methodOn(UsuarioControle.class).cadastrar(empresaId, null))));
        modelo.add(Relacoes.empresa(empresaId));
        return modelo;
    }
}
