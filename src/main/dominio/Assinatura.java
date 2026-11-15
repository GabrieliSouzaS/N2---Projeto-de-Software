package minharefeicao.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidade Assinatura. Associa-se a um Plano e a uma PreferenciaAlimentar, compõe o Endereco de
 * entrega e agrupa Pedidos e Pagamentos.
 */
public class Assinatura {

    private int id;
    private String protocolo = "";
    private StatusAssinatura status = StatusAssinatura.EM_CONTRATACAO;
    private final LocalDate dataContratacao = LocalDate.now();
    private final Plano plano;
    private PreferenciaAlimentar preferencia;
    private Endereco endereco;
    private final List<Pedido> pedidos = new ArrayList<>();
    private final List<Pagamento> pagamentos = new ArrayList<>();

    /** Seq. msg 22 («create»), disparada por Assinante.criarAssinatura(plano). */
    public Assinatura(Plano plano) {
        this.plano = plano;
    }

    /** Seq. msg 28. */
    public void definirPreferencia(PreferenciaAlimentar preferencia) {
        this.preferencia = preferencia;
    }

    /**
     * Seq. msgs 48–49. Cria o Endereco (composição) e move a assinatura para AGUARDANDO_PAGAMENTO.
     * Lança IllegalArgumentException se o endereço estiver incompleto.
     */
    public void registrarEndereco(String logradouro, String bairro, String cidade, String cep) {
        Endereco novo = new Endereco(logradouro, bairro, cidade, cep);
        if (!novo.estaCompleto()) {
            throw new IllegalArgumentException("Endereço incompleto.");
        }
        this.endereco = novo;
        this.status = StatusAssinatura.AGUARDANDO_PAGAMENTO;
    }

    /** Seq. msg 30 («create» Pedido na msg 31). */
    public Pedido criarPedido() {
        Pedido pedido = new Pedido();
        pedidos.add(pedido);
        return pedido;
    }

    /** Seq. msg 39. Delega a regra de limite ao Plano (msg 40). */
    public boolean verificarLimite(CategoriaItem categoria, int qtdTotal) {
        return plano.permiteQuantidade(categoria, qtdTotal);
    }

    /** Seq. msg 52 («create» Pagamento na msg 53). O valor é o do plano. */
    public Pagamento criarPagamento() {
        Pagamento pagamento = new Pagamento(plano.getValor());
        pagamentos.add(pagamento);
        return pagamento;
    }

    /** Seq. msg 61. */
    public void ativar() {
        this.status = StatusAssinatura.ATIVA;
    }

    /** Seq. msg 62. Protocolo no formato #10245 (10000 + id da assinatura). */
    public String gerarProtocolo() {
        this.protocolo = "#" + (10000 + id);
        return protocolo;
    }

    public int getId() {
        return id;
    }

    public void definirId(int id) {
        this.id = id;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public StatusAssinatura getStatus() {
        return status;
    }

    public LocalDate getDataContratacao() {
        return dataContratacao;
    }

    public Plano getPlano() {
        return plano;
    }

    public PreferenciaAlimentar getPreferencia() {
        return preferencia;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public List<Pedido> getPedidos() {
        return Collections.unmodifiableList(pedidos);
    }

    public List<Pagamento> getPagamentos() {
        return Collections.unmodifiableList(pagamentos);
    }
}
