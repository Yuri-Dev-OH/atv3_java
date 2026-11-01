package com.autobots.automanager.controles;

import com.autobots.automanager.dto.Dados.ServicoDados;
import com.autobots.automanager.dto.Respostas.ServicoResposta;
import com.autobots.automanager.modelo.AdicionadorLinkServico;
import com.autobots.automanager.servicos.ServicoServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas/{empresaId}/servicos")
public class ServicoControle {
    private final ServicoServico servico;
    private final AdicionadorLinkServico links;
    public ServicoControle(ServicoServico servico, AdicionadorLinkServico links) { this.servico = servico; this.links = links; }

    @GetMapping
    public PagedModel<EntityModel<ServicoResposta>> listar(@PathVariable @Positive long empresaId,
            @RequestParam(defaultValue = "0") @Min(0) Integer pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer tamanho) {
        return links.adicionarLink(empresaId, servico.listar(empresaId, pagina, tamanho));
    }
    @GetMapping("/{id}")
    public EntityModel<ServicoResposta> obter(@PathVariable @Positive long empresaId, @PathVariable @Positive long id) {
        return links.adicionarLink(empresaId, servico.obter(empresaId, id));
    }
    @PostMapping
    public ResponseEntity<EntityModel<ServicoResposta>> cadastrar(@PathVariable @Positive long empresaId,
            @RequestBody @Valid ServicoDados dados) {
        var modelo = links.adicionarLink(empresaId, servico.cadastrar(empresaId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }
    @PutMapping("/{id}")
    public EntityModel<ServicoResposta> atualizar(@PathVariable @Positive long empresaId, @PathVariable @Positive long id,
            @RequestBody @Valid ServicoDados dados) {
        return links.adicionarLink(empresaId, servico.atualizar(empresaId, id, dados));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive long empresaId, @PathVariable @Positive long id) {
        servico.excluir(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
