package minharefeicao.fronteira;

/** Tipo de dados «datatype»: resultado devolvido pela operadora de cartão. */
public class RespostaOperadora {

    private final boolean autorizado;
    private final String codigoAutorizacao;

    public RespostaOperadora(boolean autorizado, String codigoAutorizacao) {
        this.autorizado = autorizado;
        this.codigoAutorizacao = codigoAutorizacao;
    }

    /** Seq. msg 58. */
    public boolean isAutorizado() {
        return autorizado;
    }

    /** Seq. msg 59. */
    public String getCodigoAutorizacao() {
        return codigoAutorizacao;
    }
}
