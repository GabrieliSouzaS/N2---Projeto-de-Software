package minharefeicao.dominio;

/** Entidade Endereco (composição de Assinatura): endereço de entrega. */
public class Endereco {

    private final String logradouro;
    private final String bairro;
    private final String cidade;
    private final String cep;

    public Endereco(String logradouro, String bairro, String cidade, String cep) {
        this.logradouro = logradouro == null ? "" : logradouro.trim();
        this.bairro = bairro == null ? "" : bairro.trim();
        this.cidade = cidade == null ? "" : cidade.trim();
        this.cep = cep == null ? "" : cep.trim();
    }

    /** Todos os campos preenchidos e CEP com 8 dígitos. */
    public boolean estaCompleto() {
        return !logradouro.isEmpty() && !bairro.isEmpty() && !cidade.isEmpty()
                && cep.replaceAll("\\D", "").length() == 8;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getCep() {
        return cep;
    }

    @Override
    public String toString() {
        return logradouro + ", " + bairro + ", " + cidade + " - CEP " + cep;
    }
}
