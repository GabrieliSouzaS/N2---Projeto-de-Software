# Minha Refeição — Assinar Plano de Refeições (implementação em Java)

Implementação em Java do cenário **Assinar Plano de Refeições**, a partir do diagrama de
sequência e do diagrama de classes de projeto elaborados na etapa anterior (N2).
Projeto de Software — Faculdade de Computação e Informática — Mackenzie.

- **Integrantes:**
- José Maike Ferreira Ferro - 10732694
- Gabrieli Souza Santos - 10732702

## O que o sistema faz

O assinante informa o celular, valida o código recebido por SMS, escolhe o plano e a
preferência alimentar, monta o pedido (pratos principais, acompanhamentos e sobremesas),
informa o endereço de entrega e paga com cartão. Ao final, a assinatura fica **ATIVA**, o pedido
**APROVADO** e o assinante recebe um protocolo.

Fluxos tratados: fluxo principal e os fluxos alternativos **FA1** (código inválido, bloqueio e
reenvio), **FA2** (item indisponível e limite do plano) e **FA3** (pagamento não autorizado,
nova tentativa e desistência).

## Requisitos

- **JDK 17 ou superior** (testado com JDK 21). Não há dependências externas nem Maven/Gradle.
- Terminal com UTF-8 (no Windows, o `run.bat` já executa `chcp 65001`).

## Como executar

Na raiz do projeto:

| Sistema | Executar o sistema | Rodar os testes | Rodar um roteiro de demonstração |
|---|---|---|---|
| Linux / macOS | `./run.sh` | `./testar.sh` | `./demo.sh 1` |
| Windows | `run.bat` | `testar.bat` | `demo.bat 1` |

Sem IDE e sem scripts, o equivalente é:

```
javac -encoding UTF-8 -d out $(find src/main -name "*.java")
java -cp out minharefeicao.Main
```

### Uso interativo

`./run.sh` conduz pelas 12 telas do protótipo. Cada pergunta aparece como `> Pergunta:`.
O SMS é **simulado**: a mensagem com o código aparece no console (`[SMS para ...]`) e é gravada
em `dados/sms_enviados.log`.

Opções do programa (podem ser passadas ao `run.sh` / `run.bat`):

| Opção | Efeito |
|---|---|
| `--codigo-fixo=4719` | O SMS sempre traz esse código (para demonstrações) |
| `--eco` | Repete na tela o que foi lido (útil com entrada vinda de arquivo) |
| `--sem-atraso` | A operadora de cartão simulada responde na hora |
| `--zerar` | Apaga os dados gravados em execuções anteriores |
| `--dados=PASTA` | Usa outra pasta de dados (padrão: `dados`) |

### Dados para testar

- **Celular válido:** `(11) 9 8888-1234` (DDD + 9 + 8 dígitos).
- **Cartão aprovado:** `4111 1111 1111 4521`, validade `08/29`, CVV `123`.
- **Cartão recusado:** qualquer número terminado em `0000` (ex.: `4111 1111 1111 0000`).
  Validade vencida ou CVV com menos de 3 dígitos também são recusados.
- **Limite do plano:** o plano limita a quantidade de **pratos principais** do pedido
  (Semanal 5, Pacote de dias 15, Mensal 20). Acompanhamentos e sobremesas não têm limite.
- **Item indisponível:** o *Quibe assado* só existe no plano Mensal.

### Roteiros de demonstração (`demo/`)

Cada roteiro é um arquivo de texto com as respostas do assinante, uma por linha.
`./demo.sh N` apaga os dados anteriores, usa o código `4719` e mostra tudo na tela.

| Roteiro | Cenário demonstrado | Resultado esperado |
|---|---|---|
| `1-fluxo-principal` | Fluxo principal completo | Assinatura ativa e protocolo |
| `2-celular-invalido` | Celular inválido (break do diagrama) | Recusa e nova tentativa |
| `3-fa1-codigo-invalido` | FA1: dois códigos errados e depois o certo | "Você ainda tem 2 / 1 tentativa(s)" |
| `4-fa1-bloqueio-e-reenvio` | FA1: 3 erros, bloqueio e reenvio do código | "Verificação bloqueada" e novo SMS |
| `5-fa2-indisponivel-e-limite` | FA2: item indisponível e limite do plano | Avisos e pedido ajustado |
| `6-fa3-recusado-e-outro-cartao` | FA3: recusa, nova tentativa e outro cartão | Assinatura ativa no fim |
| `7-fa3-desistencia` | FA3: recusa e desistência | "Processo encerrado", nada cobrado |

## Testes automatizados

`./testar.sh` roda `TesteFluxos` (89 verificações, só com o JDK): fluxo principal, celular
inválido, FA1, FA2, FA3, regras de domínio, Notificador, estados inválidos do controle e os 7
roteiros de `demo/` passando pela fronteira de console. Os testes usam pastas temporárias e
não alteram a pasta `dados/`.

## Estrutura do projeto

```
src/main/java/minharefeicao/
  Main.java                          monta os objetos e inicia o cenário
  fronteira/   ViewAssinatura        «boundary» do ator Assinante (console)
               InterfaceOperadoraCartao  «boundary» da Operadora de Cartão (simulada)
               RespostaOperadora     «datatype» com o resultado da operadora
  controle/    CtlAssinarPlano       «control» do caso de uso
  servico/     Notificador           «service» que gera o código e envia o SMS (simulado)
  dominio/     Assinante, CodigoVerificacao, Assinatura, Plano, PreferenciaAlimentar,
               Endereco, Cardapio, ItemCardapio, Pedido, ItemPedido, Pagamento
               + enums StatusAssinatura, StatusPedido, StatusPagamento, CategoriaItem
  persistencia/ RepositorioMarmitas, ArmazenamentoCsv   arquivos CSV
src/test/java/minharefeicao/TesteFluxos.java
dados/        catálogo em CSV (planos, preferências, cardápio) e dados gravados na execução
demo/         roteiros de demonstração
docs/         diagramas (PNG, SVG, PlantUML, PDF) e protótipo HTML
wiki/         páginas da Wiki do GitHub (copiar para o repositório .wiki)
```

## Correspondência com os diagramas

Os nomes de classes, métodos e parâmetros do código são os do **diagrama de classes de projeto**,
e a ordem das chamadas segue o **diagrama de sequência**. Os comentários `Seq. msg N` no código
indicam o número da mensagem no diagrama de sequência (65 mensagens, em 3 partes).
A tabela completa mensagem → método está na Wiki (página *Mapeamento Diagrama × Código*).

Itens de implementação que **não aparecem** nos diagramas (todos documentados na Wiki):

- `ViewAssinatura.executar()` e os métodos privados de exibição: mecânica do console.
- Métodos de acesso (`getX`) e `definirId`: omitidos dos diagramas por serem de infraestrutura.
- Pacote `persistencia`: os diagramas modelam só o domínio; o controle chama o repositório.
- Atributo `idsPlanos` em `ItemCardapio`: dado necessário para `estaDisponivelPara(plano)`.
- `Date` e `DateTime` dos diagramas são `LocalDate` e `LocalDateTime` em Java.

## Persistência

Arquivos CSV (separador `;`, UTF-8) na pasta `dados/`. Catálogo (versionado, somente leitura):
`planos.csv`, `preferencias.csv`, `cardapio.csv`. Gravados na execução (ignorados pelo Git):
`assinantes.csv`, `assinaturas.csv`, `pedidos.csv`, `itens_pedido.csv`, `pagamentos.csv` e o log
de SMS `sms_enviados.log`. O código de verificação não é persistido (é temporário por segurança).
Detalhes e colunas na Wiki (página *Persistência*).

## Decisões e limitações

- **SMS e operadora de cartão são simulados**; nenhum serviço real é chamado.
- A regra de limite do plano vale para `PRATO_PRINCIPAL` e usa `Plano.qtdRefeicoes` como máximo
  de pratos principais por pedido.
- Os botões "Ajustar seleção", "Tentar novamente", "Usar outro cartão" e "Voltar ao início" do
  protótipo são tratados na `ViewAssinatura` (opções `T`, `O`, `S` e novas seleções); eles não
  são mensagens separadas no diagrama de sequência.
- Não há remoção de itens do pedido (a operação não existe no diagrama de classes).
