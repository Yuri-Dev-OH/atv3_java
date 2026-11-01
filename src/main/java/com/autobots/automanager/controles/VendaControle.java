package com.autobots.automanager.controles;

import com.autobots.automanager.dto.Dados.VendaDados;
import com.autobots.automanager.dto.Respostas.VendaResposta;
import com.autobots.automanager.modelo.AdicionadorLinkVenda;
import com.autobots.automanager.servicos.VendaServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas/{empresaId}/vendas")
public class VendaControle {
    private final VendaServico servico;
    private final AdicionadorLinkVenda links;
    public VendaControle(VendaServico servico, AdicionadorLinkVenda links) { this.servico = servico; this.links = links; }

    @GetMapping
    public PagedModel<EntityModel<VendaResposta>> listar(@PathVariable @Positive long empresaId,
            @RequestParam(required = false) @Positive Long clienteId,
            @RequestParam(required = false) @Positive Long funcionarioId,
            @RequestParam(required = false) @Positive Long veiculoId,
            @RequestParam(defaultValue = "0") @Min(0) Integer pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer tamanho) {
        var vendas = servico.listar(empresaId, clienteId, funcionarioId, veiculoId, pagina, tamanho);
        return links.adicionarLink(empresaId, clienteId, funcionarioId, veiculoId, vendas);
    }
    @GetMapping("/{id}")
    public EntityModel<VendaResposta> obter(@PathVariable @Positive long empresaId, @PathVariable @Positive long id) {
        return links.adicionarLink(empresaId, servico.obter(empresaId, id));
    }
    @PostMapping
    public ResponseEntity<EntityModel<VendaResposta>> cadastrar(@PathVariable @Positive long empresaId,
            @RequestBody @Valid VendaDados dados) {
        var modelo = links.adicionarLink(empresaId, servico.cadastrar(empresaId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }
    @PutMapping("/{id}")
    public EntityModel<VendaResposta> atualizar(@PathVariable @Positive long empresaId, @PathVariable @Positive long id,
            @RequestBody @Valid VendaDados dados) {
        return links.adicionarLink(empresaId, servico.atualizar(empresaId, id, dados));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive long empresaId, @PathVariable @Positive long id) {
        servico.excluir(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
