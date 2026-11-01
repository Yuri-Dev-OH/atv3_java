package com.autobots.automanager.controles;

import com.autobots.automanager.dto.Dados.TelefoneDados;
import com.autobots.automanager.dto.Respostas.TelefoneResposta;
import com.autobots.automanager.modelo.AdicionadorLinkTelefone;
import com.autobots.automanager.modelo.Dono;
import com.autobots.automanager.servicos.TelefoneServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas/{empresaId}/usuarios/{usuarioId}/telefones")
public class UsuarioTelefoneControle {
    private final TelefoneServico servico;
    private final AdicionadorLinkTelefone links;
    public UsuarioTelefoneControle(TelefoneServico servico, AdicionadorLinkTelefone links) { this.servico = servico; this.links = links; }
    @GetMapping
    public CollectionModel<EntityModel<TelefoneResposta>> listar(@PathVariable @Positive long empresaId,
            @PathVariable @Positive long usuarioId) {
        var dono = Dono.usuario(empresaId, usuarioId);
        return links.adicionarLink(dono, servico.listar(dono));
    }
    @GetMapping("/{id}")
    public EntityModel<TelefoneResposta> obter(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id) {
        var dono = Dono.usuario(empresaId, usuarioId);
        return links.adicionarLink(dono, servico.obter(dono, id));
    }
    @PostMapping
    public ResponseEntity<EntityModel<TelefoneResposta>> cadastrar(@PathVariable @Positive long empresaId,
            @PathVariable @Positive long usuarioId, @RequestBody @Valid TelefoneDados dados) {
        var dono = Dono.usuario(empresaId, usuarioId);
        var modelo = links.adicionarLink(dono, servico.cadastrar(dono, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }
    @PutMapping("/{id}")
    public EntityModel<TelefoneResposta> atualizar(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id, @RequestBody @Valid TelefoneDados dados) {
        var dono = Dono.usuario(empresaId, usuarioId);
        return links.adicionarLink(dono, servico.atualizar(dono, id, dados));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id) {
        servico.excluir(Dono.usuario(empresaId, usuarioId), id);
        return ResponseEntity.noContent().build();
    }
}
