package com.autobots.automanager;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifica que os novos recursos da ATVIII mantêm os níveis do Richardson Maturity Model da ATVII.
 *
 * <p>Os níveis 0 e 1 (HTTP e URI por recurso) são exercitados pelos CRUDs de {@link CadastroIntegrationTest} e
 * {@link VendaIntegrationTest}. Aqui ficam a semântica HTTP do nível 2 e a navegação hipermídia (HATEOAS) do nível 3.
 */
class MaturidadeRestIntegrationTest extends ApiTeste {

    @Test void raizEhOPontoDeEntradaDaApi() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
            .andExpect(jsonPath("$._links.self.href").value(HOST + "/"))
            .andExpect(jsonPath("$._links.empresas.href").value(HOST + "/empresas{?pagina,tamanho}"))
            .andExpect(jsonPath("$._links.empresas.templated").value(true));
    }

    /** Opera a loja inteira conhecendo apenas a raiz: todas as demais URIs vêm de {@code _links}. */
    @Test void operacaoCompletaDaLojaApenasSeguindoLinks() throws Exception {
        var empresa = enviar(post(link(obter("/"), "empresas")), empresa("Car service toyota ltda"), 201);
        String empresaUri = link(empresa, "self");
        for (var relacao : List.of("empresas", "endereco", "telefones", "usuarios", "mercadorias", "servicos", "vendas")) {
            link(empresa, relacao);
        }
        assertThat(link(enviar(post(link(empresa, "endereco")), ENDERECO, 201), "empresa")).isEqualTo(empresaUri);
        assertThat(link(enviar(post(link(empresa, "telefones")), Map.of("ddd", "11", "numero", "986454527"), 201), "empresa"))
            .isEqualTo(empresaUri);

        var cliente = enviar(post(link(empresa, "usuarios")), usuario("Dom Pedro", "CLIENTE"), 201);
        var funcionario = enviar(post(link(empresa, "usuarios")), usuario("Funcionário", "FUNCIONARIO"), 201);
        var fornecedor = enviar(post(link(empresa, "usuarios")), usuario("Fornecedor", "FORNECEDOR"), 201);
        assertThat(link(cliente, "empresa")).isEqualTo(empresaUri);
        assertThat(link(cliente, "usuarios")).isEqualTo(empresaUri + "/usuarios");

        var documento = enviar(post(link(cliente, "documentos")), documento("CPF", "12584698533"), 201);
        assertThat(link(documento, "usuario")).isEqualTo(link(cliente, "self"));
        var email = enviar(post(link(cliente, "emails")), Map.of("endereco", "c@c.com"), 201);
        assertThat(link(obter(link(email, "emails")).at("/_embedded/emails/0"), "self")).isEqualTo(link(email, "self"));
        var credencial = enviar(post(link(cliente, "credenciais")),
            Map.of("tipo", "USUARIO_SENHA", "nomeUsuario", "dompedrocliente", "senha", "senha-segura-1"), 201);
        assertThat(link(credencial, "usuario")).isEqualTo(link(cliente, "self"));
        var telefone = enviar(post(link(cliente, "telefones")), Map.of("ddd", "12", "numero", "999998888"), 201);
        assertThat(link(telefone, "usuario")).isEqualTo(link(cliente, "self"));
        var endereco = enviar(post(link(cliente, "endereco")), ENDERECO, 201);
        assertThat(link(endereco, "self")).isEqualTo(link(cliente, "endereco"));

        var veiculo = enviar(post(link(cliente, "veiculos")), veiculo("ABC0000"), 201);
        assertThat(link(veiculo, "proprietario")).isEqualTo(link(cliente, "self"));
        var mercadoria = new java.util.HashMap<String, Object>(mercadoria("Roda", 30, "300.00"));
        mercadoria.put("fornecedorId", fornecedor.get("id").asLong());
        var roda = enviar(post(link(empresa, "mercadorias")), mercadoria, 201);
        assertThat(link(roda, "fornecedor")).isEqualTo(link(fornecedor, "self"));
        var troca = enviar(post(link(empresa, "servicos")), servico("Troca de rodas", "50.00"), 201);

        var venda = enviar(post(link(empresa, "vendas")), Map.of("identificacao", "1234698745",
            "clienteId", cliente.get("id").asLong(), "funcionarioId", funcionario.get("id").asLong(),
            "veiculoId", veiculo.get("id").asLong(), "mercadorias", List.of(item(roda.get("id").asLong(), 4)),
            "servicos", List.of(item(troca.get("id").asLong(), 1))), 201);
        assertThat(link(venda, "cliente")).isEqualTo(link(cliente, "self"));
        assertThat(link(venda, "funcionario")).isEqualTo(link(funcionario, "self"));
        assertThat(link(venda, "veiculo")).isEqualTo(link(veiculo, "self"));
        assertThat(obter(link(roda, "self")).get("quantidade").asLong()).isEqualTo(26);
        assertThat(obter(link(cliente, "compras")).at("/_embedded/vendas/0/id").asLong()).isEqualTo(venda.get("id").asLong());
        assertThat(obter(link(veiculo, "vendas")).at("/page/totalElements").asLong()).isEqualTo(1);
        assertThat(obter(link(fornecedor, "mercadorias")).at("/_embedded/mercadorias/0/nome").asText()).isEqualTo("Roda");

        mvc.perform(delete(link(venda, "self"))).andExpect(status().isNoContent());
        for (var recurso : List.of(veiculo, credencial, email, documento, telefone, endereco, roda, troca, cliente, funcionario, fornecedor)) {
            mvc.perform(delete(link(recurso, "self"))).andExpect(status().isNoContent());
        }
        mvc.perform(delete(empresaUri)).andExpect(status().isNoContent());
        mvc.perform(get(empresaUri)).andExpect(status().isNotFound());
    }

    @Test void linksDoUsuarioDependemDosPerfis() throws Exception {
        long empresa = criarEmpresa();
        String usuarios = "/empresas/" + empresa + "/usuarios/";
        var cliente = obter(usuarios + criarUsuario(empresa, "Cliente", "CLIENTE"));
        assertThat(cliente.at("/_links").has("veiculos")).isTrue();
        assertThat(cliente.at("/_links").has("compras")).isTrue();
        assertThat(cliente.at("/_links").has("vendas")).isFalse();
        assertThat(cliente.at("/_links").has("mercadorias")).isFalse();
        var funcionario = obter(usuarios + criarUsuario(empresa, "Funcionário", "FUNCIONARIO"));
        assertThat(funcionario.at("/_links").has("vendas")).isTrue();
        assertThat(funcionario.at("/_links").has("veiculos")).isFalse();
        var fornecedor = obter(usuarios + criarUsuario(empresa, "Fornecedor", "FORNECEDOR"));
        assertThat(fornecedor.at("/_links").has("mercadorias")).isTrue();
        assertThat(fornecedor.at("/_links").has("compras")).isFalse();
        for (var usuario : List.of(cliente, funcionario, fornecedor)) {
            for (var relacao : List.of("self", "usuarios", "empresa", "endereco", "telefones", "documentos", "emails", "credenciais")) {
                link(usuario, relacao);
            }
        }
    }

    @Test void locationDaCriacaoApontaParaOProprioRecurso() throws Exception {
        long empresa = criarEmpresa();
        long cliente = criarUsuario(empresa, "Cliente", "CLIENTE");
        String base = "/empresas/" + empresa;
        String usuario = base + "/usuarios/" + cliente;
        var criacoes = Map.<String, Object>of(
            "/empresas", empresa("Outra"),
            base + "/endereco", ENDERECO,
            base + "/telefones", Map.of("ddd", "12", "numero", "99999888"),
            base + "/usuarios", usuario("Ana", "CLIENTE"),
            usuario + "/documentos", documento("RG", "1"),
            usuario + "/emails", Map.of("endereco", "a@a.com"),
            usuario + "/credenciais", Map.of("tipo", "CODIGO_BARRA", "codigo", 123),
            usuario + "/veiculos", veiculo("ABC1234"),
            base + "/mercadorias", mercadoria("Roda", 1, "1.00"),
            base + "/servicos", servico("Troca", "1.00"));
        for (var criacao : criacoes.entrySet()) {
            var resposta = mvc.perform(post(criacao.getKey()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(criacao.getValue()))).andExpect(status().isCreated()).andReturn().getResponse();
            var corpo = json.readTree(resposta.getContentAsByteArray());
            assertThat(resposta.getHeader("Location")).startsWith(HOST + criacao.getKey()).isEqualTo(link(corpo, "self"));
        }
    }

    @Test void paginacaoOfereceLinksDeNavegacao() throws Exception {
        for (int i = 0; i < 3; i++) criarEmpresa();
        String pagina = HOST + "/empresas?pagina=%d&tamanho=1";
        var meio = obter("/empresas?pagina=1&tamanho=1");
        assertThat(link(meio, "self")).isEqualTo(pagina.formatted(1));
        assertThat(link(meio, "first")).isEqualTo(pagina.formatted(0));
        assertThat(link(meio, "prev")).isEqualTo(pagina.formatted(0));
        assertThat(link(meio, "next")).isEqualTo(pagina.formatted(2));
        assertThat(link(meio, "last")).isEqualTo(pagina.formatted(2));
        assertThat(meio.at("/page/totalElements").asLong()).isEqualTo(3);
        assertThat(link(meio.at("/_embedded/empresas/0"), "self")).startsWith(HOST + "/empresas/");

        var primeira = obter("/empresas?pagina=0&tamanho=1");
        assertThat(primeira.at("/_links/prev").isMissingNode()).isTrue();
        var ultima = obter(link(primeira, "last"));
        assertThat(ultima.at("/_links/next").isMissingNode()).isTrue();
        var foraDoIntervalo = obter("/empresas?pagina=9&tamanho=1");
        assertThat(foraDoIntervalo.has("_embedded")).isFalse();
        assertThat(link(foraDoIntervalo, "prev")).isEqualTo(pagina.formatted(2));

        long empresa = criarEmpresa();
        for (int i = 0; i < 3; i++) criarUsuario(empresa, "Cliente " + i, "CLIENTE");
        var filtrada = obter("/empresas/" + empresa + "/usuarios?perfil=CLIENTE&pagina=0&tamanho=2");
        assertThat(link(filtrada, "next")).isEqualTo(HOST + "/empresas/" + empresa + "/usuarios?perfil=CLIENTE&pagina=1&tamanho=2");
        assertThat(link(filtrada, "empresa")).isEqualTo(HOST + "/empresas/" + empresa);
        mvc.perform(get("/empresas?tamanho=101")).andExpect(status().isBadRequest());
        mvc.perform(get("/empresas?pagina=-1")).andExpect(status().isBadRequest());
    }

    @Test void colecaoVaziaMantemLinksSemEmbedded() throws Exception {
        long empresa = criarEmpresa();
        long usuario = criarUsuario(empresa, "Ana", "CLIENTE");
        String uri = "/empresas/" + empresa + "/usuarios/" + usuario;
        for (var recurso : List.of("telefones", "documentos", "emails", "credenciais", "veiculos")) {
            var vazia = obter(uri + "/" + recurso);
            assertThat(vazia.has("_embedded")).isFalse();
            assertThat(link(vazia, "self")).isEqualTo(HOST + uri + "/" + recurso);
        }
        var vendas = obter("/empresas/" + empresa + "/vendas");
        assertThat(vendas.at("/page/totalElements").asLong()).isZero();
        assertThat(link(vendas, "empresa")).isEqualTo(HOST + "/empresas/" + empresa);
    }

    @Test void halFormsAnunciaAcoesConformeOEstado() throws Exception {
        long empresa = criarEmpresa();
        String empresaUri = "/empresas/" + empresa;
        String usuarioUri = empresaUri + "/usuarios/" + criarUsuario(empresa, "Ana", "CLIENTE");
        for (var uri : List.of(empresaUri, usuarioUri)) {
            var semEndereco = formulario(uri);
            assertThat(semEndereco.at("/_templates/default/method").asText()).isEqualTo("PUT");
            assertThat(semEndereco.at("/_templates/excluir/method").asText()).isEqualTo("DELETE");
            assertThat(semEndereco.at("/_templates/cadastrar/method").asText()).isEqualTo("POST");
            assertThat(semEndereco.at("/_templates/cadastrar/target").asText()).isEqualTo(HOST + uri + "/endereco");
            enviar(post(uri + "/endereco"), ENDERECO, 201);
            assertThat(formulario(uri).at("/_templates/cadastrar").isMissingNode()).isTrue();
            var endereco = formulario(uri + "/endereco");
            assertThat(endereco.at("/_templates/default/method").asText()).isEqualTo("PUT");
            assertThat(endereco.at("/_templates/excluir/method").asText()).isEqualTo("DELETE");
        }
        for (var colecao : List.of("/empresas", empresaUri + "/usuarios", empresaUri + "/mercadorias", empresaUri + "/servicos",
                empresaUri + "/vendas", usuarioUri + "/veiculos", usuarioUri + "/credenciais")) {
            assertThat(formulario(colecao).at("/_templates/default/method").asText()).as(colecao).isEqualTo("POST");
        }
    }

    @Test void halFormsDescreveCamposDasEntradas() throws Exception {
        long empresa = criarEmpresa();
        String usuario = "/empresas/" + empresa + "/usuarios/" + criarUsuario(empresa, "Ana", "CLIENTE");
        var documentos = formulario(usuario + "/documentos").at("/_templates/default/properties");
        assertThat(documentos.findValuesAsText("name")).containsExactlyInAnyOrder("tipo", "numero", "dataEmissao");
        for (var campo : documentos) {
            assertThat(campo.get("required").asBoolean()).isTrue();
            assertThat(campo.has("readOnly")).isFalse();
        }
        var credenciais = formulario(usuario + "/credenciais").at("/_templates/default/properties");
        assertThat(credenciais.findValuesAsText("name")).containsExactlyInAnyOrder("tipo", "nomeUsuario", "senha", "codigo", "inativo");
        var vendas = formulario("/empresas/" + empresa + "/vendas").at("/_templates/default/properties");
        assertThat(vendas.findValuesAsText("name"))
            .contains("identificacao", "clienteId", "funcionarioId", "veiculoId").doesNotContain("possuiItens");
        var veiculos = formulario(usuario + "/veiculos").at("/_templates/default/properties");
        assertThat(veiculos.findValuesAsText("regex")).contains("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}");
        var raiz = formulario("/");
        assertThat(raiz.at("/_templates/default/target").asText()).isEqualTo(HOST + "/empresas");
        JsonNode razao = raiz.at("/_templates/default/properties").findParents("name").stream()
            .filter(it -> it.get("name").asText().equals("razaoSocial")).findFirst().orElseThrow();
        assertThat(razao.get("maxLength").asInt()).isEqualTo(150);
    }

    @Test void errosUsamProblemDetail() throws Exception {
        mvc.perform(get("/empresas/99999999")).andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.title").value("Recurso não encontrado."))
            .andExpect(jsonPath("$.detail").value("Empresa não encontrada.")).andExpect(jsonPath("$.instance").value("/empresas/99999999"));
        mvc.perform(get("/empresas/99999999").accept(MediaTypes.HAL_FORMS_JSON)).andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(post("/empresas").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detalhes.length()").value(1));
        long empresa = criarEmpresa();
        long funcionario = criarUsuario(empresa, "Funcionário", "FUNCIONARIO");
        mvc.perform(post("/empresas/" + empresa + "/usuarios/" + funcionario + "/veiculos").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(veiculo("ABC1234")))).andExpect(status().isUnprocessableEntity())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Regra de negócio violada."));
        enviar(post("/empresas/" + empresa + "/endereco"), ENDERECO, 201);
        assertThat(enviar(post("/empresas/" + empresa + "/endereco"), ENDERECO, 409).get("title").asText()).isEqualTo("Conflito.");
    }

    @Test void metodosEMidiasSeguemASemanticaHttp() throws Exception {
        String uri = "/empresas/" + criarEmpresa();
        mvc.perform(patch(uri).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isMethodNotAllowed())
            .andExpect(header().string("Allow", org.hamcrest.Matchers.containsString("PUT")))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(post("/empresas").contentType(MediaType.TEXT_PLAIN).content("x")).andExpect(status().isUnsupportedMediaType());
        mvc.perform(get(uri).accept(MediaType.APPLICATION_XML)).andExpect(status().isNotAcceptable());
        // O MockMvc não descarta o corpo do HEAD; isso é feito pelo contêiner servlet (ver scripts/demo.ps1).
        mvc.perform(head(uri)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON));
        mvc.perform(options(uri)).andExpect(status().isOk())
            .andExpect(header().string("Allow", org.hamcrest.Matchers.allOf(org.hamcrest.Matchers.containsString("GET"),
                org.hamcrest.Matchers.containsString("PUT"), org.hamcrest.Matchers.containsString("DELETE"))));
        mvc.perform(get("/clientes")).andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
            .andExpect(jsonPath("$._links.self.href").value(HOST + uri));
        mvc.perform(get("/empresas/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/empresas/0")).andExpect(status().isBadRequest());
    }
}
