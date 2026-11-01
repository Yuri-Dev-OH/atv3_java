package com.autobots.automanager.modelo;

import com.autobots.automanager.dto.Dados.MercadoriaDados;
import com.autobots.automanager.entidades.Mercadoria;
import org.springframework.stereotype.Component;

/** Copia os campos simples da mercadoria; o fornecedor é validado e atribuído pelo serviço. */
@Component
public class MercadoriaAtualizador {
    public void atualizar(Mercadoria entidade, MercadoriaDados dados) {
        entidade.setNome(dados.nome());
        entidade.setDescricao(dados.descricao());
        entidade.setFabricacao(dados.fabricacao());
        entidade.setValidade(dados.validade());
        entidade.setQuantidade(dados.quantidade());
        entidade.setValor(dados.valor());
    }
}
