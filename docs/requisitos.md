# Requisitos da atividade ATVIII

Fonte: `atviii-autoboots-micro-servico-java.pdf`, três páginas, incluindo a Figura 1 (diagrama de classes) e a Figura 2 (QR code do repositório).

## Escopo

O PDF divide as mudanças em "atualização de base" e "atualização de segurança". Esta entrega é a **atualização de base**: o sistema passa a guardar usuários, veículos, serviços, peças, vendas "e o que mais for minimamente necessário para a operação de uma loja de manutenção veicular".

> "O objetivo é implementar estas classes no sistema, bem como todo o CRUD para as entidades e HATEOAS."

O PDF permite modificar o código sugerido, desde que estas funcionalidades não sejam negligenciadas:

1. Cadastro de empresa, a unidade comercial que oferece manutenção e venda de mercadorias.
2. Associação de usuários a uma empresa.
3. Tipos de usuário: cliente, fornecedor ou funcionário.
4. Cadastro de veículos, mercadorias e serviços, para gerenciar vendas e serviços prestados a um usuário e ao seu veículo.
5. Credencial de acesso de um usuário ao sistema.

A autenticação e a autorização ficam para a "atualização de segurança", que não faz parte desta atividade. O diagrama da Figura 1 está ilegível na resolução do PDF, por isso as classes foram tiradas do repositório indicado (ver [origem.md](origem.md)).

## Verificação da entrega

| Requisito | Implementação | Verificação |
|---|---|---|
| 1. Cadastro de empresa | `Empresa` com razão social, nome fantasia, data de cadastro, endereço e telefones; CRUD em `/empresas` | `CadastroIntegrationTest.empresaTemCrudCompletoComEnderecoETelefones`, `empresaComUsuariosNaoPodeSerExcluida` |
| 2. Usuários associados à empresa | `Usuario` pertence a uma empresa; rotas `/empresas/{id}/usuarios`; um usuário de outra empresa responde 404 | `usuarioTemCrudCompletoEPertenceAEmpresa` |
| 3. Tipos de usuário | `PerfilUsuario` (`CLIENTE`, `FUNCIONARIO`, `FORNECEDOR`); um usuário pode acumular perfis; filtro `?perfil=`; links e regras dependem do perfil | `listagemDeUsuariosFiltraPorPerfil`, `linksDoUsuarioDependemDosPerfis`, `registrosVendidosNaoPodemSerApagadosNemPerderPerfil` |
| 4. Veículos, mercadorias, serviços e vendas | CRUDs de `Veiculo` (do cliente), `Mercadoria` (estoque, com fornecedor), `Servico` (catálogo) e `Venda` (cliente, funcionário, veículo, itens) | `VendaIntegrationTest` (8 testes) |
| 5. Credencial de acesso | `Credencial` abstrata com `CredencialUsuarioSenha` e `CredencialCodigoBarra`; senha gravada como hash PBKDF2 e nunca devolvida | `credencialGuardaSomenteHashDaSenha`, `credenciaisValidamCamposPorTipoEUnicidade`, `CodificadorSenhaTest` |
| Demais classes do diagrama | `Documento` (com tipo enumerado e data de emissão), `Email`, `Endereco` e `Telefone` (de usuário e de empresa) | `documentosSaoUnicosPorEmpresa`, `emailsTemCrudEValidacao` |
| HATEOAS | `AdicionadorLink*` para os 13 recursos; raiz `/`; paginação; HAL-FORMS com ações conforme o estado | `MaturidadeRestIntegrationTest` (10 testes), inclusive `operacaoCompletaDaLojaApenasSeguindoLinks` |
| Dados da ATVII preservados | Migração Flyway `V2` transforma clientes em usuários `CLIENTE` de uma empresa criada para recebê-los | `MigracaoIntegrationTest` |

Em 01/11/2026, no Windows com Java 21:

- `verify`: **33 testes, zero falhas, zero erros, nenhum ignorado; BUILD SUCCESS**.
- Cobertura JaCoCo: **853 de 916 linhas (93,1%)** e **136 de 154 ramificações (88,3%)**. A maior parte das linhas não cobertas é de `DadosDemonstracao`, que só roda com o perfil `demo` e foi verificado à mão.
- JAR iniciado na porta 18083 com banco temporário: `scripts/demo.ps1` (Windows PowerShell 5.1) partiu da raiz e, só por links, cadastrou empresa, três usuários, credencial, veículo, mercadoria, dois serviços e uma venda. Conferiu total (1300,00), baixa de estoque (30 para 26), devolução do estoque ao excluir a venda, HAL-FORMS, `HEAD` e `405`, e removeu tudo o que criou.
- JAR com `--spring.profiles.active=demo` e `AUTOMANAGER_DEMO_SENHA` definida: carregou a empresa, 3 usuários, 2 vendas (1300,00 e 80,00) e credenciais dos dois tipos.

## Decisões em relação à base do professor

| Base do professor | Esta entrega | Motivo |
|---|---|---|
| Senha em texto puro | Hash PBKDF2-HMAC-SHA256 (600.000 iterações, sal aleatório), nunca serializada | Segurança mínima de credenciais, com a biblioteca padrão do JDK |
| `Venda` → `@OneToMany Mercadoria`: a mesma mercadoria não pode estar em duas vendas e o estoque não muda | Itens embutidos (`ItemMercadoria`, `ItemServico`) com quantidade e preço da época; a venda baixa o estoque e a exclusão o devolve | Uma loja vende a mesma peça várias vezes; o histórico não pode mudar quando o preço muda |
| `Venda` → `@OneToOne Veiculo`, mas `Veiculo` com várias vendas | `@ManyToOne` | Um veículo passa por várias manutenções |
| `EAGER` e `CascadeType.ALL` em quase tudo; empresa carrega todas as vendas | `LAZY`, cascata só onde há posse (contatos, documentos, credenciais, veículos do usuário); `default_batch_fetch_size` | Consultar uma empresa não traz o histórico inteiro; excluir uma empresa com movimento é recusado (409) em vez de apagar tudo |
| `@Data` nas entidades, com relações cíclicas | `@Getter/@Setter` e DTOs (`record`) | Evita recursão em `toString`/`hashCode` e mantém a separação entre persistência e API da ATVI/ATVII |
| `java.util.Date`, `double` para dinheiro, `fabricao` | `LocalDate`/`Instant`, `BigDecimal` com 2 casas, `fabricacao` | Precisão monetária e datas sem fuso implícito |
| `ddl-auto=create` e MySQL com usuário `root/root` no código | Flyway (`V2`) e H2 em arquivo, credenciais por variável de ambiente | Mantém os dados entre reinícios e não guarda segredos no repositório |
| `CommandLineRunner` sempre ativo com senhas `123456` | `DadosDemonstracao` só no perfil `demo`; senhas vêm de `AUTOMANAGER_DEMO_SENHA` | Dados de exemplo não entram em produção |
| Documento com número único global | Único por empresa (`empresa_id`, `tipo`, `numero`) | A mesma pessoa pode ser cliente de duas unidades |
| `TipoDocumento` sem valor para tipos livres | Acrescentado `OUTRO` | Recebe os tipos digitados livremente na ATVII durante a migração |
| Sem regras entre perfis | Venda exige cliente `CLIENTE` e funcionário `FUNCIONARIO`; veículo só para `CLIENTE`; fornecedor de mercadoria precisa de `FORNECEDOR`; perfil em uso não pode ser retirado | Faz os tipos de usuário terem efeito no sistema |

## Inspeção do PDF

O texto das três páginas e as figuras foram examinados. Não há instruções dirigidas à IA, ações JavaScript, arquivos incorporados ou comandos de execução. Essa inspeção não substitui uma análise antivírus.
