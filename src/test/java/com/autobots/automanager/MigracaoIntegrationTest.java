package com.autobots.automanager;

import java.nio.file.Path;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.enumeracoes.TipoDocumento;
import com.autobots.automanager.servicos.UsuarioServico;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.assertj.core.api.Assertions.assertThat;

/** Garante que um banco da ATVII (esquema V1 com clientes) é migrado para a ATVIII sem perder dados. */
class MigracaoIntegrationTest {
    @TempDir Path pasta;

    @Test void clientesDaAtviiViramUsuariosClientesDeUmaEmpresa() {
        String url = "jdbc:h2:file:" + pasta.resolve("atvii").toAbsolutePath().toString().replace('\\', '/');
        var banco = new DriverManagerDataSource(url, "sa", "");
        Flyway.configure().dataSource(banco).target("1").load().migrate();
        var sql = new JdbcTemplate(banco);
        sql.update("INSERT INTO endereco (cidade, rua, numero) VALUES ('São Paulo', 'Rua A', '1')");
        sql.update("INSERT INTO cliente (nome, data_nascimento, data_cadastro, endereco_id) VALUES ('Ana', DATE '1990-01-01', CURRENT_TIMESTAMP, 1)");
        sql.update("INSERT INTO documento (tipo, numero, cliente_id) VALUES (' rg ', '123', 1), ('Título de eleitor', '456', 1)");
        sql.update("INSERT INTO telefone (ddd, numero, cliente_id) VALUES ('11', '999998888', 1)");

        try (var contexto = new SpringApplicationBuilder(AutomanagerApplication.class).web(WebApplicationType.NONE)
                .run("--spring.datasource.url=" + url, "--spring.datasource.username=sa", "--spring.datasource.password=",
                    "--spring.main.banner-mode=off")) {
            long empresa = sql.queryForObject("SELECT id FROM empresa", Long.class);
            assertThat(sql.queryForObject("SELECT razao_social FROM empresa", String.class)).isEqualTo("Empresa migrada da ATVII");
            var ana = contexto.getBean(UsuarioServico.class).obter(empresa, 1);
            assertThat(ana.nome()).isEqualTo("Ana");
            assertThat(ana.perfis()).containsExactly(PerfilUsuario.CLIENTE);
            assertThat(ana.endereco().cidade()).isEqualTo("São Paulo");
            assertThat(ana.telefones()).hasSize(1);
            assertThat(ana.documentos()).extracting(d -> d.tipo()).containsExactly(TipoDocumento.RG, TipoDocumento.OUTRO);
        }
    }

    @Test void bancoVazioDaAtviiNaoGanhaEmpresaFicticia() {
        String url = "jdbc:h2:file:" + pasta.resolve("vazio").toAbsolutePath().toString().replace('\\', '/');
        var banco = new DriverManagerDataSource(url, "sa", "");
        Flyway.configure().dataSource(banco).load().migrate();
        assertThat(new JdbcTemplate(banco).queryForObject("SELECT COUNT(*) FROM empresa", Long.class)).isZero();
    }
}
