package com.autobots.automanager.modelo;

/**
 * Identifica quem possui um endereço ou telefone: a empresa ({@code usuarioId} nulo) ou um de seus usuários.
 *
 * @param empresaId empresa da URL
 * @param usuarioId usuário da URL, ou {@code null} quando o titular é a própria empresa
 */
public record Dono(long empresaId, Long usuarioId) {
    public static Dono empresa(long empresaId) { return new Dono(empresaId, null); }
    public static Dono usuario(long empresaId, long usuarioId) { return new Dono(empresaId, usuarioId); }
    public boolean ehEmpresa() { return usuarioId == null; }
    /** Complemento usado nas mensagens de erro, como "para esta empresa". */
    public String descricao() { return ehEmpresa() ? "para esta empresa" : "para este usuário"; }
}
