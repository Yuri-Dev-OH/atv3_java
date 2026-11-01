package com.autobots.automanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CRUDs de veículos, mercadorias, serviços e vendas, com as regras de negócio da venda.
 *
 * <p>Cobre a funcionalidade da ATVIII "cadastro de veículos, mercadorias e serviços, para gerenciamento de vendas e
 * serviços prestados a um usuário e seu veículo".
 */
class VendaIntegrationTest extends ApiTeste {
    long empresa;
    long cliente;
    long funcionario;
    long fornecedor;
    String base;

    @BeforeEach void prepararEmpresa() throws Exception {
        empresa = criarEmpresa();
        base = "/empresas/" + empresa;
        cliente = criarUsuario(empresa, "Cliente", "CLIENTE");
        funcionario = criarUsuario(empresa, "Funcionário", "FUNCIONARIO");
        fornecedor = criarUsuario(empresa, "Fornecedor", "FORNECEDOR");
    }
    long criarVeiculo(long dono, String placa) throws Exception {
        return enviar(post(base + "/usuarios/" + dono + "/veiculos"), veiculo(placa), 201).get("id").asLong();
    }
    long criarMercadoria(String nome, long quantidade, String valor) throws Exception {
        return enviar(post(base + "/mercadorias"), mercadoria(nome, quantidade, valor), 201).get("id").asLong();
    }
    long criarServico(String nome, String valor) throws Exception {
        return enviar(post(base + "/servicos"), servico(nome, valor), 201).get("id").asLong();
    }
    Map<String, Object> venda(String identificacao, Long veiculo, List<?> itensMercadoria, List<?> itensServico) {
        var dados = new HashMap<String, Object>(Map.of("identificacao", identificacao, "clienteId", cliente,
            "funcionarioId", funcionario, "mercadorias", itensMercadoria, "servicos", itensServico));
        if (veiculo != null) dados.put("veiculoId", veiculo);
        return dados;
    }
    long estoque(long mercadoria) throws Exception { return obter(base + "/mercadorias/" + mercadoria).get("quantidade").asLong(); }

    @Test void veiculoPertenceAUmClienteETemPlacaUnicaNaEmpresa() throws Exception {
        String veiculos = base + "/usuarios/" + cliente + "/veiculos";
        var criado = enviar(post(veiculos), Map.of("tipo", "SUV", "modelo", "Corolla Cross", "placa", "abc-1d23"), 201);
        long id = criado.get("id").asLong();
        assertThat(criado.get("placa").asText()).isEqualTo("ABC1D23");
        enviar(post(base + "/usuarios/" + criarUsuario(empresa, "Outro", "CLIENTE") + "/veiculos"), veiculo("ABC1D23"), 409);
        long outra = criarEmpresa();
        enviar(post("/empresas/" + outra + "/usuarios/" + criarUsuario(outra, "Outro", "CLIENTE") + "/veiculos"), veiculo("ABC1D23"), 201);

        var funcionarioSemVeiculo = enviar(post(base + "/usuarios/" + funcionario + "/veiculos"), veiculo("XYZ9999"), 422);
        assertThat(funcionarioSemVeiculo.get("detail").asText()).contains("CLIENTE");
        enviar(post(veiculos), veiculo("12345"), 400);
        enviar(post(veiculos), Map.of("tipo", "MOTO", "modelo", "CG", "placa", "ABC1234"), 400);

        assertThat(enviar(put(veiculos + "/" + id), Map.of("tipo", "HATCH", "modelo", "Etios", "placa", "ABC1D23"), 200)
            .get("tipo").asText()).isEqualTo("HATCH");
        assertThat(obter(veiculos).at("/_embedded/veiculos").size()).isEqualTo(1);
        mvc.perform(get(base + "/usuarios/" + funcionario + "/veiculos/" + id)).andExpect(status().isNotFound());
        excluir(veiculos + "/" + id, 204);
        assertThat(obter(veiculos).has("_embedded")).isFalse();
    }

    @Test void mercadoriaTemCrudEFornecedorComPerfilCorreto() throws Exception {
        var dados = new HashMap<String, Object>(mercadoria("Roda liga leve", 30, "300.00"));
        dados.putAll(Map.of("fabricacao", "2024-01-10", "validade", "2030-01-10", "fornecedorId", fornecedor, "descricao", "Aro 15"));
        var criada = enviar(post(base + "/mercadorias"), dados, 201);
        long id = criada.get("id").asLong();
        assertThat(criada.get("valor").decimalValue()).isEqualByComparingTo("300.00");
        assertThat(criada.get("fornecedorId").asLong()).isEqualTo(fornecedor);
        assertThat(link(criada, "fornecedor")).isEqualTo(HOST + base + "/usuarios/" + fornecedor);

        dados.put("fornecedorId", cliente);
        enviar(post(base + "/mercadorias"), dados, 422);
        dados.put("fornecedorId", 999999);
        enviar(post(base + "/mercadorias"), dados, 422);
        dados.remove("fornecedorId");
        dados.put("validade", "2023-01-01");
        assertThat(enviar(post(base + "/mercadorias"), dados, 400).get("detalhes").toString()).contains("validadeAposFabricacao");
        enviar(post(base + "/mercadorias"), mercadoria("Negativa", -1, "1.00"), 400);
        enviar(post(base + "/mercadorias"), mercadoria("Centavos", 1, "1.001"), 400);

        var alterada = enviar(put(base + "/mercadorias/" + id), mercadoria("Roda aro 16", 40, "350.50"), 200);
        assertThat(alterada.get("quantidade").asLong()).isEqualTo(40);
        assertThat(alterada.get("fornecedorId").isNull()).isTrue();
        assertThat(alterada.get("cadastro").asText()).isEqualTo(criada.get("cadastro").asText());

        long outra = criarMercadoria("Pneu", 4, "500.00");
        dados = new HashMap<>(mercadoria("Pneu", 4, "500.00"));
        dados.put("fornecedorId", fornecedor);
        enviar(put(base + "/mercadorias/" + outra), dados, 200);
        var doFornecedor = obter(base + "/mercadorias?fornecedorId=" + fornecedor);
        assertThat(doFornecedor.at("/_embedded/mercadorias").findValuesAsText("nome")).containsExactly("Pneu");
        assertThat(link(obter(base + "/usuarios/" + fornecedor), "mercadorias")).isEqualTo(HOST + base + "/mercadorias?fornecedorId=" + fornecedor);
        assertThat(obter(base + "/mercadorias").at("/page/totalElements").asLong()).isEqualTo(2);

        var semPerfil = enviar(put(base + "/usuarios/" + fornecedor), usuario("Fornecedor", "CLIENTE"), 409);
        assertThat(semPerfil.get("detail").asText()).contains("FORNECEDOR");
        excluir(base + "/mercadorias/" + id, 204);
        mvc.perform(get(base + "/mercadorias/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/empresas/" + criarEmpresa() + "/mercadorias/" + outra)).andExpect(status().isNotFound());
    }

    @Test void servicoTemCrudCompleto() throws Exception {
        var criado = enviar(post(base + "/servicos"), Map.of("nome", "Alinhamento", "descricao", "Rodas", "valor", 50), 201);
        long id = criado.get("id").asLong();
        assertThat(criado.get("valor").decimalValue()).isEqualByComparingTo("50");
        assertThat(enviar(put(base + "/servicos/" + id), servico("Alinhamento 3D", "80.00"), 200).get("nome").asText())
            .isEqualTo("Alinhamento 3D");
        assertThat(obter(base + "/servicos/" + id).get("descricao").isNull()).isTrue();
        assertThat(obter(base + "/servicos").at("/_embedded/servicos").size()).isEqualTo(1);
        enviar(post(base + "/servicos"), Map.of("nome", "Sem valor"), 400);
        enviar(post(base + "/servicos"), servico("Negativo", "-1.00"), 400);
        excluir(base + "/servicos/" + id, 204);
        excluir(base + "/servicos/" + id, 404);
    }

    @Test void vendaBaixaEstoqueCalculaTotalEGuardaPrecoDaEpoca() throws Exception {
        long veiculo = criarVeiculo(cliente, "ABC0000");
        long roda = criarMercadoria("Roda", 30, "300.00");
        long troca = criarServico("Troca de rodas", "50.00");
        long alinhamento = criarServico("Alinhamento", "45.50");

        var venda = enviar(post(base + "/vendas"),
            venda("V-1", veiculo, List.of(item(roda, 4)), List.of(item(troca, 1), item(alinhamento, 2))), 201);
        long id = venda.get("id").asLong();
        assertThat(venda.get("valorTotal").decimalValue()).isEqualByComparingTo("1341.00");
        assertThat(venda.at("/cliente/nome").asText()).isEqualTo("Cliente");
        assertThat(venda.at("/funcionario/id").asLong()).isEqualTo(funcionario);
        assertThat(venda.at("/veiculo/placa").asText()).isEqualTo("ABC0000");
        assertThat(venda.at("/mercadorias/0/subtotal").decimalValue()).isEqualByComparingTo("1200.00");
        assertThat(venda.at("/servicos/1/nome").asText()).isEqualTo("Alinhamento");
        assertThat(estoque(roda)).isEqualTo(26);
        assertThat(link(venda, "cliente")).isEqualTo(HOST + base + "/usuarios/" + cliente);
        assertThat(link(venda, "funcionario")).isEqualTo(HOST + base + "/usuarios/" + funcionario);
        assertThat(link(venda, "veiculo")).isEqualTo(HOST + base + "/usuarios/" + cliente + "/veiculos/" + veiculo);

        enviar(put(base + "/mercadorias/" + roda), mercadoria("Roda", 26, "999.00"), 200);
        assertThat(obter(base + "/vendas/" + id).at("/mercadorias/0/valorUnitario").decimalValue()).isEqualByComparingTo("300.00");

        var alterada = enviar(put(base + "/vendas/" + id), venda("V-1", null, List.of(item(roda, 1)), List.of()), 200);
        assertThat(estoque(roda)).isEqualTo(29);
        assertThat(alterada.get("veiculo").isNull()).isTrue();
        assertThat(alterada.get("valorTotal").decimalValue()).isEqualByComparingTo("999.00");
        excluir(base + "/vendas/" + id, 204);
        assertThat(estoque(roda)).isEqualTo(30);
        mvc.perform(get(base + "/vendas/" + id)).andExpect(status().isNotFound());
    }

    @Test void vendaValidaParticipantesItensEEstoque() throws Exception {
        long roda = criarMercadoria("Roda", 2, "300.00");
        long servico = criarServico("Troca", "50.00");
        long outroCliente = criarUsuario(empresa, "Outro cliente", "CLIENTE");
        long veiculoAlheio = criarVeiculo(outroCliente, "XYZ1234");

        var estoque = enviar(post(base + "/vendas"), venda("V-1", null, List.of(item(roda, 3)), List.of()), 409);
        assertThat(estoque.get("detail").asText()).contains("Estoque insuficiente", "disponível 2");
        assertThat(estoque(roda)).isEqualTo(2);

        var trocados = venda("V-1", null, List.of(), List.of(item(servico, 1)));
        trocados.put("clienteId", funcionario);
        assertThat(enviar(post(base + "/vendas"), trocados, 422).get("detail").asText()).contains("perfil CLIENTE");
        trocados.put("clienteId", cliente);
        trocados.put("funcionarioId", cliente);
        assertThat(enviar(post(base + "/vendas"), trocados, 422).get("detail").asText()).contains("perfil FUNCIONARIO");

        enviar(post(base + "/vendas"), venda("V-1", veiculoAlheio, List.of(), List.of(item(servico, 1))), 422);
        enviar(post(base + "/vendas"), venda("V-1", null, List.of(item(999999, 1)), List.of()), 422);
        enviar(post(base + "/vendas"), venda("V-1", null, List.of(), List.of(item(servico, 1), item(servico, 2))), 422);
        assertThat(enviar(post(base + "/vendas"), venda("V-1", null, List.of(), List.of()), 400).get("detalhes").toString())
            .contains("possuiItens");
        enviar(post(base + "/vendas"), venda("V-1", null, List.of(item(roda, 0)), List.of()), 400);

        long outraEmpresa = criarEmpresa();
        long servicoAlheio = enviar(post("/empresas/" + outraEmpresa + "/servicos"), servico("Alheio", "1.00"), 201).get("id").asLong();
        enviar(post(base + "/vendas"), venda("V-1", null, List.of(), List.of(item(servicoAlheio, 1))), 422);

        enviar(post(base + "/vendas"), venda("V-1", null, List.of(item(roda, 2)), List.of()), 201);
        enviar(post(base + "/vendas"), venda("V-1", null, List.of(), List.of(item(servico, 1))), 409);
        assertThat(estoque(roda)).isZero();
    }

    @Test void registrosVendidosNaoPodemSerApagadosNemPerderPerfil() throws Exception {
        long veiculo = criarVeiculo(cliente, "ABC0000");
        long roda = criarMercadoria("Roda", 5, "300.00");
        long servico = criarServico("Troca", "50.00");
        enviar(post(base + "/vendas"), venda("V-1", veiculo, List.of(item(roda, 1)), List.of(item(servico, 1))), 201);

        excluir(base + "/mercadorias/" + roda, 409);
        excluir(base + "/servicos/" + servico, 409);
        excluir(base + "/usuarios/" + cliente + "/veiculos/" + veiculo, 409);
        excluir(base + "/usuarios/" + cliente, 409);
        excluir(base + "/usuarios/" + funcionario, 409);
        assertThat(enviar(put(base + "/usuarios/" + cliente), usuario("Cliente", "FORNECEDOR"), 409).get("detail").asText())
            .contains("CLIENTE");
        assertThat(enviar(put(base + "/usuarios/" + funcionario), usuario("Funcionário", "CLIENTE"), 409).get("detail").asText())
            .contains("FUNCIONARIO");
        enviar(put(base + "/usuarios/" + cliente), usuario("Cliente", "CLIENTE", "FORNECEDOR"), 200);
    }

    @Test void vendasSaoFiltradasPorClienteFuncionarioEVeiculo() throws Exception {
        long veiculo = criarVeiculo(cliente, "ABC0000");
        long outroCliente = criarUsuario(empresa, "Outro", "CLIENTE");
        long servico = criarServico("Troca", "50.00");
        enviar(post(base + "/vendas"), venda("V-1", veiculo, List.of(), List.of(item(servico, 1))), 201);
        enviar(post(base + "/vendas"), venda("V-2", null, List.of(), List.of(item(servico, 1))), 201);
        var outra = venda("V-3", null, List.of(), List.of(item(servico, 1)));
        outra.put("clienteId", outroCliente);
        enviar(post(base + "/vendas"), outra, 201);

        var clienteJson = obter(base + "/usuarios/" + cliente);
        var compras = obter(link(clienteJson, "compras"));
        assertThat(compras.at("/_embedded/vendas").findValuesAsText("identificacao")).containsExactly("V-1", "V-2");
        assertThat(link(compras, "self")).isEqualTo(HOST + base + "/vendas?clienteId=" + cliente + "&pagina=0&tamanho=20");
        var doVeiculo = obter(link(obter(base + "/usuarios/" + cliente + "/veiculos/" + veiculo), "vendas"));
        assertThat(doVeiculo.at("/_embedded/vendas").findValuesAsText("identificacao")).containsExactly("V-1");
        var atendidas = obter(link(obter(base + "/usuarios/" + funcionario), "vendas"));
        assertThat(atendidas.at("/page/totalElements").asLong()).isEqualTo(3);
        assertThat(obter(base + "/vendas?pagina=0&tamanho=2").at("/page/totalPages").asInt()).isEqualTo(2);
    }

    @Test void vendasSimultaneasNaoVendemOMesmoEstoqueDuasVezes() throws Exception {
        long roda = criarMercadoria("Roda", 5, "300.00");
        try (var executor = Executors.newFixedThreadPool(4)) {
            var tarefas = new java.util.ArrayList<Callable<Integer>>();
            for (int i = 0; i < 4; i++) {
                var dados = json.writeValueAsBytes(venda("C-" + i, null, List.of(item(roda, 2)), List.of()));
                tarefas.add(() -> mvc.perform(post(base + "/vendas").contentType("application/json").content(dados))
                    .andReturn().getResponse().getStatus());
            }
            var status = new java.util.ArrayList<Integer>();
            for (var futuro : executor.invokeAll(tarefas)) status.add(futuro.get());
            assertThat(status).containsOnly(201, 409);
            assertThat(status.stream().filter(s -> s == 201).count()).isEqualTo(2);
        }
        assertThat(estoque(roda)).isEqualTo(1);
    }
}
