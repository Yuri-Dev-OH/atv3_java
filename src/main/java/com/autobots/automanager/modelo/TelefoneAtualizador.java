package com.autobots.automanager.modelo;

import com.autobots.automanager.dto.Dados.TelefoneDados;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Titular;
import com.autobots.automanager.erros.Conflito;
import org.springframework.stereotype.Component;

@Component
public class TelefoneAtualizador {
    public void atualizar(Telefone entidade, TelefoneDados dados) {
        entidade.setDdd(dados.ddd());
        entidade.setNumero(dados.numero());
    }

    /**
     * Cria um telefone e o adiciona ao titular, recusando número repetido.
     *
     * @param titular empresa ou usuário
     * @param dados telefone informado
     * @return telefone adicionado, ainda sem id até o próximo flush
     */
    public Telefone adicionar(Titular titular, TelefoneDados dados) {
        verificarDuplicado(titular, dados, null);
        var telefone = new Telefone();
        atualizar(telefone, dados);
        titular.getTelefones().add(telefone);
        return telefone;
    }

    /**
     * Recusa telefone igual a outro do mesmo titular.
     *
     * @param titular empresa ou usuário
     * @param dados telefone informado
     * @param atual telefone em edição, ignorado na comparação; {@code null} em cadastros
     */
    public void verificarDuplicado(Titular titular, TelefoneDados dados, Telefone atual) {
        boolean duplicado = titular.getTelefones().stream().anyMatch(t ->
            t != atual && t.getDdd().equals(dados.ddd()) && t.getNumero().equals(dados.numero()));
        if (duplicado) throw new Conflito("Telefone já cadastrado para este titular.");
    }
}
