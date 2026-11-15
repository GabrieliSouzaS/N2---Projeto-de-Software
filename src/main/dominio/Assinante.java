package minharefeicao.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Entidade Assinante: o cliente (cadastro). Não confundir com o ator Assinante, que é a pessoa. */
public class Assinante {

    private int id;
    private final String celular;
    private CodigoVerificacao codigoAtual;
    private final List<Assinatura> assinaturas = new ArrayList<>();

    public Assinante(int id, String celular) {
        this.id = id;
        this.celular = celular == null ? "" : celular.trim();
    }

    /** Seq. msg 3. Aceita 11 dígitos: DDD (11 a 99) + 9 + 8 dígitos. */
    public boolean validarCelular() {
        String digitos = celular.replaceAll("\\D", "");
        return digitos.matches("[1-9][1-9]9\\d{8}");
    }

    /** Seq. msg 7. Guarda o código gerado pelo Notificador como código atual. */
    public void registrarCodigo(CodigoVerificacao codigo) {
        this.codigoAtual = codigo;
    }

    /** Seq. msg 10. */
    public boolean validarCodigo(String codigo) {
        return codigoAtual != null && codigoAtual.validar(codigo);
    }

    /** Seq. msg 13. */
    public int tentativasRestantes() {
        return codigoAtual == null ? 0 : codigoAtual.tentativasRestantes();
    }

    /** Seq. msg 21 («create» Assinatura na msg 22). */
    public Assinatura criarAssinatura(Plano plano) {
        Assinatura assinatura = new Assinatura(plano);
        assinaturas.add(assinatura);
        return assinatura;
    }

    public String getCelular() {
        return celular;
    }

    /** Ex.: (11) 9 8888-1234. */
    public String getCelularFormatado() {
        String d = celular.replaceAll("\\D", "");
        if (d.length() != 11) {
            return celular;
        }
        return "(" + d.substring(0, 2) + ") " + d.charAt(2) + " " + d.substring(3, 7) + "-" + d.substring(7);
    }

    public int getId() {
        return id;
    }

    public void definirId(int id) {
        this.id = id;
    }

    public List<Assinatura> getAssinaturas() {
        return Collections.unmodifiableList(assinaturas);
    }
}
