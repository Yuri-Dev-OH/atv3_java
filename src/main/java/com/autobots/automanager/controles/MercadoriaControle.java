package com.autobots.automanager.controles;

import com.autobots.automanager.dto.Dados.MercadoriaDados;
import com.autobots.automanager.dto.Respostas.MercadoriaResposta;
import com.autobots.automanager.modelo.AdicionadorLinkMercadoria;
import com.autobots.automanager.servicos.MercadoriaServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas/{empresaId}/mercadorias")
public class MercadoriaControle {
    private final MercadoriaServico servico;
    private final AdicionadorLinkMercadoria links;
    public MercadoriaControle(MercadoriaServico servico, AdicionadorLinkMercadoria links) { this.servico = servico; this.links = links; }

    @GetMapping
    public PagedModel<EntityModel<MercadoriaResposta>> listar(@PathVariable @Positive long empresaId,
            @RequestParam(required = false) @Positive Long fornecedorId,
            @RequestParam(defaultValue = "0") @Min(0) Integer pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer tamanho) {
        return links.adicionarLink(empresaId, fornecedorId, servico.listar(empresaId, fornecedorId, pagina, tamanho));
    }
    @GetMapping("/{id}")
    public EntityModel<MercadoriaResposta> obter(@PathVariable @Positive long empresaId, @PathVariable @Positive long id) {
        return links.adicionarLink(empresaId, servico.obter(empresaId, id));
    }
    @PostMapping
    public ResponseEntity<EntityModel<MercadoriaResposta>> cadastrar(@PathVariable @Positive long empresaId,
            @RequestBody @Valid MercadoriaDados dados) {
        var modelo = links.adicionarLink(empresaId, servico.cadastrar(empresaId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }
    @PutMapping("/{id}")
    public EntityModel<MercadoriaResposta> atualizar(@PathVariable @Positive long empresaId, @PathVariable @Positive long id,
            @RequestBody @Valid MercadoriaDados dados) {
        return links.adicionarLink(empresaId, servico.atualizar(empresaId, id, dados));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive long empresaId, @PathVariable @Positive long id) {
        servico.excluir(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
