# Origem do projeto

## Base direta: entrega da ATVII

Esta entrega continua o projeto https://github.com/Yuri-Dev-OH/atv2_java (commit `07985bc`), que levou a API aos quatro níveis do Richardson Maturity Model com HAL, HAL-FORMS e Problem Details. A ATVII, por sua vez, continuou a ATVI (https://github.com/Yuri-Dev-OH/atv1_java), que completou os CRUDs de cliente, endereço, documento e telefone.

Já vinham das entregas anteriores: Spring Boot 3.5 e Java 21, DTOs validados, serviços transacionais, Flyway, H2 em arquivo, bloqueio pessimista, verificação de pertencimento nas URLs, `AdicionadorLink*`, paginação com links e erros em `application/problem+json`.

## Referência da ATVIII

Repositório indicado no PDF: https://github.com/gerson-pn/atviii-autobots-microservico-spring (branch `master`, commit `d0d4c21`, consultado em 01/11/2026).

O que essa base contém:

- Spring Boot 2.6.7, Java 17, MySQL (`root`/`root` em `application.properties`) e `ddl-auto=create`.
- Entidades: `Empresa`, `Usuario`, `Endereco`, `Telefone`, `Documento`, `Email`, `Credencial` (abstrata, herança `JOINED`), `CredencialUsuarioSenha`, `CredencialCodigoBarra`, `Veiculo`, `Mercadoria`, `Servico` e `Venda`.
- Enumerações: `PerfilUsuario`, `TipoDocumento` e `TipoVeiculo`.
- Apenas `RepositorioEmpresa` e um `CommandLineRunner` com dados de exemplo; não há controllers, serviços, CRUD nem HATEOAS.

## Alterações nesta entrega

- `Cliente` deu lugar a `Usuario` com perfis; as rotas `/clientes` foram substituídas por `/empresas/{empresaId}/usuarios`.
- Novas entidades e enumerações da base do professor, com as correções descritas em [requisitos.md](requisitos.md#decisões-em-relação-à-base-do-professor).
- Migração `V2__atualizacao_de_base.sql`, que preserva os dados de um banco da ATVII.
- CRUD e HATEOAS para empresa, endereço e telefones da empresa, usuário, endereço e telefones do usuário, documentos, e-mails, credenciais, veículos, mercadorias, serviços e vendas.
- `CodificadorSenha` (PBKDF2 do JDK) no pacote `seguranca`, preparando a "atualização de segurança".
- `DadosDemonstracao` com os dados de exemplo do professor no perfil `demo`.
- Testes, `scripts/demo.ps1`, README e diagrama refeitos para o novo modelo.
