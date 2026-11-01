package com.autobots.automanager.controles;

import com.autobots.automanager.dto.Dados.VeiculoDados;
import com.autobots.automanager.dto.Respostas.VeiculoResposta;
import com.autobots.automanager.modelo.AdicionadorLinkVeiculo;
import com.autobots.automanager.servicos.VeiculoServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas/{empresaId}/usuarios/{usuarioId}/veiculos")
public class VeiculoControle {
    private final VeiculoServico servico;
    private final AdicionadorLinkVeiculo links;
    public VeiculoControle(VeiculoServico servico, AdicionadorLinkVeiculo links) { this.servico = servico; this.links = links; }
    @GetMapping
    public CollectionModel<EntityModel<VeiculoResposta>> listar(@PathVariable @Positive long empresaId,
            @PathVariable @Positive long usuarioId) {
        return links.adicionarLink(empresaId, usuarioId, servico.listar(empresaId, usuarioId));
    }
    @GetMapping("/{id}")
    public EntityModel<VeiculoResposta> obter(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id) {
        return links.adicionarLink(empresaId, usuarioId, servico.obter(empresaId, usuarioId, id));
    }
    @PostMapping
    public ResponseEntity<EntityModel<VeiculoResposta>> cadastrar(@PathVariable @Positive long empresaId,
            @PathVariable @Positive long usuarioId, @RequestBody @Valid VeiculoDados dados) {
        var modelo = links.adicionarLink(empresaId, usuarioId, servico.cadastrar(empresaId, usuarioId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }
    @PutMapping("/{id}")
    public EntityModel<VeiculoResposta> atualizar(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id, @RequestBody @Valid VeiculoDados dados) {
        return links.adicionarLink(empresaId, usuarioId, servico.atualizar(empresaId, usuarioId, id, dados));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @PathVariable @Positive long id) {
        servico.excluir(empresaId, usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
