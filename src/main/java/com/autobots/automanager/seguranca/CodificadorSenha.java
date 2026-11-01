package com.autobots.automanager.seguranca;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Gera e confere hashes de senha com PBKDF2-HMAC-SHA256, disponível no próprio JDK.
 *
 * <p>O hash gravado tem o formato {@code pbkdf2-sha256$iteracoes$sal$hash}, com sal aleatório de 16 bytes em Base64.
 * Guardar as iterações em cada hash permite aumentá-las depois sem invalidar senhas antigas.
 */
@Component
public class CodificadorSenha {
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIXO = "pbkdf2-sha256";
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private final SecureRandom aleatorio = new SecureRandom();
    private final int iteracoes;

    /**
     * Cria o codificador.
     *
     * @param iteracoes rodadas do PBKDF2; o padrão de 600.000 segue a recomendação OWASP para SHA-256
     */
    public CodificadorSenha(@Value("${automanager.senha.iteracoes:600000}") int iteracoes) {
        if (iteracoes < 1) throw new IllegalArgumentException("automanager.senha.iteracoes deve ser positivo.");
        this.iteracoes = iteracoes;
    }

    /**
     * Gera o hash de uma senha.
     *
     * @param senha senha em texto puro
     * @return hash no formato {@code pbkdf2-sha256$iteracoes$sal$hash}
     */
    public String codificar(String senha) {
        var sal = new byte[BYTES_SAL];
        aleatorio.nextBytes(sal);
        var codificador = Base64.getEncoder();
        return String.join("$", PREFIXO, Integer.toString(iteracoes), codificador.encodeToString(sal),
            codificador.encodeToString(derivar(senha, sal, iteracoes)));
    }

    /**
     * Confere uma senha com um hash gerado por {@link #codificar(String)}, em tempo constante.
     *
     * @param senha senha em texto puro
     * @param hash hash gravado
     * @return {@code true} se a senha corresponde ao hash
     */
    public boolean confere(String senha, String hash) {
        var partes = hash.split("\\$");
        if (partes.length != 4 || !PREFIXO.equals(partes[0])) return false;
        var decodificador = Base64.getDecoder();
        var esperado = decodificador.decode(partes[3]);
        return MessageDigest.isEqual(esperado, derivar(senha, decodificador.decode(partes[2]), Integer.parseInt(partes[1])));
    }

    private static byte[] derivar(String senha, byte[] sal, int iteracoes) {
        var especificacao = new PBEKeySpec(senha.toCharArray(), sal, iteracoes, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacao).getEncoded();
        } catch (GeneralSecurityException erro) {
            throw new IllegalStateException("PBKDF2 indisponível na JVM.", erro);
        } finally {
            especificacao.clearPassword();
        }
    }
}
