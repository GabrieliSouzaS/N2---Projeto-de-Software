package minharefeicao.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Entidade Pedido (composição de 0..* ItemPedido). */
public class Pedido {

    private int id;
    private StatusPedido status = StatusPedido.EM_COMPOSICAO;
    private final List<ItemPedido> itens = new ArrayList<>();

    /** Seq. msg 41. Se o item já está no pedido soma a quantidade; senão cria um ItemPedido. */
    public void adicionarItem(ItemCardapio item, int qtde) {
        for (ItemPedido existente : itens) {
            if (existente.getItem().getId() == item.getId()) {
                existente.alterarQuantidade(existente.getQuantidade() + qtde);
                return;
            }
        }
        itens.add(new ItemPedido(item, qtde));
    }

    /** Seq. msg 38. Soma das quantidades dos itens da categoria. */
    public int totalItens(CategoriaItem categoria) {
        int total = 0;
        for (ItemPedido ip : itens) {
            if (ip.getItem().getCategoria() == categoria) {
                total += ip.getQuantidade();
            }
        }
        return total;
    }

    /** Seq. msg 45. */
    public List<ItemPedido> listarItens() {
        return Collections.unmodifiableList(itens);
    }

    /** Seq. msg 63. */
    public void aprovar() {
        this.status = StatusPedido.APROVADO;
    }

    public int getId() {
        return id;
    }

    public void definirId(int id) {
        this.id = id;
    }

    public StatusPedido getStatus() {
        return status;
    }
}
