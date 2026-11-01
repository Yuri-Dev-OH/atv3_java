package com.autobots.automanager.controles;

import com.autobots.automanager.dto.Dados.EnderecoDados;
import com.autobots.automanager.dto.Respostas.EnderecoResposta;
import com.autobots.automanager.modelo.AdicionadorLinkEndereco;
import com.autobots.automanager.modelo.Dono;
import com.autobots.automanager.servicos.EnderecoServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas/{empresaId}/usuarios/{usuarioId}/endereco")
public class UsuarioEnderecoControle {
    private final EnderecoServico servico;
    private final AdicionadorLinkEndereco links;
    public UsuarioEnderecoControle(EnderecoServico servico, AdicionadorLinkEndereco links) { this.servico = servico; this.links = links; }
    @GetMapping
    public EntityModel<EnderecoResposta> obter(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId) {
        var dono = Dono.usuario(empresaId, usuarioId);
        return links.adicionarLink(dono, servico.obter(dono));
    }
    @PostMapping
    public ResponseEntity<EntityModel<EnderecoResposta>> cadastrar(@PathVariable @Positive long empresaId,
            @PathVariable @Positive long usuarioId, @RequestBody @Valid EnderecoDados dados) {
        var dono = Dono.usuario(empresaId, usuarioId);
        var modelo = links.adicionarLink(dono, servico.cadastrar(dono, dados));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }
    @PutMapping
    public EntityModel<EnderecoResposta> atualizar(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId,
            @RequestBody @Valid EnderecoDados dados) {
        var dono = Dono.usuario(empresaId, usuarioId);
        return links.adicionarLink(dono, servico.atualizar(dono, dados));
    }
    @DeleteMapping
    public ResponseEntity<Void> excluir(@PathVariable @Positive long empresaId, @PathVariable @Positive long usuarioId) {
        servico.excluir(Dono.usuario(empresaId, usuarioId));
        return ResponseEntity.noContent().build();
    }
}
