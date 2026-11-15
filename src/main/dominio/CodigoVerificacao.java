package minharefeicao.dominio;

import java.time.LocalDateTime;

/** Entidade CodigoVerificacao: código de 4 dígitos enviado por SMS, com limite de tentativas. */
public class CodigoVerificacao {

    public static final int LIMITE_TENTATIVAS = 3;

    private final String valor;
    private final LocalDateTime geradoEm;
    private int tentativas;

    public CodigoVerificacao(String valor) {
        this.valor = valor;
        this.geradoEm = LocalDateTime.now();
        this.tentativas = 0;
    }

    /** Seq. msg 11. Cada código errado consome uma tentativa; bloqueado, sempre recusa. */
    public boolean validar(String codigo) {
        if (estaBloqueado()) {
            return false;
        }
        String informado = codigo == null ? "" : codigo.trim();
        if (valor.equals(informado)) {
            return true;
        }
        tentativas++;
        return false;
    }

    /** Seq. msg 14. */
    public int tentativasRestantes() {
        return Math.max(0, LIMITE_TENTATIVAS - tentativas);
    }

    public boolean estaBloqueado() {
        return tentativas >= LIMITE_TENTATIVAS;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }
}
