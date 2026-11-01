package com.autobots.automanager;

import java.util.List;
import java.util.Map;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.repositorios.CredencialRepositorio;
import com.autobots.automanager.seguranca.CodificadorSenha;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CRUDs de empresa, usuário e seus sub-recursos: endereço, telefones, documentos, e-mails e credenciais.
 *
 * <p>Cobre as funcionalidades da ATVIII "cadastro de empresa", "associação de usuários a uma empresa", "tipos de
 * usuário" e "credencial de acesso".
 */
class CadastroIntegrationTest extends ApiTeste {
    @Autowired CredencialRepositorio credenciais;
    @Autowired CodificadorSenha codificador;

    @Test void empresaTemCrudCompletoComEnderecoETelefones() throws Exception {
        var cadastro = Map.of("razaoSocial", " Car service toyota ltda ", "nomeFantasia", "Car service",
            "endereco", ENDERECO, "telefones", List.of(Map.of("ddd", "11", "numero", "986454527")));
        var empresa = enviar(post("/empresas"), cadastro, 201);
        long id = empresa.get("id").asLong();
        String uri = "/empresas/" + id;
        assertThat(empresa.get("razaoSocial").asText()).isEqualTo("Car service toyota ltda");
        assertThat(empresa.get("cadastro").isTextual()).isTrue();
        assertThat(empresa.at("/endereco/cidade").asText()).isEqualTo("São José dos Campos");
        assertThat(empresa.at("/telefones/0/numero").asText()).isEqualTo("986454527");

        var alterada = enviar(put(uri), Map.of("razaoSocial", "Toyota Brasil", "nomeFantasia", "Toyota"), 200);
        assertThat(alterada.get("nomeFantasia").asText()).isEqualTo("Toyota");
        assertThat(alterada.at("/telefones").size()).isEqualTo(1);

        enviar(put(uri + "/endereco"), Map.of("cidade", "Campinas", "rua", "Rua Dois", "numero", "2", "codigoPostal", "13010000"), 200);
        enviar(post(uri + "/endereco"), ENDERECO, 409);
        long telefone = enviar(post(uri + "/telefones"), Map.of("ddd", "12", "numero", "39998888"), 201).get("id").asLong();
        enviar(post(uri + "/telefones"), Map.of("ddd", "12", "numero", "39998888"), 409);
        enviar(put(uri + "/telefones/" + telefone), Map.of("ddd", "12", "numero", "39997777"), 200);
        assertThat(obter(uri + "/telefones").at("/_embedded/telefones").size()).isEqualTo(2);
        assertThat(obter(uri).at("/endereco/cidade").asText()).isEqualTo("Campinas");

        excluir(uri + "/telefones/" + telefone, 204);
        excluir(uri + "/endereco", 204);
        excluir(uri + "/endereco", 404);
        assertThat(obter(uri).get("endereco").isNull()).isTrue();
        assertThat(obter("/empresas").at("/page/totalElements").asLong()).isEqualTo(1);
        excluir(uri, 204);
        mvc.perform(get(uri)).andExpect(status().isNotFound());
    }

    @Test void empresaComUsuariosNaoPodeSerExcluida() throws Exception {
        long empresa = criarEmpresa();
        long usuario = criarUsuario(empresa, "Ana", "CLIENTE");
        var conflito = enviar(delete("/empresas/" + empresa), Map.of(), 409);
        assertThat(conflito.get("title").asText()).isEqualTo("Conflito.");
        excluir("/empresas/" + empresa + "/usuarios/" + usuario, 204);
        excluir("/empresas/" + empresa, 204);
    }

    @Test void usuarioTemCrudCompletoEPertenceAEmpresa() throws Exception {
        long empresa = criarEmpresa();
        String usuarios = "/empresas/" + empresa + "/usuarios";
        var cadastro = Map.of("nome", "Pedro Alcântara", "nomeSocial", "Dom Pedro", "dataNascimento", "1990-01-01",
            "perfis", List.of("CLIENTE", "FUNCIONARIO"), "endereco", ENDERECO,
            "telefones", List.of(Map.of("ddd", "11", "numero", "985463372")),
            "documentos", List.of(documento("CPF", "85647381922")), "emails", List.of(Map.of("endereco", " Pedro@Exemplo.com ")));
        var usuario = enviar(post(usuarios), cadastro, 201);
        long id = usuario.get("id").asLong();
        String uri = usuarios + "/" + id;
        assertThat(usuario.get("perfis").toString()).isEqualTo("[\"CLIENTE\",\"FUNCIONARIO\"]");
        assertThat(usuario.at("/emails/0/endereco").asText()).isEqualTo("pedro@exemplo.com");
        assertThat(usuario.at("/documentos/0/tipo").asText()).isEqualTo("CPF");
        assertThat(usuario.at("/documentos/0/dataEmissao").asText()).isEqualTo("2020-01-01");
        assertThat(usuario.has("credenciais")).isFalse();

        var alterado = enviar(put(uri), Map.of("nome", "Pedro II", "perfis", List.of("FORNECEDOR")), 200);
        assertThat(alterado.get("perfis").toString()).isEqualTo("[\"FORNECEDOR\"]");
        assertThat(alterado.get("dataNascimento").isNull()).isTrue();
        assertThat(alterado.at("/telefones").size()).isEqualTo(1);

        long outraEmpresa = criarEmpresa();
        mvc.perform(get("/empresas/" + outraEmpresa + "/usuarios/" + id)).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Usuário não encontrado nesta empresa."));
        mvc.perform(get("/empresas/" + outraEmpresa + "/usuarios/" + id + "/documentos")).andExpect(status().isNotFound());
        mvc.perform(get("/empresas/999999/usuarios")).andExpect(status().isNotFound());

        excluir(uri, 204);
        mvc.perform(get(uri)).andExpect(status().isNotFound());
    }

    @Test void listagemDeUsuariosFiltraPorPerfil() throws Exception {
        long empresa = criarEmpresa();
        criarUsuario(empresa, "Cliente", "CLIENTE");
        criarUsuario(empresa, "Funcionário", "FUNCIONARIO");
        criarUsuario(empresa, "Misto", "CLIENTE", "FORNECEDOR");
        criarUsuario(criarEmpresa(), "Cliente de outra empresa", "CLIENTE");
        String uri = "/empresas/" + empresa + "/usuarios";
        assertThat(obter(uri).at("/page/totalElements").asLong()).isEqualTo(3);
        var clientes = obter(uri + "?perfil=CLIENTE");
        assertThat(clientes.at("/_embedded/usuarios").findValuesAsText("nome")).containsExactly("Cliente", "Misto");
        assertThat(link(clientes, "self")).isEqualTo(HOST + uri + "?perfil=CLIENTE&pagina=0&tamanho=20");
        assertThat(obter(uri + "?perfil=FORNECEDOR").at("/page/totalElements").asLong()).isEqualTo(1);
        mvc.perform(get(uri + "?perfil=GERENTE")).andExpect(status().isBadRequest());
    }

    @Test void usuarioExigeNomeEPerfilValidos() throws Exception {
        String uri = "/empresas/" + criarEmpresa() + "/usuarios";
        var erro = enviar(post(uri), Map.of("nome", " ", "perfis", List.of()), 400);
        assertThat(erro.get("detalhes").toString()).contains("nome:", "perfis:");
        enviar(post(uri), Map.of("nome", "Ana", "perfis", List.of("ADMIN")), 400);
        enviar(post(uri), Map.of("nome", "Ana", "perfis", List.of("CLIENTE"), "id", 5), 400);
        enviar(post(uri), Map.of("nome", "Ana", "perfis", List.of("CLIENTE"), "dataNascimento", "2999-01-01"), 400);
    }

    @Test void documentosSaoUnicosPorEmpresa() throws Exception {
        long empresa = criarEmpresa();
        long ana = criarUsuario(empresa, "Ana", "CLIENTE");
        long bia = criarUsuario(empresa, "Bia", "CLIENTE");
        String documentos = "/empresas/" + empresa + "/usuarios/" + ana + "/documentos";
        long id = enviar(post(documentos), documento("CPF", "12345678900"), 201).get("id").asLong();
        enviar(post("/empresas/" + empresa + "/usuarios/" + bia + "/documentos"), documento("CPF", "12345678900"), 409);
        enviar(post(documentos), documento("RG", "12345678900"), 201);
        long outra = criarEmpresa();
        enviar(post("/empresas/" + outra + "/usuarios/" + criarUsuario(outra, "Ana", "CLIENTE") + "/documentos"),
            documento("CPF", "12345678900"), 201);

        var alterado = enviar(put(documentos + "/" + id), Map.of("tipo", "CNH", "numero", "999", "dataEmissao", "2021-05-05"), 200);
        assertThat(alterado.get("tipo").asText()).isEqualTo("CNH");
        enviar(post(documentos), Map.of("tipo", "CPF", "numero", "1"), 400);
        enviar(post(documentos), documento("TITULO", "1"), 400);
        assertThat(obter(documentos).at("/_embedded/documentos").size()).isEqualTo(2);
        excluir(documentos + "/" + id, 204);
        excluir(documentos + "/" + id, 404);
    }

    @Test void emailsTemCrudEValidacao() throws Exception {
        long empresa = criarEmpresa();
        String emails = "/empresas/" + empresa + "/usuarios/" + criarUsuario(empresa, "Ana", "CLIENTE") + "/emails";
        long id = enviar(post(emails), Map.of("endereco", "ana@exemplo.com"), 201).get("id").asLong();
        enviar(post(emails), Map.of("endereco", "ANA@exemplo.com"), 409);
        enviar(post(emails), Map.of("endereco", "sem-arroba"), 400);
        assertThat(enviar(put(emails + "/" + id), Map.of("endereco", "nova@exemplo.com"), 200).get("endereco").asText())
            .isEqualTo("nova@exemplo.com");
        assertThat(obter(emails + "/" + id).get("endereco").asText()).isEqualTo("nova@exemplo.com");
        excluir(emails + "/" + id, 204);
        assertThat(obter(emails).has("_embedded")).isFalse();
    }

    @Test void credencialGuardaSomenteHashDaSenha() throws Exception {
        long empresa = criarEmpresa();
        String uri = "/empresas/" + empresa + "/usuarios/" + criarUsuario(empresa, "Ana", "FUNCIONARIO") + "/credenciais";
        var cadastro = Map.of("tipo", "USUARIO_SENHA", "nomeUsuario", "ana.silva", "senha", "segredo-forte-1");
        var credencial = enviar(post(uri), cadastro, 201);
        long id = credencial.get("id").asLong();
        assertThat(credencial.get("nomeUsuario").asText()).isEqualTo("ana.silva");
        assertThat(credencial.get("inativo").asBoolean()).isFalse();
        assertThat(credencial.get("criacao").isTextual()).isTrue();
        assertThat(credencial.has("senha")).isFalse();
        assertThat(credencial.toString()).doesNotContain("segredo-forte-1");

        var hash = ((CredencialUsuarioSenha) credenciais.findById(id).orElseThrow()).getSenha();
        assertThat(hash).startsWith("pbkdf2-sha256$").doesNotContain("segredo-forte-1");
        assertThat(codificador.confere("segredo-forte-1", hash)).isTrue();

        enviar(put(uri + "/" + id), Map.of("tipo", "USUARIO_SENHA", "nomeUsuario", "ana.s", "inativo", true), 200);
        var mantida = (CredencialUsuarioSenha) credenciais.findById(id).orElseThrow();
        assertThat(mantida.getSenha()).isEqualTo(hash);
        assertThat(mantida.isInativo()).isTrue();
        enviar(put(uri + "/" + id), Map.of("tipo", "USUARIO_SENHA", "nomeUsuario", "ana.s", "senha", "outra-senha-2"), 200);
        assertThat(codificador.confere("outra-senha-2", ((CredencialUsuarioSenha) credenciais.findById(id).orElseThrow()).getSenha())).isTrue();
        assertThat(enviar(put(uri + "/" + id), Map.of("tipo", "CODIGO_BARRA", "codigo", 10), 422).get("title").asText())
            .isEqualTo("Regra de negócio violada.");
        excluir(uri + "/" + id, 204);
        assertThat(credenciais.count()).isZero();
    }

    @Test void credenciaisValidamCamposPorTipoEUnicidade() throws Exception {
        long empresa = criarEmpresa();
        String ana = "/empresas/" + empresa + "/usuarios/" + criarUsuario(empresa, "Ana", "FUNCIONARIO") + "/credenciais";
        String bia = "/empresas/" + empresa + "/usuarios/" + criarUsuario(empresa, "Bia", "CLIENTE") + "/credenciais";
        var cracha = enviar(post(ana), Map.of("tipo", "CODIGO_BARRA", "codigo", 7891234567895L), 201);
        assertThat(cracha.get("codigo").asLong()).isEqualTo(7891234567895L);
        assertThat(cracha.get("nomeUsuario").isNull()).isTrue();
        enviar(post(bia), Map.of("tipo", "CODIGO_BARRA", "codigo", 7891234567895L), 409);
        enviar(post(ana), Map.of("tipo", "USUARIO_SENHA", "nomeUsuario", "ana", "senha", "segredo-123"), 201);
        enviar(post(bia), Map.of("tipo", "USUARIO_SENHA", "nomeUsuario", "ana", "senha", "segredo-123"), 409);

        var invalida = enviar(post(bia), Map.of("tipo", "CODIGO_BARRA", "codigo", 1, "nomeUsuario", "bia"), 400);
        assertThat(invalida.get("detalhes").toString()).contains("camposDoTipo");
        enviar(post(bia), Map.of("tipo", "USUARIO_SENHA", "nomeUsuario", "bia", "senha", "curta"), 400);
        enviar(post(bia), Map.of("tipo", "USUARIO_SENHA", "nomeUsuario", "bia com espaço", "senha", "segredo-123"), 400);
        enviar(post(bia), Map.of("tipo", "USUARIO_SENHA", "nomeUsuario", "bia"), 422);
        assertThat(obter(ana).at("/_embedded/credenciais").size()).isEqualTo(2);
    }
}
