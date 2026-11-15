package minharefeicao.fronteira;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Fronteira com o ator externo "Operadora de Cartão". Nesta entrega a operadora é simulada:
 * o pagamento é autorizado quando número (16 dígitos), validade (MM/AA futura) e CVV (3 dígitos)
 * são válidos e o número NÃO termina em 0000. Cartões terminados em 0000 são sempre recusados.
 */
public class InterfaceOperadoraCartao {

    private final int atrasoMs;
    private int sequencia = 482100;

    /** @param atrasoMs tempo simulado de resposta da operadora (0 nos testes). */
    public InterfaceOperadoraCartao(int atrasoMs) {
        this.atrasoMs = atrasoMs;
    }

    /** Seq. msgs 55–57: consulta a operadora e cria a RespostaOperadora. */
    public RespostaOperadora solicitarAutorizacao(String numero, String validade, String cvv, BigDecimal valor) {
        esperar();
        String digitos = numero == null ? "" : numero.replaceAll("\\D", "");
        boolean numeroOk = digitos.length() == 16 && !digitos.endsWith("0000");
        boolean cvvOk = cvv != null && cvv.trim().matches("\\d{3}");
        boolean validadeOk = validadeFutura(validade);
        boolean valorOk = valor != null && valor.signum() > 0;
        if (numeroOk && cvvOk && validadeOk && valorOk) {
            sequencia++;
            return new RespostaOperadora(true, "AUT" + sequencia);
        }
        return new RespostaOperadora(false, "");
    }

    private boolean validadeFutura(String validade) {
        if (validade == null || !validade.trim().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return false;
        }
        String[] partes = validade.trim().split("/");
        YearMonth vencimento = YearMonth.of(2000 + Integer.parseInt(partes[1]), Integer.parseInt(partes[0]));
        return !vencimento.isBefore(YearMonth.now());
    }

    private void esperar() {
        if (atrasoMs <= 0) {
            return;
        }
        try {
            Thread.sleep(atrasoMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
