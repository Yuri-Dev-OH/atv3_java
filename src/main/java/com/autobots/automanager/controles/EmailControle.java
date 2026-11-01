package com.autobots.automanager.controles;

import com.autobots.automanager.dto.Dados.EmailDados;
import com.autobots.automanager.dto.Respostas.EmailResposta;
import com.autobots.automanager.modelo.AdicionadorLinkEmail;
import com.autobots.automanager.servicos.EmailServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas/{empresaId}/usuarios/{usuarioId}/emails")
public class EmailControle {
    private final EmailServico servico;
    private final AdicionadorLinkEmail links;
    public EmailControle(EmailServico servico, AdicionadorLinkEmail links) { this.servico = servico; this.links = links; }
    @GetMapping
    public CollectionModel<EntityModel<EmailResposta>> listar(@PathVariable @Positive long empresaId,
            @PathVariable @Positive long usuarioId) {
        return links.adicionarLink(empresaId, usuarioId, servico.listar(empresaId, usuarioId));
    }
    @GetMapping("/{id}")
    public EntityModel<EmailResposta> obter(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id) {
        return links.adicionarLink(empresaId, usuarioId, servico.obter(empresaId, usuarioId, id));
    }
    @PostMapping
    public ResponseEntity<EntityModel<EmailResposta>> cadastrar(@PathVariable @Positive long empresaId,
            @PathVariable @Positive long usuarioId, @RequestBody @Valid EmailDados dados) {
        var modelo = links.adicionarLink(empresaId, usuarioId, servico.cadastrar(empresaId, usuarioId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }
    @PutMapping("/{id}")
    public EntityModel<EmailResposta> atualizar(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id, @RequestBody @Valid EmailDados dados) {
        return links.adicionarLink(empresaId, usuarioId, servico.atualizar(empresaId, usuarioId, id, dados));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id) {
        servico.excluir(empresaId, usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
