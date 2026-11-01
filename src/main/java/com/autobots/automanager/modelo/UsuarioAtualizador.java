package com.autobots.automanager.modelo;

import com.autobots.automanager.dto.Dados.UsuarioDados;
import com.autobots.automanager.entidades.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioAtualizador {
    public void atualizar(Usuario usuario, UsuarioDados dados) {
        usuario.setNome(dados.nome());
        usuario.setNomeSocial(dados.nomeSocial());
        usuario.setDataNascimento(dados.dataNascimento());
        usuario.getPerfis().retainAll(dados.perfis());
        usuario.getPerfis().addAll(dados.perfis());
    }
}
