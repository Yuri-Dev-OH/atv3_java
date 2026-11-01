package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Titular;
import org.springframework.stereotype.Component;

/** Resolve o {@link Dono} da URL para a empresa ou o usuário correspondente. */
@Component
public class TitularSelecionador {
    private final EmpresaSelecionador empresas;
    private final UsuarioSelecionador usuarios;
    public TitularSelecionador(EmpresaSelecionador empresas, UsuarioSelecionador usuarios) {
        this.empresas = empresas; this.usuarios = usuarios;
    }
    public Titular selecionar(Dono dono) {
        return dono.ehEmpresa() ? empresas.selecionar(dono.empresaId()) : usuarios.selecionar(dono.empresaId(), dono.usuarioId());
    }
    public Titular bloquear(Dono dono) {
        return dono.ehEmpresa() ? empresas.bloquear(dono.empresaId()) : usuarios.bloquear(dono.empresaId(), dono.usuarioId());
    }
}
