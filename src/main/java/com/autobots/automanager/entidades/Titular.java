package com.autobots.automanager.entidades;

import java.util.List;

/** Cadastro que possui endereço e telefones: uma empresa ou um usuário. */
public interface Titular {
    Endereco getEndereco();
    void setEndereco(Endereco endereco);
    List<Telefone> getTelefones();
}
