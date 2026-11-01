package com.autobots.automanager.controles;

import com.autobots.automanager.dto.Dados.*;
import com.autobots.automanager.dto.Respostas.UsuarioResposta;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelo.AdicionadorLinkUsuario;
import com.autobots.automanager.servicos.UsuarioServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas/{empresaId}/usuarios")
public class UsuarioControle {
    private final UsuarioServico servico;
    private final AdicionadorLinkUsuario links;
    public UsuarioControle(UsuarioServico servico, AdicionadorLinkUsuario links) { this.servico = servico; this.links = links; }

    @GetMapping
    public PagedModel<EntityModel<UsuarioResposta>> listar(@PathVariable @Positive long empresaId,
            @RequestParam(required = false) PerfilUsuario perfil,
            @RequestParam(defaultValue = "0") @Min(0) Integer pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer tamanho) {
        return links.adicionarLink(empresaId, perfil, servico.listar(empresaId, perfil, pagina, tamanho));
    }
    @GetMapping("/{id}")
    public EntityModel<UsuarioResposta> obter(@PathVariable @Positive long empresaId, @PathVariable @Positive long id) {
        return links.adicionarLink(empresaId, servico.obter(empresaId, id));
    }
    @PostMapping
    public ResponseEntity<EntityModel<UsuarioResposta>> cadastrar(@PathVariable @Positive long empresaId,
            @RequestBody @Valid UsuarioCadastro dados) {
        var modelo = links.adicionarLink(empresaId, servico.cadastrar(empresaId, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }
    @PutMapping("/{id}")
    public EntityModel<UsuarioResposta> atualizar(@PathVariable @Positive long empresaId, @PathVariable @Positive long id,
            @RequestBody @Valid UsuarioDados dados) {
        return links.adicionarLink(empresaId, servico.atualizar(empresaId, id, dados));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive long empresaId, @PathVariable @Positive long id) {
        servico.excluir(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
