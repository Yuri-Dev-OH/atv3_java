package com.autobots.automanager;

import com.autobots.automanager.seguranca.CodificadorSenha;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CodificadorSenhaTest {
    private final CodificadorSenha codificador = new CodificadorSenha(1000);

    @Test void geraHashComSalAleatorioEConfereASenha() {
        String primeiro = codificador.codificar("segredo-123");
        String segundo = codificador.codificar("segredo-123");
        assertThat(primeiro).startsWith("pbkdf2-sha256$1000$").isNotEqualTo(segundo).doesNotContain("segredo-123");
        assertThat(codificador.confere("segredo-123", primeiro)).isTrue();
        assertThat(codificador.confere("segredo-124", primeiro)).isFalse();
    }

    @Test void confereHashGeradoComOutraQuantidadeDeIteracoes() {
        String antigo = new CodificadorSenha(500).codificar("segredo-123");
        assertThat(codificador.confere("segredo-123", antigo)).isTrue();
    }

    @Test void recusaHashDesconhecidoEIteracoesInvalidas() {
        assertThat(codificador.confere("x", "texto-puro")).isFalse();
        assertThat(codificador.confere("x", "bcrypt$1$a$b")).isFalse();
        assertThatThrownBy(() -> new CodificadorSenha(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
