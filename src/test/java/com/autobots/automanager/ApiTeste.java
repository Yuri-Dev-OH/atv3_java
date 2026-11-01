package com.autobots.automanager;

import java.util.List;
import java.util.Map;
import com.autobots.automanager.repositorios.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Base dos testes de API: banco limpo a cada teste, envio de JSON e leitura de links HAL. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class ApiTeste {
    static final String HOST = "http://localhost";
    static final Map<String, Object> ENDERECO = Map.of("cidade", "São José dos Campos", "rua", "Rua Um", "numero", "10");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired VendaRepositorio vendas;
    @Autowired MercadoriaRepositorio mercadorias;
    @Autowired ServicoRepositorio servicos;
    @Autowired UsuarioRepositorio usuarios;
    @Autowired EmpresaRepositorio empresas;

    @BeforeEach void limparBanco() {
        vendas.deleteAll();
        mercadorias.deleteAll();
        servicos.deleteAll();
        usuarios.deleteAll();
        empresas.deleteAll();
    }

    JsonNode enviar(MockHttpServletRequestBuilder request, Object body, int esperado) throws Exception {
        var resposta = mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(body)))
            .andExpect(status().is(esperado)).andReturn().getResponse();
        return json.readTree(resposta.getContentAsByteArray());
    }
    JsonNode obter(String uri) throws Exception {
        return json.readTree(mvc.perform(get(uri)).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
    }
    JsonNode formulario(String uri) throws Exception {
        var resposta = mvc.perform(get(uri).accept(MediaTypes.HAL_FORMS_JSON)).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_FORMS_JSON)).andReturn().getResponse();
        return json.readTree(resposta.getContentAsByteArray());
    }
    void excluir(String uri, int esperado) throws Exception { mvc.perform(delete(uri)).andExpect(status().is(esperado)); }

    /** Lê o href de uma relação, descartando variáveis de template como {@code {?pagina,tamanho}}. */
    static String link(JsonNode recurso, String relacao) {
        var href = recurso.at("/_links/" + relacao + "/href");
        assertThat(href.isTextual()).as("link '%s' em %s", relacao, recurso).isTrue();
        return href.asText().replaceAll("\\{.*}", "");
    }

    // Fábricas de dados usadas pelos testes.
    static Map<String, Object> empresa(String razaoSocial) { return Map.of("razaoSocial", razaoSocial); }
    static Map<String, Object> usuario(String nome, String... perfis) { return Map.of("nome", nome, "perfis", List.of(perfis)); }
    static Map<String, Object> documento(String tipo, String numero) {
        return Map.of("tipo", tipo, "numero", numero, "dataEmissao", "2020-01-01");
    }
    static Map<String, Object> veiculo(String placa) { return Map.of("tipo", "SUV", "modelo", "Corolla Cross", "placa", placa); }
    static Map<String, Object> mercadoria(String nome, long quantidade, String valor) {
        return Map.of("nome", nome, "quantidade", quantidade, "valor", new java.math.BigDecimal(valor));
    }
    static Map<String, Object> servico(String nome, String valor) { return Map.of("nome", nome, "valor", new java.math.BigDecimal(valor)); }
    static Map<String, Object> item(long id, int quantidade) { return Map.of("id", id, "quantidade", quantidade); }

    long criarEmpresa() throws Exception { return enviar(post("/empresas"), empresa("Oficina Teste"), 201).get("id").asLong(); }
    long criarUsuario(long empresaId, String nome, String... perfis) throws Exception {
        return enviar(post("/empresas/" + empresaId + "/usuarios"), usuario(nome, perfis), 201).get("id").asLong();
    }
}
