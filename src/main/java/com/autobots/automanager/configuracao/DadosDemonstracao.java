package com.autobots.automanager.configuracao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import com.autobots.automanager.dto.Dados.*;
import com.autobots.automanager.enumeracoes.*;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.servicos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Carrega os dados de exemplo da base do professor (empresa, funcionário, fornecedor, cliente, veículo, catálogo e
 * duas vendas) quando o perfil {@code demo} está ativo e o banco não tem empresas.
 *
 * <p>Passa pelos mesmos serviços da API, então as regras valem também aqui. As credenciais de usuário e senha só são
 * criadas se a variável {@code AUTOMANAGER_DEMO_SENHA} estiver definida: nenhuma senha fica no código.
 */
@Component
@Profile("demo")
public class DadosDemonstracao implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(DadosDemonstracao.class);
    private final EmpresaRepositorio empresas;
    private final EmpresaServico empresaServico;
    private final UsuarioServico usuarioServico;
    private final CredencialServico credencialServico;
    private final VeiculoServico veiculoServico;
    private final MercadoriaServico mercadoriaServico;
    private final ServicoServico servicoServico;
    private final VendaServico vendaServico;
    private final String senha;

    public DadosDemonstracao(EmpresaRepositorio empresas, EmpresaServico empresaServico, UsuarioServico usuarioServico,
            CredencialServico credencialServico, VeiculoServico veiculoServico, MercadoriaServico mercadoriaServico,
            ServicoServico servicoServico, VendaServico vendaServico, @Value("${AUTOMANAGER_DEMO_SENHA:}") String senha) {
        this.empresas = empresas; this.empresaServico = empresaServico; this.usuarioServico = usuarioServico;
        this.credencialServico = credencialServico; this.veiculoServico = veiculoServico;
        this.mercadoriaServico = mercadoriaServico; this.servicoServico = servicoServico; this.vendaServico = vendaServico;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (empresas.count() > 0) {
            LOG.info("Perfil demo: o banco já possui empresas; nenhum dado de exemplo foi carregado.");
            return;
        }
        long empresa = empresaServico.cadastrar(new EmpresaCadastro("Car service toyota ltda", "Car service manutenção veicular",
            endereco("São Paulo", "São Paulo", "Centro", "Av. São João", "01035000"), List.of(new TelefoneDados("11", "986454527")))).id();

        long funcionario = usuarioServico.cadastrar(empresa, new UsuarioCadastro("Pedro Alcântara de Bragança e Bourbon", "Dom Pedro",
            null, Set.of(PerfilUsuario.FUNCIONARIO), endereco("São Paulo", "São Paulo", "Jardins", "Av. São Gabriel", "01435001"),
            List.of(new TelefoneDados("11", "985463372")), List.of(documento(TipoDocumento.CPF, "85647381922")),
            List.of(new EmailDados("funcionario@autobots.example")))).id();
        long fornecedor = usuarioServico.cadastrar(empresa, new UsuarioCadastro("Componentes varejo de partes automotivas ltda",
            "Loja do carro", null, Set.of(PerfilUsuario.FORNECEDOR),
            endereco("Rio de Janeiro", "Rio de Janeiro", "Centro", "Av. República do Chile", "20031170"), List.of(),
            List.of(documento(TipoDocumento.CNPJ, "00014556000100")), List.of(new EmailDados("fornecedor@autobots.example")))).id();
        long cliente = usuarioServico.cadastrar(empresa, new UsuarioCadastro("Pedro Alcântara de Bragança e Bourbon", "Dom Pedro cliente",
            LocalDate.of(1798, 10, 12), Set.of(PerfilUsuario.CLIENTE),
            endereco("São Paulo", "São José dos Campos", "Centro", "Av. Dr. Nelson D'Ávila", "12245070"), List.of(),
            List.of(documento(TipoDocumento.CPF, "12584698533")), List.of(new EmailDados("cliente@autobots.example")))).id();

        if (senha.length() < 8) {
            LOG.info("Perfil demo: AUTOMANAGER_DEMO_SENHA ausente ou com menos de 8 caracteres; credenciais de usuário e senha não foram criadas.");
        } else {
            credencialServico.cadastrar(empresa, funcionario, new CredencialDados(TipoCredencial.USUARIO_SENHA, "dompedrofuncionario", senha, null, false));
            credencialServico.cadastrar(empresa, fornecedor, new CredencialDados(TipoCredencial.USUARIO_SENHA, "dompedrofornecedor", senha, null, false));
            credencialServico.cadastrar(empresa, cliente, new CredencialDados(TipoCredencial.USUARIO_SENHA, "dompedrocliente", senha, null, false));
        }
        credencialServico.cadastrar(empresa, funcionario, new CredencialDados(TipoCredencial.CODIGO_BARRA, null, null, 7891234567895L, false));

        long veiculo = veiculoServico.cadastrar(empresa, cliente, new VeiculoDados(TipoVeiculo.SUV, "corolla-cross", "ABC0000")).id();
        long roda = mercadoriaServico.cadastrar(empresa, new MercadoriaDados("Roda de liga leve modelo toyota etios",
            "Roda de liga leve original de fábrica da toyota para modelos do tipo hatch", LocalDate.now(), null, 30L,
            new BigDecimal("300.00"), fornecedor)).id();
        long trocaRodas = servicoServico.cadastrar(empresa, new ServicoDados("Troca de rodas", "Troca das rodas do carro por novas", new BigDecimal("50.00"))).id();
        long alinhamento = servicoServico.cadastrar(empresa, new ServicoDados("Alinhamento de rodas", "Alinhamento das rodas do carro", new BigDecimal("50.00"))).id();
        long balanceamento = servicoServico.cadastrar(empresa, new ServicoDados("Balanceamento de rodas", "Balanceamento das rodas do carro", new BigDecimal("30.00"))).id();

        vendaServico.cadastrar(empresa, new VendaDados("1234698745", cliente, funcionario, veiculo,
            List.of(new ItemDados(roda, 4)), List.of(new ItemDados(trocaRodas, 1), new ItemDados(alinhamento, 1))));
        vendaServico.cadastrar(empresa, new VendaDados("1234698749", cliente, funcionario, veiculo,
            List.of(), List.of(new ItemDados(balanceamento, 1), new ItemDados(alinhamento, 1))));
        LOG.info("Perfil demo: dados de exemplo carregados na empresa {}.", empresa);
    }

    private static EnderecoDados endereco(String estado, String cidade, String bairro, String rua, String cep) {
        return new EnderecoDados(estado, cidade, bairro, rua, "00", cep, null);
    }
    private static DocumentoDados documento(TipoDocumento tipo, String numero) {
        return new DocumentoDados(tipo, numero, LocalDate.of(2020, 1, 1));
    }
}
