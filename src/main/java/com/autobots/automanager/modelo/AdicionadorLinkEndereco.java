package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.autobots.automanager.controles.EmpresaEnderecoControle;
import com.autobots.automanager.controles.UsuarioEnderecoControle;
import com.autobots.automanager.dto.Respostas.EnderecoResposta;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

/** Adiciona links HATEOAS ao endereço de uma empresa ou de um usuário. */
@Component
public class AdicionadorLinkEndereco {

    /**
     * Envolve o endereço com seus links.
     *
     * @param dono empresa ou usuário dono do endereço
     * @param endereco representação do endereço
     * @return endereço com {@code self} (ações de atualizar e excluir) e link para o dono ({@code empresa} ou
     *     {@code usuario})
     */
    public EntityModel<EnderecoResposta> adicionarLink(Dono dono, EnderecoResposta endereco) {
        return EntityModel.of(endereco, self(dono), Relacoes.dono(dono));
    }

    private static Link self(Dono dono) {
        long empresaId = dono.empresaId();
        if (dono.ehEmpresa()) {
            return linkTo(methodOn(EmpresaEnderecoControle.class).obter(empresaId)).withSelfRel()
                .andAffordance(afford(methodOn(EmpresaEnderecoControle.class).atualizar(empresaId, null)))
                .andAffordance(afford(methodOn(EmpresaEnderecoControle.class).excluir(empresaId)));
        }
        long usuarioId = dono.usuarioId();
        return linkTo(methodOn(UsuarioEnderecoControle.class).obter(empresaId, usuarioId)).withSelfRel()
            .andAffordance(afford(methodOn(UsuarioEnderecoControle.class).atualizar(empresaId, usuarioId, null)))
            .andAffordance(afford(methodOn(UsuarioEnderecoControle.class).excluir(empresaId, usuarioId)));
    }
}
