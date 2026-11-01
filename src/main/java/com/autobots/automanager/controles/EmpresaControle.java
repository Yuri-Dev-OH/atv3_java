package com.autobots.automanager.controles;

import com.autobots.automanager.dto.Dados.*;
import com.autobots.automanager.dto.Respostas.EmpresaResposta;
import com.autobots.automanager.modelo.AdicionadorLinkEmpresa;
import com.autobots.automanager.servicos.EmpresaServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas")
public class EmpresaControle {
    private final EmpresaServico servico;
    private final AdicionadorLinkEmpresa links;
    public EmpresaControle(EmpresaServico servico, AdicionadorLinkEmpresa links) { this.servico = servico; this.links = links; }

    @GetMapping
    public PagedModel<EntityModel<EmpresaResposta>> listar(@RequestParam(defaultValue = "0") @Min(0) Integer pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer tamanho) {
        return links.adicionarLink(servico.listar(pagina, tamanho));
    }
    @GetMapping("/{id}")
    public EntityModel<EmpresaResposta> obter(@PathVariable @Positive long id) { return links.adicionarLink(servico.obter(id)); }
    @PostMapping
    public ResponseEntity<EntityModel<EmpresaResposta>> cadastrar(@RequestBody @Valid EmpresaCadastro dados) {
        var modelo = links.adicionarLink(servico.cadastrar(dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }
    @PutMapping("/{id}")
    public EntityModel<EmpresaResposta> atualizar(@PathVariable @Positive long id, @RequestBody @Valid EmpresaDados dados) {
        return links.adicionarLink(servico.atualizar(id, dados));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
