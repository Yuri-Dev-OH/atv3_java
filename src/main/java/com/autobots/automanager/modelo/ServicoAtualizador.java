package com.autobots.automanager.modelo;

import com.autobots.automanager.dto.Dados.ServicoDados;
import com.autobots.automanager.entidades.Servico;
import org.springframework.stereotype.Component;

@Component
public class ServicoAtualizador {
    public void atualizar(Servico entidade, ServicoDados dados) {
        entidade.setNome(dados.nome());
        entidade.setDescricao(dados.descricao());
        entidade.setValor(dados.valor());
    }
}
