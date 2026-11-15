package minharefeicao.fronteira;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import minharefeicao.controle.CtlAssinarPlano;
import minharefeicao.dominio.Assinatura;
import minharefeicao.dominio.CategoriaItem;
import minharefeicao.dominio.ItemCardapio;
import minharefeicao.dominio.ItemPedido;
import minharefeicao.dominio.Plano;
import minharefeicao.dominio.PreferenciaAlimentar;

/**
 * «boundary» ViewAssinatura: fronteira do ator Assinante, em modo console.
 *
 * Os métodos públicos informarCelular, informarCodigo, reenviarCodigo, confirmarPlano,
 * salvarPreferencia, selecionarItem, confirmarComposicao, informarEndereco e informarCartao são
 * os estímulos do diagrama de classes. Cada um apenas repassa o estímulo ao controle e exibe a
 * resposta, sem regra de negócio. O método executar() e os métodos privados de exibição são
 * mecânica do console (mostrar telas e ler o teclado) e não aparecem no diagrama.
 */
public class ViewAssinatura {

    private final CtlAssinarPlano ctl;
    private final BufferedReader entrada;
    private final PrintStream saida;
    private final boolean eco;

    // Estado de apresentação (qual tela está ativa e o que foi mostrado ao assinante).
    private boolean celularAceito;
    private boolean codigoValidado;
    private boolean bloqueado;
    private boolean planoConfirmado;
    private boolean preferenciaSalva;
    private boolean composicaoConfirmada;
    private boolean enderecoRegistrado;
    private boolean pagamentoAprovado;
    private boolean encerrado;
    private List<Plano> planosExibidos = List.of();
    private List<PreferenciaAlimentar> preferenciasExibidas = List.of();
    private List<ItemCardapio> itensExibidos = List.of();
    private Plano planoEscolhido;
    private PreferenciaAlimentar preferenciaEscolhida;

    /** @param eco repete no console o que foi lido (útil quando a entrada vem de um arquivo). */
    public ViewAssinatura(CtlAssinarPlano ctl, InputStream entrada, PrintStream saida, boolean eco) {
        this.ctl = ctl;
        this.entrada = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        this.saida = saida;
        this.eco = eco;
    }

    // ================================================================ estímulos (diagrama)

    /** Seq. msg 1: o assinante informa o celular. */
    public void informarCelular(String celular) {
        if (ctl.iniciarAssinatura(celular)) {                                   // msg 2
            celularAceito = true;
            saida.println("Código enviado por SMS. Digite-o para continuar.");
        } else {
            saida.println("Celular inválido. Use DDD + número, ex.: (11) 9 8888-1234.");
        }
    }

    /** Seq. msg 8: o assinante informa o código recebido por SMS. */
    public void informarCodigo(String codigo) {
        if (ctl.validarCodigo(codigo)) {                                        // msg 9
            codigoValidado = true;
            saida.println("Código validado ✓");
            planosExibidos = ctl.listarPlanos();                                // msg 16
            exibirPlanos();
        } else {
            int restantes = ctl.tentativasRestantes();                          // msg 12
            if (restantes > 0) {
                saida.println("Código inválido. Você ainda tem " + restantes + " tentativa(s).");
            } else {
                bloqueado = true;
                titulo("Verificação bloqueada");
                saida.println("Limite de tentativas atingido. Você errou o código "
                        + minharefeicao.dominio.CodigoVerificacao.LIMITE_TENTATIVAS
                        + " vezes. Por segurança, solicite um novo código para continuar.");
            }
        }
    }

    /** Seq. msg 15: o assinante pede um novo código (botão "Reenviar código por SMS"). */
    public void reenviarCodigo() {
        ctl.reenviarCodigo();
        bloqueado = false;
        saida.println("Novo código enviado por SMS.");
    }

    /** Seq. msg 18: o assinante confirma o plano escolhido (botão "Confirmar plano"). */
    public void confirmarPlano(int idPlano) {
        Plano escolhido = null;
        for (Plano p : planosExibidos) {
            if (p.getId() == idPlano) {
                escolhido = p;
            }
        }
        if (escolhido == null) {
            saida.println("Plano inválido. Escolha um dos ids listados.");
            return;
        }
        ctl.selecionarPlano(idPlano);                                           // msg 19
        planoEscolhido = escolhido;
        planoConfirmado = true;
        preferenciasExibidas = ctl.listarPreferencias();                        // msg 23
        exibirPreferencias();
    }

    /** Seq. msg 25: o assinante salva a preferência alimentar (botão "Salvar preferência"). */
    public void salvarPreferencia(int idPreferencia) {
        PreferenciaAlimentar escolhida = null;
        for (PreferenciaAlimentar p : preferenciasExibidas) {
            if (p.getId() == idPreferencia) {
                escolhida = p;
            }
        }
        if (escolhida == null) {
            saida.println("Preferência inválida. Escolha um dos ids listados.");
            return;
        }
        ctl.registrarPreferencia(idPreferencia);                                // msg 26
        preferenciaEscolhida = escolhida;
        preferenciaSalva = true;
        saida.println("Preferência salva: " + escolhida.getNome());
        ctl.iniciarPedido();                                                    // msg 29
    }

    /** Seq. msg 34: o assinante seleciona um item do cardápio e a quantidade. */
    public void selecionarItem(int idItem, int qtde) {
        ItemCardapio item = null;
        for (ItemCardapio i : itensExibidos) {
            if (i.getId() == idItem) {
                item = i;
            }
        }
        if (item == null) {
            saida.println("Item inválido. Escolha um dos ids listados nesta tela.");
            return;
        }
        if (qtde <= 0) {
            saida.println("Quantidade inválida. Informe um número maior que zero.");
            return;
        }
        if (ctl.adicionarItem(idItem, qtde)) {                                  // msg 35
            saida.println("Item adicionado ao pedido: " + item.getNome() + " x" + qtde);
        } else if (!item.estaDisponivelPara(planoEscolhido)) {
            saida.println("Indisponível para este plano: " + item.getNome());
        } else {
            saida.println("Seu plano permite no máximo " + planoEscolhido.getQtdRefeicoes()
                    + " pratos principais. Ajuste a seleção.");
        }
    }

    /** Seq. msg 43: o assinante confirma a composição (botão "Confirmar composição"). */
    public void confirmarComposicao() {
        List<ItemPedido> resumo = ctl.obterResumo();                            // msg 44
        titulo("Tela 8 — Resumo do pedido");
        if (resumo.isEmpty()) {
            saida.println("Nenhum item selecionado. Escolha ao menos um item.");
            return;
        }
        for (CategoriaItem categoria : CategoriaItem.values()) {
            boolean cabecalho = false;
            for (ItemPedido ip : resumo) {
                if (ip.getItem().getCategoria() == categoria) {
                    if (!cabecalho) {
                        saida.println(categoria.getRotulo() + ":");
                        cabecalho = true;
                    }
                    saida.println("  " + ip.getItem().getNome() + "  " + ip.getQuantidade() + "x");
                }
            }
        }
        composicaoConfirmada = true;
    }

    /** Seq. msg 46: o assinante informa o endereço de entrega. */
    public void informarEndereco(String logradouro, String bairro, String cidade, String cep) {
        try {
            ctl.registrarEndereco(logradouro, bairro, cidade, cep);             // msg 47
            enderecoRegistrado = true;
            saida.println("Endereço registrado. Status da assinatura: aguardando pagamento.");
        } catch (IllegalArgumentException e) {
            saida.println("Endereço incompleto. Preencha todos os campos e um CEP com 8 dígitos.");
        }
    }

    /** Seq. msg 50: o assinante informa os dados do cartão e paga. */
    public void informarCartao(String numero, String validade, String cvv) {
        titulo("Tela 11 — Processando pagamento");
        saida.println("Solicitando autorização à operadora de cartão de crédito...");
        if (ctl.efetuarPagamento(numero, validade, cvv)) {                      // msg 51
            pagamentoAprovado = true;
            Assinatura assinatura = ctl.obterConfirmacao();                     // msg 64
            titulo("Tela 12 — Assinatura confirmada");
            saida.println("Assinatura ativa");
            saida.println("Plano " + assinatura.getPlano().getNome()
                    + " (" + assinatura.getPlano().getQtdRefeicoes() + ")");
            saida.println("Preferência: " + assinatura.getPreferencia().getNome());
            saida.println("Entrega: " + assinatura.getEndereco().getLogradouro());
            saida.println("Protocolo " + assinatura.getProtocolo());
        } else {
            titulo("Pagamento não autorizado");
            saida.println("Status: aguardando pagamento");
            saida.println("A operadora de cartão de crédito não autorizou o pagamento.");
        }
    }

    // ================================================================ mecânica do console

    /** Conduz o assinante pelas telas do cenário, na ordem do protótipo. */
    public void executar() {
        try {
            fluxo();
        } catch (EntradaEncerrada e) {
            saida.println();
            saida.println("Entrada encerrada. Fim da execução.");
        }
    }

    private void fluxo() {
        titulo("Tela 1 — Entrar");
        saida.println("Informe seu número de celular para começar sua assinatura.");
        while (!celularAceito) {
            informarCelular(ler("Celular (ex.: (11) 9 8888-1234)"));
        }

        titulo("Tela 2 — Verificação");
        while (!codigoValidado) {
            if (bloqueado) {
                String opcao = ler("Digite R para reenviar o código por SMS");
                if (opcao.equalsIgnoreCase("R")) {
                    reenviarCodigo();
                }
            } else {
                informarCodigo(ler("Código de 4 dígitos"));
            }
        }

        while (!planoConfirmado) {
            confirmarPlano(lerInteiro("Escolha o plano (id)"));
        }
        while (!preferenciaSalva) {
            salvarPreferencia(lerInteiro("Escolha a preferência (id)"));
        }

        while (!composicaoConfirmada) {
            for (CategoriaItem categoria : CategoriaItem.values()) {
                exibirItens(categoria);
                while (true) {
                    String linha = ler("Item e quantidade (ex.: 1 2). ENTER para seguir");
                    if (linha.isBlank()) {
                        break;
                    }
                    int[] par = lerParItemQuantidade(linha);
                    if (par == null) {
                        saida.println("Formato inválido. Digite o id do item e a quantidade, ex.: 1 2.");
                    } else {
                        selecionarItem(par[0], par[1]);
                    }
                }
            }
            ler("Pressione ENTER para ver o resumo e confirmar a composição");
            confirmarComposicao();
        }

        titulo("Tela 9 — Endereço de entrega");
        while (!enderecoRegistrado) {
            informarEndereco(ler("Rua e número"), ler("Bairro"), ler("Cidade"), ler("CEP"));
        }

        titulo("Tela 10 — Pagamento");
        saida.println("Status: aguardando pagamento");
        saida.println("Total da assinatura: " + moeda(planoEscolhido.getValor()));
        String[] cartao = null;
        while (!pagamentoAprovado && !encerrado) {
            if (cartao == null) {
                cartao = new String[] {ler("Número do cartão"), ler("Validade (MM/AA)"), ler("CVV")};
            }
            informarCartao(cartao[0], cartao[1], cartao[2]);
            if (!pagamentoAprovado) {
                String opcao = ler("[T] Tentar novamente  [O] Usar outro cartão  [S] Encerrar");
                if (opcao.equalsIgnoreCase("O")) {
                    cartao = null;
                } else if (opcao.equalsIgnoreCase("S")) {
                    encerrar();
                }
            }
        }
    }

    private void encerrar() {
        encerrado = true;
        titulo("Assinatura não concluída");
        saida.println("Processo encerrado");
        saida.println("Você optou por não tentar novamente. Sua assinatura não foi ativada "
                + "e nenhum valor foi cobrado.");
    }

    private void exibirPlanos() {
        titulo("Tela 3 — Planos de assinatura");
        for (Plano p : planosExibidos) {
            saida.println("  [" + p.getId() + "] " + p.getNome() + " — " + p.getQtdRefeicoes()
                    + " refeições, " + p.getPeriodicidade() + " — " + moeda(p.getValor()));
        }
    }

    private void exibirPreferencias() {
        titulo("Tela 4 — Preferências alimentares");
        saida.println("Selecione a preferência que deseja seguir no seu cardápio.");
        for (PreferenciaAlimentar p : preferenciasExibidas) {
            saida.println("  [" + p.getId() + "] " + p.getNome());
        }
    }

    private void exibirItens(CategoriaItem categoria) {
        itensExibidos = ctl.listarItens(categoria);                             // msg 32
        String tela;
        switch (categoria) {
            case PRATO_PRINCIPAL:
                tela = "Tela 5 — Pratos principais";
                break;
            case ACOMPANHAMENTO:
                tela = "Tela 6 — Acompanhamentos";
                break;
            default:
                tela = "Tela 7 — Sobremesas";
        }
        titulo(tela);
        saida.println("Cardápio da semana · " + preferenciaEscolhida.getNome());
        for (ItemCardapio item : itensExibidos) {
            String aviso = item.estaDisponivelPara(planoEscolhido) ? "" : "  (indisponível para este plano)";
            saida.println("  [" + item.getId() + "] " + item.getNome() + " — " + item.getCalorias() + " kcal" + aviso);
        }
    }

    private void titulo(String texto) {
        saida.println();
        saida.println("=== " + texto + " ===");
    }

    private String moeda(BigDecimal valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %,.2f", valor);
    }

    private String ler(String pergunta) {
        saida.print("> " + pergunta + ": ");
        saida.flush();
        String linha;
        try {
            linha = entrada.readLine();
        } catch (IOException e) {
            throw new EntradaEncerrada();
        }
        if (linha == null) {
            throw new EntradaEncerrada();
        }
        if (eco) {
            saida.println(linha);
        }
        return linha.trim();
    }

    private int lerInteiro(String pergunta) {
        while (true) {
            String texto = ler(pergunta);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                saida.println("Digite apenas o número do id.");
            }
        }
    }

    private int[] lerParItemQuantidade(String linha) {
        String[] partes = linha.trim().split("\\s+");
        try {
            if (partes.length == 1) {
                return new int[] {Integer.parseInt(partes[0]), 1};
            }
            if (partes.length == 2) {
                return new int[] {Integer.parseInt(partes[0]), Integer.parseInt(partes[1])};
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    /** Sinaliza que a entrada acabou (fim do arquivo de demonstração ou Ctrl+D). */
    private static class EntradaEncerrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
