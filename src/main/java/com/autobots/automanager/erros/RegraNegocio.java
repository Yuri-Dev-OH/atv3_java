package com.autobots.automanager.erros;

/** Entrada bem formada que viola uma regra do negócio, como vender a quem não tem o perfil de cliente (HTTP 422). */
public class RegraNegocio extends RuntimeException {
    public RegraNegocio(String mensagem) { super(mensagem); }
}
