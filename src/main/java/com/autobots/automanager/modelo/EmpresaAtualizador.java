package com.autobots.automanager.modelo;

import com.autobots.automanager.dto.Dados.EmpresaDados;
import com.autobots.automanager.entidades.Empresa;
import org.springframework.stereotype.Component;

@Component
public class EmpresaAtualizador {
    public void atualizar(Empresa empresa, EmpresaDados dados) {
        empresa.setRazaoSocial(dados.razaoSocial());
        empresa.setNomeFantasia(dados.nomeFantasia());
    }
}
