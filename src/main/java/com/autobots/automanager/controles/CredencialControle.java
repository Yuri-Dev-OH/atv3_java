package com.autobots.automanager.controles;

import com.autobots.automanager.dto.Dados.CredencialDados;
import com.autobots.automanager.dto.Respostas.CredencialResposta;
import com.autobots.automanager.modelo.AdicionadorLinkCredencial;
import com.autobots.automanager.servicos.CredencialServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas/{empresaId}/usuarios/{usuarioId}/credenciais")
public class CredencialControle {
    private final CredencialServico servico;
    private final AdicionadorLinkCredencial links;
    public CredencialControle(CredencialServico servico, AdicionadorLinkCredencial links) { this.servico = servico; this.links = links; }
    @GetMapping
    public CollectionModel<EntityModel<CredencialResposta>> listar(@PathVariable @Positive long empresaId,
            @PathVariable @Positive long usuarioId) {
        return links.adicionarLink(empresaId, usuarioId, servico.listar(empresaId, usuarioId));
    }
    @GetMapping("/{id}")
    public EntityModel<CredencialResposta> obter(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id) {
        return links.adicionarLink(empresaId, usuarioId, servico.obter(empresaId, usuarioId, id));
    }
    @PostMapping
    public ResponseEntity<EntityModel<CredencialResposta>> cadastrar(@PathVariable @Positive long empresaId,
            @PathVariable @Positive long usuarioId, @RequestBody @Valid CredencialDados dados) {
        var modelo = links.adicionarLink(empresaId, usuarioId, servico.cadastrar(empresaId, usuarioId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }
    @PutMapping("/{id}")
    public EntityModel<CredencialResposta> atualizar(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id, @RequestBody @Valid CredencialDados dados) {
        return links.adicionarLink(empresaId, usuarioId, servico.atualizar(empresaId, usuarioId, id, dados));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id) {
        servico.excluir(empresaId, usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
