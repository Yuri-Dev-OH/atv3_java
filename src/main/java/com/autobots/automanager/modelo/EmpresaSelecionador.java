package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.erros.RecursoNaoEncontrado;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import org.springframework.stereotype.Component;

@Component
public class EmpresaSelecionador {
    private final EmpresaRepositorio repositorio;
    public EmpresaSelecionador(EmpresaRepositorio repositorio) { this.repositorio = repositorio; }
    public Empresa selecionar(long id) {
        return repositorio.findById(id).orElseThrow(() -> new RecursoNaoEncontrado("Empresa não encontrada."));
    }
    public Empresa bloquear(long id) {
        return repositorio.bloquearPorId(id).orElseThrow(() -> new RecursoNaoEncontrado("Empresa não encontrada."));
    }
}
