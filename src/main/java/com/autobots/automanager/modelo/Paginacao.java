package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;

/** Monta coleções paginadas com links de navegação, compartilhado pelos {@code AdicionadorLink*}. */
public final class Paginacao {
    private Paginacao() {}

    /**
     * Envolve uma página com links {@code self}, {@code first}, {@code last} e, quando aplicáveis, {@code prev} e
     * {@code next}.
     *
     * @param pagina página consultada
     * @param item converte cada elemento em modelo com links
     * @param invocacao recebe número e tamanho da página e devolve a chamada {@code methodOn(...).listar(...)} que gera
     *     a URI, preservando os filtros da consulta
     * @param <T> tipo dos elementos
     * @return modelo paginado
     */
    public static <T> PagedModel<EntityModel<T>> paginar(Page<T> pagina, Function<T, EntityModel<T>> item,
            BiFunction<Integer, Integer, Object> invocacao) {
        int numero = pagina.getNumber();
        int tamanho = pagina.getSize();
        int ultima = Math.max(pagina.getTotalPages() - 1, 0);
        var links = new ArrayList<Link>();
        links.add(linkTo(invocacao.apply(numero, tamanho)).withSelfRel());
        links.add(linkTo(invocacao.apply(0, tamanho)).withRel(IanaLinkRelations.FIRST));
        if (numero > 0) links.add(linkTo(invocacao.apply(Math.min(numero - 1, ultima), tamanho)).withRel(IanaLinkRelations.PREV));
        if (numero < ultima) links.add(linkTo(invocacao.apply(numero + 1, tamanho)).withRel(IanaLinkRelations.NEXT));
        links.add(linkTo(invocacao.apply(ultima, tamanho)).withRel(IanaLinkRelations.LAST));
        var metadados = new PageMetadata(tamanho, numero, pagina.getTotalElements(), pagina.getTotalPages());
        return PagedModel.of(pagina.getContent().stream().map(item).toList(), metadados, links);
    }
}
