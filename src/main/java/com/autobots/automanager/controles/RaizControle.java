package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ponto de entrada da API.
 *
 * <p>Um cliente HATEOAS precisa conhecer somente esta URI; as demais são descobertas pelos links.
 */
@RestController
public class RaizControle {

    /**
     * Lista os recursos de primeiro nível.
     *
     * @return links {@code self} e {@code empresas} (template com {@code pagina} e {@code tamanho}); os demais
     *     recursos ficam dentro de cada empresa
     */
    @GetMapping("/")
    public RepresentationModel<?> raiz() {
        return new RepresentationModel<>()
            .add(linkTo(methodOn(RaizControle.class).raiz()).withSelfRel())
            .add(linkTo(methodOn(EmpresaControle.class).listar(null, null)).withRel("empresas")
                .andAffordance(afford(methodOn(EmpresaControle.class).cadastrar(null))));
    }
}
