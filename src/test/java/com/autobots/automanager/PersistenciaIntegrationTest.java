package com.autobots.automanager;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import com.autobots.automanager.dto.Dados.*;
import com.autobots.automanager.enumeracoes.*;
import com.autobots.automanager.repositorios.*;
import com.autobots.automanager.servicos.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

class PersistenciaIntegrationTest {
    @TempDir Path pasta;

    private ConfigurableApplicationContext iniciar() {
        return new SpringApplicationBuilder(AutomanagerApplication.class).web(WebApplicationType.NONE)
            .run("--spring.datasource.url=jdbc:h2:file:" + pasta.resolve("automanager").toAbsolutePath().toString().replace('\\', '/'),
                "--spring.datasource.username=sa", "--spring.datasource.password=", "--spring.main.banner-mode=off",
                "--automanager.senha.iteracoes=1000");
    }

    @Test void conservaCadastrosEVendasAposReabrirBancoEmArquivo() {
        long empresa;
        long cliente;
        long venda;
        try (var contexto = iniciar()) {
            empresa = contexto.getBean(EmpresaServico.class).cadastrar(new EmpresaCadastro("Oficina", null, null, List.of())).id();
            var usuarios = contexto.getBean(UsuarioServico.class);
            cliente = usuarios.cadastrar(empresa, new UsuarioCadastro("Ana", null, null, Set.of(PerfilUsuario.CLIENTE),
                new EnderecoDados("SP", "São Paulo", null, "Rua A", "1", "01001000", null), List.of(new TelefoneDados("11", "999998888")),
                List.of(new DocumentoDados(TipoDocumento.CPF, "12345678900", java.time.LocalDate.of(2020, 1, 1))),
                List.of(new EmailDados("ana@exemplo.com")))).id();
            long funcionario = usuarios.cadastrar(empresa, new UsuarioCadastro("Bia", null, null, Set.of(PerfilUsuario.FUNCIONARIO),
                null, List.of(), List.of(), List.of())).id();
            contexto.getBean(CredencialServico.class).cadastrar(empresa, funcionario,
                new CredencialDados(TipoCredencial.USUARIO_SENHA, "bia", "segredo-123", null, false));
            long veiculo = contexto.getBean(VeiculoServico.class).cadastrar(empresa, cliente, new VeiculoDados(TipoVeiculo.SUV, "SW4", "ABC1234")).id();
            long roda = contexto.getBean(MercadoriaServico.class).cadastrar(empresa,
                new MercadoriaDados("Roda", null, null, null, 10L, new BigDecimal("300.00"), null)).id();
            venda = contexto.getBean(VendaServico.class).cadastrar(empresa,
                new VendaDados("V-1", cliente, funcionario, veiculo, List.of(new ItemDados(roda, 2)), List.of())).id();
        }
        try (var contexto = iniciar()) {
            var usuario = contexto.getBean(UsuarioServico.class).obter(empresa, cliente);
            assertThat(usuario.nome()).isEqualTo("Ana");
            assertThat(usuario.perfis()).containsExactly(PerfilUsuario.CLIENTE);
            assertThat(usuario.documentos()).hasSize(1);
            assertThat(usuario.emails()).hasSize(1);
            assertThat(usuario.endereco().codigoPostal()).isEqualTo("01001000");
            var lida = contexto.getBean(VendaServico.class).obter(empresa, venda);
            assertThat(lida.valorTotal()).isEqualByComparingTo("600.00");
            assertThat(lida.veiculo().placa()).isEqualTo("ABC1234");
            assertThat(contexto.getBean(MercadoriaRepositorio.class).findAll().getFirst().getQuantidade()).isEqualTo(8);
            assertThat(contexto.getBean(CredencialRepositorio.class).count()).isEqualTo(1);
        }
    }
}
