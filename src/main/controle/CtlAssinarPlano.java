package minharefeicao.controle;

import java.math.BigDecimal;
import java.util.List;

import minharefeicao.dominio.Assinante;
import minharefeicao.dominio.Assinatura;
import minharefeicao.dominio.Cardapio;
import minharefeicao.dominio.CategoriaItem;
import minharefeicao.dominio.CodigoVerificacao;
import minharefeicao.dominio.ItemCardapio;
import minharefeicao.dominio.ItemPedido;
import minharefeicao.dominio.Pagamento;
import minharefeicao.dominio.Pedido;
import minharefeicao.dominio.Plano;
import minharefeicao.dominio.PreferenciaAlimentar;
import minharefeicao.fronteira.InterfaceOperadoraCartao;
import minharefeicao.fronteira.RespostaOperadora;
import minharefeicao.persistencia.RepositorioMarmitas;
import minharefeicao.servico.Notificador;

/**
 * «control» CtlAssinarPlano: coordena o caso de uso "Assinar Plano de Refeições".
 * Recebe os estímulos da ViewAssinatura, delega as regras às entidades e aos serviços e guarda
 * o contexto da assinatura em andamento (assinante, assinatura, pedido, preferência, plano).
 * Os comentários "Seq. msg N" apontam para a numeração do diagrama de sequência.
 */
public class CtlAssinarPlano {

    private final RepositorioMarmitas repositorio;
    private final Notificador notificador;
    private final InterfaceOperadoraCartao operadora;
    private final Cardapio cardapio;

    private Assinante assinante;
    private Assinatura assinatura;
    private Pedido pedido;
    private PreferenciaAlimentar preferencia;
    private Plano plano;

    public CtlAssinarPlano(RepositorioMarmitas repositorio, Notificador notificador,
                           InterfaceOperadoraCartao operadora) {
        this.repositorio = repositorio;
        this.notificador = notificador;
        this.operadora = operadora;
        this.cardapio = repositorio.carregarCatalogo();
    }

    // ---------------------------------------------------------------- Parte 1 (msgs 2–28)

    /** Seq. msgs 2–7. Retorna false se o celular for inválido (fragmento break). */
    public boolean iniciarAssinatura(String celular) {
        Assinante candidato = repositorio.obterAssinante(celular);
        if (!candidato.validarCelular()) {                       // msg 3
            return false;
        }
        this.assinante = candidato;
        this.assinatura = null;
        this.pedido = null;
        this.preferencia = null;
        this.plano = null;
        enviarNovoCodigo();                                       // msgs 4–7
        persistir();
        return true;
    }

    /** Seq. msgs 9–11. */
    public boolean validarCodigo(String codigo) {
        return assinante != null && assinante.validarCodigo(codigo);
    }

    /** Seq. msgs 12–14. */
    public int tentativasRestantes() {
        return assinante == null ? 0 : assinante.tentativasRestantes();
    }

    /** Seq. msg 15 (ref: repete as msgs 4–7). */
    public void reenviarCodigo() {
        exigirAssinante();
        enviarNovoCodigo();
    }

    /** Seq. msgs 16–17. */
    public List<Plano> listarPlanos() {
        return Plano.listarDisponiveis();
    }

    /** Seq. msgs 19–22. */
    public void selecionarPlano(int idPlano) {
        exigirAssinante();
        Plano escolhido = Plano.buscarPorId(idPlano);              // msg 20
        if (escolhido == null) {
            throw new IllegalArgumentException("Plano inexistente: " + idPlano);
        }
        this.plano = escolhido;
        this.assinatura = assinante.criarAssinatura(escolhido);    // msgs 21–22
        persistir();
    }

    /** Seq. msgs 23–24. */
    public List<PreferenciaAlimentar> listarPreferencias() {
        return PreferenciaAlimentar.listarTodas();
    }

    /** Seq. msgs 26–28. */
    public void registrarPreferencia(int idPreferencia) {
        exigirAssinatura();
        PreferenciaAlimentar escolhida = PreferenciaAlimentar.buscarPorId(idPreferencia); // msg 27
        if (escolhida == null) {
            throw new IllegalArgumentException("Preferência inexistente: " + idPreferencia);
        }
        this.preferencia = escolhida;
        assinatura.definirPreferencia(escolhida);                  // msg 28
        persistir();
    }

    // ---------------------------------------------------------------- Parte 2 (msgs 29–45)

    /** Seq. msgs 29–31. */
    public void iniciarPedido() {
        exigirAssinatura();
        this.pedido = assinatura.criarPedido();                    // msgs 30–31
        persistir();
    }

    /** Seq. msgs 32–33. Itens da categoria que atendem a preferência escolhida. */
    public List<ItemCardapio> listarItens(CategoriaItem categoria) {
        return cardapio.listarItens(categoria, preferencia);
    }

    /**
     * Seq. msgs 35–42. Retorna false se o item não existe, está indisponível para o plano ou
     * se a quantidade ultrapassa o limite do plano (FA2).
     */
    public boolean adicionarItem(int idItem, int qtde) {
        exigirPedido();
        if (qtde <= 0) {
            return false;
        }
        ItemCardapio item = cardapio.buscarItem(idItem);           // msg 36
        if (item == null || !item.estaDisponivelPara(plano)) {     // msg 37
            return false;
        }
        int total = pedido.totalItens(item.getCategoria());        // msg 38
        if (!assinatura.verificarLimite(item.getCategoria(), total + qtde)) { // msgs 39–40
            return false;
        }
        pedido.adicionarItem(item, qtde);                          // msgs 41–42
        persistir();
        return true;
    }

    /** Seq. msgs 44–45. */
    public List<ItemPedido> obterResumo() {
        exigirPedido();
        return pedido.listarItens();
    }

    // ---------------------------------------------------------------- Parte 3 (msgs 46–65)

    /** Seq. msgs 47–49. Lança IllegalArgumentException se o endereço estiver incompleto. */
    public void registrarEndereco(String logradouro, String bairro, String cidade, String cep) {
        exigirAssinatura();
        assinatura.registrarEndereco(logradouro, bairro, cidade, cep);
        persistir();
    }

    /** Seq. msgs 51–65. Retorna true se a operadora autorizou o pagamento. */
    public boolean efetuarPagamento(String numero, String validade, String cvv) {
        exigirPedido();
        Pagamento pagamento = assinatura.criarPagamento();                       // msgs 52–53
        BigDecimal valor = pagamento.getValor();                                 // msg 54
        RespostaOperadora resposta = operadora.solicitarAutorizacao(numero, validade, cvv, valor); // msgs 55–57
        boolean autorizado = resposta.isAutorizado();                            // msg 58
        if (autorizado) {
            pagamento.registrarAutorizacao(resposta.getCodigoAutorizacao());     // msgs 59–60
            assinatura.ativar();                                                 // msg 61
            assinatura.gerarProtocolo();                                         // msg 62
            pedido.aprovar();                                                    // msg 63
        } else {
            pagamento.registrarRecusa();                                         // msg 65
        }
        persistir();
        return autorizado;
    }

    /** Seq. msg 64. Dados para a tela de confirmação: plano, preferência, endereço e protocolo. */
    public Assinatura obterConfirmacao() {
        exigirAssinatura();
        return assinatura;
    }

    // ---------------------------------------------------------------- apoio

    private void enviarNovoCodigo() {
        CodigoVerificacao codigo = notificador.gerarCodigo(assinante.getCelular()); // msgs 4–6
        assinante.registrarCodigo(codigo);                                          // msg 7
    }

    private void persistir() {
        repositorio.salvar(assinante, assinatura);
    }

    private void exigirAssinante() {
        if (assinante == null) {
            throw new IllegalStateException("Nenhuma assinatura iniciada: informe o celular primeiro.");
        }
    }

    private void exigirAssinatura() {
        exigirAssinante();
        if (assinatura == null) {
            throw new IllegalStateException("Nenhum plano selecionado.");
        }
    }

    private void exigirPedido() {
        exigirAssinatura();
        if (pedido == null) {
            throw new IllegalStateException("Pedido não iniciado.");
        }
    }
}
