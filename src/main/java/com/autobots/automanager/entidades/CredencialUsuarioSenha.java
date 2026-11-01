package com.autobots.automanager.entidades;

import com.autobots.automanager.enumeracoes.TipoCredencial;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Acesso por nome de usuário e senha. A senha é gravada somente como hash (ver {@code CodificadorSenha}). */
@Getter
@Setter
@Entity
public class CredencialUsuarioSenha extends Credencial {
    @Column(length = 50, nullable = false, unique = true)
    private String nomeUsuario;
    @Column(length = 200, nullable = false)
    private String senha;

    @Override
    public TipoCredencial getTipo() { return TipoCredencial.USUARIO_SENHA; }
}
