package minharefeicao.dominio;

/** Categoria de um item do cardápio (tipo enumerado do diagrama de classes). */
public enum CategoriaItem {
    PRATO_PRINCIPAL("Prato principal"),
    ACOMPANHAMENTO("Acompanhamento"),
    SOBREMESA("Sobremesa");

    private final String rotulo;

    CategoriaItem(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
