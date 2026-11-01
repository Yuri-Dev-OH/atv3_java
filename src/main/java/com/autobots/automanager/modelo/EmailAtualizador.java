package com.autobots.automanager.modelo;

import com.autobots.automanager.dto.Dados.EmailDados;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Usuario;
import org.springframework.stereotype.Component;

@Component
public class EmailAtualizador {
    public void atualizar(Email entidade, EmailDados dados) { entidade.setEndereco(dados.endereco()); }

    /**
     * Cria um e-mail e o adiciona ao usuário. Repetição no mesmo usuário é barrada pelo banco.
     *
     * @param usuario dono do e-mail
     * @param dados e-mail informado
     * @return e-mail adicionado, ainda sem id até o próximo flush
     */
    public Email adicionar(Usuario usuario, EmailDados dados) {
        var email = new Email();
        atualizar(email, dados);
        usuario.getEmails().add(email);
        return email;
    }
}
