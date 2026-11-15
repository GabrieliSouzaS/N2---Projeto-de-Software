package minharefeicao.dominio;

/** Entidade ItemPedido (composição de Pedido): um item do cardápio e a quantidade pedida. */
public class ItemPedido {

    private final ItemCardapio item;
    private int quantidade;

    /** Seq. msg 42 («create»). */
    public ItemPedido(ItemCardapio item, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        this.item = item;
        this.quantidade = quantidade;
    }

    public void alterarQuantidade(int qtde) {
        if (qtde <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        this.quantidade = qtde;
    }

    public ItemCardapio getItem() {
        return item;
    }

    public int getQuantidade() {
        return quantidade;
    }
}
