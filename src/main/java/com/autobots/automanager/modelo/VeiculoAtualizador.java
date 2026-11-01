package com.autobots.automanager.modelo;

import com.autobots.automanager.dto.Dados.VeiculoDados;
import com.autobots.automanager.entidades.Veiculo;
import org.springframework.stereotype.Component;

@Component
public class VeiculoAtualizador {
    public void atualizar(Veiculo entidade, VeiculoDados dados) {
        entidade.setTipo(dados.tipo());
        entidade.setModelo(dados.modelo());
        entidade.setPlaca(dados.placa());
    }
}
