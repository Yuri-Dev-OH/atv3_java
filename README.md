# AutoBots — automanager: atualização de base (ATVIII)

API Java/Spring para lojas de manutenção veicular e venda de autopeças. Esta versão implementa a "atualização de base" pedida em `atviii-autoboots-micro-servico-java.pdf`: **empresas, usuários com perfis (cliente, funcionário, fornecedor), credenciais de acesso, veículos, mercadorias, serviços e vendas**, todos com CRUD e **HATEOAS** (HAL e HAL-FORMS), mantendo os quatro níveis de maturidade REST da ATVII.

A origem está em [docs/origem.md](docs/origem.md); o escopo, a verificação e as decisões em relação à base do professor, em [docs/requisitos.md](docs/requisitos.md).

## Executar

É necessário **JDK 21**, com `JAVA_HOME` configurado. O projeto inclui o Maven Wrapper; não é preciso instalar Maven, Docker ou banco separado. A primeira compilação precisa de internet para baixar as dependências.

No PowerShell, na pasta deste README:

```powershell
.\mvnw.cmd "-Dmaven.repo.local=.tools/m2" verify
java -jar target/automanager-3.0.0.jar
```

No Git Bash ou Linux:

```bash
sh mvnw -Dmaven.repo.local=.tools/m2 verify
java -jar target/automanager-3.0.0.jar
```

A API fica em **http://localhost:8081/** (ponto de entrada HATEOAS). O banco H2 é gravado em `dados/` e permanece após reiniciar. Um banco da ATVII é migrado automaticamente: os clientes viram usuários `CLIENTE` de uma empresa criada para recebê-los.

### Dados de exemplo (opcional)

Para carregar os dados da base do professor (empresa, funcionário, fornecedor, cliente, veículo, mercadoria, serviços e duas vendas), use o perfil `demo`. As credenciais de usuário e senha só são criadas se `AUTOMANAGER_DEMO_SENHA` estiver definida; nenhuma senha fica no código.

```powershell
$env:AUTOMANAGER_DEMO_SENHA = "escolha-uma-senha"
java -jar target/automanager-3.0.0.jar --spring.profiles.active=demo
```

Os dados só são carregados se o banco não tiver empresas. Opcionalmente, com Docker: `docker compose up --build -d`.

## Testar

### 1. Testes automatizados

```powershell
.\mvnw.cmd "-Dmaven.repo.local=.tools/m2" verify
```

O resultado esperado é **BUILD SUCCESS**, com 33 testes, zero falhas e zero erros. A cobertura fica em `target/site/jacoco/index.html`. Para rodar uma classe só:

```powershell
.\mvnw.cmd "-Dmaven.repo.local=.tools/m2" test "-Dtest=VendaIntegrationTest"
```

| Classe | O que verifica |
|---|---|
| `CadastroIntegrationTest` | CRUD de empresa, usuário, endereço, telefones, documentos, e-mails e credenciais; perfis; isolamento entre empresas; senha só como hash |
| `VendaIntegrationTest` | CRUD de veículos, mercadorias, serviços e vendas; baixa e devolução de estoque; regras de perfil; vendas simultâneas |
| `MaturidadeRestIntegrationTest` | Nível 2 (405, 415, 406, HEAD, OPTIONS, `Location`, Problem Details) e nível 3 (raiz, operação completa só por links, paginação, HAL-FORMS) |
| `MigracaoIntegrationTest` | Banco da ATVII migrado sem perda de dados |
| `PersistenciaIntegrationTest` | Dados preservados após fechar e reabrir o banco em arquivo |
| `CodificadorSenhaTest` | Hash PBKDF2 com sal aleatório e conferência da senha |

### 2. Demonstração HTTP real

Com a API aberta, em outro PowerShell:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\demo.ps1
```

O script conhece **apenas a raiz** `http://localhost:8081/`. Seguindo os `_links`, ele cadastra empresa, cliente, funcionário, fornecedor, credencial, veículo, mercadoria, serviços e uma venda. Depois confere o total, a baixa e a devolução de estoque, os templates HAL-FORMS, `HEAD` e `405`, e remove tudo o que criou. Para outra porta: `-BaseUrl http://localhost:8082`.

### 3. Manualmente (curl)

```bash
curl http://localhost:8081/
curl -i -X POST http://localhost:8081/empresas -H "Content-Type: application/json" \
     -d '{"razaoSocial":"Car service toyota ltda"}'
curl -X POST http://localhost:8081/empresas/1/usuarios -H "Content-Type: application/json" \
     -d '{"nome":"Dom Pedro","perfis":["CLIENTE"]}'
curl -X POST http://localhost:8081/empresas/1/usuarios -H "Content-Type: application/json" \
     -d '{"nome":"Funcionario","perfis":["FUNCIONARIO"]}'
curl -X POST http://localhost:8081/empresas/1/usuarios/1/veiculos -H "Content-Type: application/json" \
     -d '{"tipo":"SUV","modelo":"corolla-cross","placa":"ABC1D23"}'
curl -X POST http://localhost:8081/empresas/1/mercadorias -H "Content-Type: application/json" \
     -d '{"nome":"Roda","quantidade":30,"valor":300.00}'
curl -X POST http://localhost:8081/empresas/1/servicos -H "Content-Type: application/json" \
     -d '{"nome":"Alinhamento","valor":50.00}'
curl -X POST http://localhost:8081/empresas/1/vendas -H "Content-Type: application/json" \
     -d '{"identificacao":"V-1","clienteId":1,"funcionarioId":2,"veiculoId":1,"mercadorias":[{"id":1,"quantidade":4}],"servicos":[{"id":1,"quantidade":1}]}'
curl -H "Accept: application/prs.hal-forms+json" http://localhost:8081/empresas/1/usuarios/1
curl "http://localhost:8081/empresas/1/usuarios?perfil=CLIENTE"
```

No PowerShell, use `curl.exe` em vez de `curl` (que é um alias de `Invoke-WebRequest`) e escape as aspas do JSON como `\"`.

## Modelo

| Entidade | Pertence a | Observações |
|---|---|---|
| Empresa | — | Unidade comercial; tem endereço e telefones |
| Usuário | Empresa | Perfis `CLIENTE`, `FUNCIONARIO`, `FORNECEDOR` (pode acumular); endereço, telefones, documentos, e-mails, credenciais |
| Credencial | Usuário | `USUARIO_SENHA` (nome único, senha em hash) ou `CODIGO_BARRA` (código único); `inativo` |
| Veículo | Usuário `CLIENTE` | Tipo `HATCH`, `SEDA`, `SUV`, `PICKUP`, `SW`; placa única na empresa |
| Mercadoria | Empresa | Estoque (`quantidade`), valor, fabricação e validade; fornecedor opcional com perfil `FORNECEDOR` |
| Serviço | Empresa | Catálogo com nome, descrição e valor |
| Venda | Empresa | Cliente, funcionário, veículo opcional do cliente, itens de mercadoria e serviço com quantidade e preço da época, total |

O diagrama está em [docs/diagrama.puml](docs/diagrama.puml).

## Níveis de maturidade (RMM)

| Nível | Requisito | Implementação |
|---|---|---|
| 0 | Uso do protocolo HTTP | API HTTP/JSON com Spring MVC |
| 1 | URI única por recurso | `/empresas/{id}`, `/empresas/{id}/usuarios/{id}`, `/empresas/{id}/usuarios/{id}/veiculos/{id}`, `/empresas/{id}/vendas/{id}` etc. |
| 2 | Métodos e suas semânticas | `GET` seguro, `PUT` idempotente, `POST` cria com `201` e `Location` absoluto, `DELETE` responde `204`. Status `400`, `404`, `405` (com `Allow`), `406`, `409`, `415` e `422`. `HEAD` e `OPTIONS` automáticos. Erros em `application/problem+json` (RFC 9457) |
| 3 | HATEOAS | Todas as respostas trazem `_links` (HAL). A raiz `/` é o ponto de entrada. Coleções paginadas têm `first`, `prev`, `next`, `last`. Com `Accept: application/prs.hal-forms+json`, as respostas trazem `_templates` com as ações permitidas |

### Links por recurso

| Recurso | Relações | Templates HAL-FORMS |
|---|---|---|
| Raiz `/` | `self`, `empresas` | `default`: POST empresa |
| Empresa | `self`, `empresas`, `endereco`, `telefones`, `usuarios`, `mercadorias`, `servicos`, `vendas` | `default`: PUT, `excluir`: DELETE e, **sem endereço**, `cadastrar`: POST endereço |
| Usuário | `self`, `usuarios`, `empresa`, `endereco`, `telefones`, `documentos`, `emails`, `credenciais`; **conforme o perfil**: `veiculos` e `compras` (cliente), `vendas` (funcionário), `mercadorias` (fornecedor) | `default`: PUT, `excluir`: DELETE e, sem endereço, `cadastrar`: POST endereço |
| Veículo | `self`, `veiculos`, `proprietario`, `vendas` (do veículo) | PUT, DELETE |
| Mercadoria | `self`, `mercadorias`, `empresa`, `fornecedor` (se houver) | PUT, DELETE |
| Serviço | `self`, `servicos`, `empresa` | PUT, DELETE |
| Venda | `self`, `vendas`, `empresa`, `cliente`, `funcionario`, `veiculo` (se houver) | PUT, DELETE |
| Endereço, telefone, documento, e-mail, credencial | `self`, a coleção, o dono (`empresa` ou `usuario`) | PUT, DELETE |
| Coleções | `self` e, nas paginadas, `first`, `prev`, `next`, `last` e `empresa` | `default`: POST |

Listas vazias seguem o padrão do HAL: trazem `_links`, mas não `_embedded`.

### Erros (Problem Details)

```json
{ "type": "about:blank", "title": "Regra de negócio violada.", "status": 422,
  "detail": "Cliente informado não tem o perfil CLIENTE.", "instance": "/empresas/1/vendas" }
```

| Status | Quando |
|---|---|
| 400 | JSON malformado, campo desconhecido, tipo errado ou validação (`detalhes` lista cada campo) |
| 404 | Recurso inexistente ou de outra empresa/usuário |
| 409 | Valor único repetido, estoque insuficiente, registro ainda usado (ex.: excluir mercadoria já vendida) ou perfil em uso |
| 422 | Regra de negócio: perfil errado, veículo de outro cliente, item de outra empresa, item repetido, troca de tipo de credencial |

## Rotas

Todas as rotas abaixo de `/empresas/{empresaId}` só enxergam dados daquela empresa.

| Método | Caminho | Operação |
|---|---|---|
| GET | `/` | Ponto de entrada com links |
| GET, POST | `/empresas?pagina&tamanho` | Listar (paginado) e cadastrar empresa, opcionalmente com endereço e telefones |
| GET, PUT, DELETE | `/empresas/{id}` | Consultar, atualizar razão social e nome fantasia, excluir (só sem usuários, estoque, serviços e vendas) |
| POST, GET, PUT, DELETE | `/empresas/{id}/endereco` | Endereço da empresa |
| GET, POST / GET, PUT, DELETE | `/empresas/{id}/telefones[/{id}]` | Telefones da empresa |
| GET, POST | `/empresas/{id}/usuarios?perfil&pagina&tamanho` | Listar (filtro por perfil) e cadastrar usuário, opcionalmente com endereço, telefones, documentos e e-mails |
| GET, PUT, DELETE | `/empresas/{id}/usuarios/{id}` | Consultar, atualizar nome, nome social, nascimento e perfis, excluir |
| POST, GET, PUT, DELETE | `/empresas/{id}/usuarios/{id}/endereco` | Endereço do usuário |
| GET, POST / GET, PUT, DELETE | `/empresas/{id}/usuarios/{id}/{telefones,documentos,emails,credenciais,veiculos}[/{id}]` | Sub-recursos do usuário |
| GET, POST | `/empresas/{id}/mercadorias?fornecedorId&pagina&tamanho` | Estoque |
| GET, PUT, DELETE | `/empresas/{id}/mercadorias/{id}` | Mercadoria (o PUT ajusta o estoque) |
| GET, POST / GET, PUT, DELETE | `/empresas/{id}/servicos[/{id}]` | Catálogo de serviços (paginado) |
| GET, POST | `/empresas/{id}/vendas?clienteId&funcionarioId&veiculoId&pagina&tamanho` | Vendas com filtros |
| GET, PUT, DELETE | `/empresas/{id}/vendas/{id}` | Consultar, refazer (devolve e baixa estoque) e cancelar (devolve estoque) |

## Regras

- IDs e datas de cadastro são gerados pelo servidor. Campos desconhecidos são rejeitados, inclusive `id` e `_links` no corpo.
- `PUT` substitui os campos do recurso; campos opcionais omitidos são limpos. Exceção: na credencial, `senha` omitida mantém a atual.
- A senha só aparece na entrada: é gravada como hash PBKDF2-HMAC-SHA256 (600.000 iterações; `SENHA_ITERACOES` altera) e nunca é devolvida. `ultimoAcesso` será preenchido pela autenticação da futura "atualização de segurança".
- Documento (tipo e número) e placa são únicos por empresa; nome de usuário e código de barras, em todo o sistema; e-mail, por usuário; identificação da venda, por empresa.
- Veículos só para usuários `CLIENTE`. A venda exige cliente `CLIENTE` e funcionário `FUNCIONARIO` da mesma empresa, e o veículo, se informado, precisa ser do cliente. Um perfil não pode ser retirado enquanto houver veículos, vendas ou mercadorias que dependam dele.
- A venda guarda nome, quantidade e preço de cada item no momento da venda; baixa o estoque das mercadorias (409 se insuficiente). Atualizar a venda devolve o estoque anterior e baixa o novo; excluí-la devolve o estoque. As mercadorias são bloqueadas durante a venda, evitando que vendas simultâneas vendam a mesma unidade.
- Mercadorias e serviços já vendidos, veículos com vendas e usuários citados em vendas não podem ser excluídos (409), preservando o histórico.
- Os links são gerados a partir do host da requisição. Atrás de um proxy reverso confiável, defina `FORWARD_HEADERS_STRATEGY=framework`.

## Organização

```text
src/main/java/com/autobots/automanager/
  controles/     rotas HTTP; RaizControle é o ponto de entrada HATEOAS
  modelo/        AdicionadorLink* (links e affordances), paginação, selecionadores e atualizadores
  dto/           entradas validadas e respostas (com @Relation para o HAL)
  entidades/     entidades JPA da Figura 1
  enumeracoes/   PerfilUsuario, TipoDocumento, TipoVeiculo, TipoCredencial
  servicos/      transações e regras de negócio
  repositorios/  acesso ao banco
  seguranca/     hash de senhas (base para a atualização de segurança)
  erros/         Problem Details (RFC 9457)
  configuracao/  JSON estrito, HAL/HAL-FORMS e dados do perfil demo
src/main/resources/db/migration/  esquema versionado com Flyway (V1 da ATVII, V2 da ATVIII)
src/test/                        testes automatizados
scripts/demo.ps1                 demonstração HTTP navegando por links
```
