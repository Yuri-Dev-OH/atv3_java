package com.autobots.automanager.controles;

import com.autobots.automanager.dto.Dados.DocumentoDados;
import com.autobots.automanager.dto.Respostas.DocumentoResposta;
import com.autobots.automanager.modelo.AdicionadorLinkDocumento;
import com.autobots.automanager.servicos.DocumentoServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas/{empresaId}/usuarios/{usuarioId}/documentos")
public class DocumentoControle {
    private final DocumentoServico servico;
    private final AdicionadorLinkDocumento links;
    public DocumentoControle(DocumentoServico servico, AdicionadorLinkDocumento links) { this.servico = servico; this.links = links; }
    @GetMapping
    public CollectionModel<EntityModel<DocumentoResposta>> listar(@PathVariable @Positive long empresaId,
            @PathVariable @Positive long usuarioId) {
        return links.adicionarLink(empresaId, usuarioId, servico.listar(empresaId, usuarioId));
    }
    @GetMapping("/{id}")
    public EntityModel<DocumentoResposta> obter(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id) {
        return links.adicionarLink(empresaId, usuarioId, servico.obter(empresaId, usuarioId, id));
    }
    @PostMapping
    public ResponseEntity<EntityModel<DocumentoResposta>> cadastrar(@PathVariable @Positive long empresaId,
            @PathVariable @Positive long usuarioId, @RequestBody @Valid DocumentoDados dados) {
        var modelo = links.adicionarLink(empresaId, usuarioId, servico.cadastrar(empresaId, usuarioId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }
    @PutMapping("/{id}")
    public EntityModel<DocumentoResposta> atualizar(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id, @RequestBody @Valid DocumentoDados dados) {
        return links.adicionarLink(empresaId, usuarioId, servico.atualizar(empresaId, usuarioId, id, dados));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id) {
        servico.excluir(empresaId, usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
