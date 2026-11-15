package minharefeicao.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Entidade Pagamento: uma tentativa de pagamento da assinatura. */
public class Pagamento {

    private int id;
    private final BigDecimal valor;
    private StatusPagamento status = StatusPagamento.PENDENTE;
    private String codigoAutorizacao = "";
    private final LocalDateTime dataHora = LocalDateTime.now();

    /** Seq. msg 53 («create»). */
    public Pagamento(BigDecimal valor) {
        this.valor = valor;
    }

    /** Seq. msg 60. */
    public void registrarAutorizacao(String codigo) {
        this.codigoAutorizacao = codigo;
        this.status = StatusPagamento.AUTORIZADO;
    }

    /** Seq. msg 65. */
    public void registrarRecusa() {
        this.status = StatusPagamento.NAO_AUTORIZADO;
    }

    /** Seq. msg 54. */
    public BigDecimal getValor() {
        return valor;
    }

    public int getId() {
        return id;
    }

    public void definirId(int id) {
        this.id = id;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public String getCodigoAutorizacao() {
        return codigoAutorizacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
