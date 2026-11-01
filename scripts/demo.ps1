param([string]$BaseUrl = 'http://localhost:8081')
$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
$criados = New-Object System.Collections.Generic.List[string]

# Somente a raiz e conhecida; todas as outras URIs vem dos links HATEOAS das respostas.
function Requisitar([string]$Metodo, [string]$Uri, $Corpo = $null, [string]$Aceita = 'application/hal+json') {
    $parametros = @{ Method = $Metodo; Uri = $Uri; Headers = @{ Accept = $Aceita } }
    if ($null -ne $Corpo) {
        $parametros.ContentType = 'application/json; charset=utf-8'
        $parametros.Body = [Text.Encoding]::UTF8.GetBytes(($Corpo | ConvertTo-Json -Depth 10))
    }
    Invoke-RestMethod @parametros
}
function Link($Recurso, [string]$Relacao) {
    $link = $Recurso._links.$Relacao
    if ($null -eq $link) { throw "Link '$Relacao' ausente." }
    $link.href -replace '\{.*\}', ''
}
function Criar([string]$Uri, $Corpo) {
    $recurso = Requisitar POST $Uri $Corpo
    $criados.Insert(0, (Link $recurso 'self'))
    $recurso
}
function Conferir([bool]$Condicao, [string]$Mensagem) {
    if (!$Condicao) { throw $Mensagem }
}

try {
    $raiz = Requisitar GET "$BaseUrl/"
    $empresa = Criar (Link $raiz 'empresas') @{ razaoSocial = 'Car service toyota ltda'; nomeFantasia = 'Car service' }
    Write-Host "Nivel 3: raiz -> empresa $(Link $empresa 'self')"
    $null = Requisitar POST (Link $empresa 'endereco') @{ cidade = 'Sao Paulo'; rua = 'Av. Sao Joao'; numero = '00'; codigoPostal = '01035000' }
    $null = Requisitar POST (Link $empresa 'telefones') @{ ddd = '11'; numero = '986454527' }

    $usuarios = Link $empresa 'usuarios'
    $cliente = Criar $usuarios @{ nome = 'Dom Pedro cliente'; perfis = @('CLIENTE'); documentos = @(@{ tipo = 'CPF'; numero = [guid]::NewGuid().ToString('N').Substring(0, 11); dataEmissao = '2020-01-01' }) }
    $funcionario = Criar $usuarios @{ nome = 'Dom Pedro funcionario'; perfis = @('FUNCIONARIO') }
    $fornecedor = Criar $usuarios @{ nome = 'Loja do carro'; perfis = @('FORNECEDOR') }
    $null = Requisitar POST (Link $cliente 'emails') @{ endereco = 'cliente@autobots.example' }
    $credencial = Requisitar POST (Link $funcionario 'credenciais') @{ tipo = 'USUARIO_SENHA'; nomeUsuario = "func$([guid]::NewGuid().ToString('N').Substring(0, 8))"; senha = [guid]::NewGuid().ToString() }
    Conferir ($null -eq $credencial.senha) 'A senha nao pode aparecer na resposta.'
    Write-Host 'Usuarios: cliente, funcionario e fornecedor associados a empresa; credencial sem senha na resposta.'

    $veiculo = Criar (Link $cliente 'veiculos') @{ tipo = 'SUV'; modelo = 'corolla-cross'; placa = 'ABC0000' }
    $roda = Criar (Link $empresa 'mercadorias') @{ nome = 'Roda de liga leve'; quantidade = 30; valor = 300.00; fornecedorId = $fornecedor.id }
    $troca = Criar (Link $empresa 'servicos') @{ nome = 'Troca de rodas'; valor = 50.00 }
    $alinhamento = Criar (Link $empresa 'servicos') @{ nome = 'Alinhamento'; valor = 50.00 }

    $venda = Criar (Link $empresa 'vendas') @{
        identificacao = [guid]::NewGuid().ToString('N').Substring(0, 10); clienteId = $cliente.id; funcionarioId = $funcionario.id
        veiculoId = $veiculo.id; mercadorias = @(@{ id = $roda.id; quantidade = 4 })
        servicos = @(@{ id = $troca.id; quantidade = 1 }, @{ id = $alinhamento.id; quantidade = 1 })
    }
    Conferir ([decimal]$venda.valorTotal -eq 1300) "Total inesperado: $($venda.valorTotal)."
    Conferir ((Requisitar GET (Link $roda 'self')).quantidade -eq 26) 'A venda deveria baixar o estoque para 26.'
    Conferir ((Link $venda 'veiculo') -eq (Link $veiculo 'self')) 'Venda sem link para o veiculo.'
    $compras = Requisitar GET (Link $cliente 'compras')
    Conferir ($compras.page.totalElements -eq 1) 'Falha nas compras do cliente.'
    $vendasDoVeiculo = Requisitar GET (Link $veiculo 'vendas')
    Conferir ($vendasDoVeiculo.page.totalElements -eq 1) 'Falha nas vendas do veiculo.'
    Write-Host 'Venda: total 1300.00, estoque baixado de 30 para 26, links para cliente, funcionario e veiculo.'

    $formulario = Requisitar GET (Link $cliente 'self') -Aceita 'application/prs.hal-forms+json'
    Conferir ($formulario._templates.default.method -eq 'PUT' -and $formulario._templates.cadastrar.method -eq 'POST') 'Falha nos templates HAL-FORMS.'
    Write-Host 'HAL-FORMS: usuario anuncia PUT, DELETE e cadastro de endereco.'

    $cabecalho = Invoke-WebRequest -Method Head -Uri (Link $empresa 'self') -UseBasicParsing
    Conferir ($cabecalho.StatusCode -eq 200 -and $cabecalho.Content.Length -eq 0) 'Falha no HEAD.'
    try {
        $null = Invoke-WebRequest -Method Patch -Uri (Link $empresa 'self') -ContentType 'application/json' -Body '{}' -UseBasicParsing
        throw 'PATCH deveria retornar 405.'
    } catch {
        $resposta = $_.Exception.Response
        Conferir ($null -ne $resposta -and [int]$resposta.StatusCode -eq 405) 'PATCH deveria retornar 405.'
    }
    Write-Host 'Nivel 2: HEAD 200 sem corpo e PATCH 405.'

    $null = Requisitar DELETE (Link $venda 'self')
    $criados.Remove((Link $venda 'self')) | Out-Null
    Conferir ((Requisitar GET (Link $roda 'self')).quantidade -eq 30) 'Excluir a venda deveria devolver o estoque.'
    Write-Host 'Venda excluida e estoque devolvido. CRUDs verificados por HTTP, navegando apenas por links.'
} finally {
    foreach ($uri in $criados) { try { $null = Requisitar DELETE $uri } catch { Write-Warning "Nao foi possivel excluir $uri" } }
}
