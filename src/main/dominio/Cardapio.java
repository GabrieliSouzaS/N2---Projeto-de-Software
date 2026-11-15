package minharefeicao.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Entidade Cardapio (agrega 1..* ItemCardapio), análoga ao Catálogo do material de apoio. */
public class Cardapio {

    private final LocalDate semanaReferencia;
    private final List<ItemCardapio> itens;

    public Cardapio(LocalDate semanaReferencia, List<ItemCardapio> itens) {
        this.semanaReferencia = semanaReferencia;
        this.itens = new ArrayList<>(itens);
    }

    /** Seq. msg 33. Itens da categoria que atendem a preferência alimentar. */
    public List<ItemCardapio> listarItens(CategoriaItem categoria, PreferenciaAlimentar preferencia) {
        List<ItemCardapio> resultado = new ArrayList<>();
        for (ItemCardapio item : itens) {
            if (item.getCategoria() == categoria && item.getPreferencias().contains(preferencia)) {
                resultado.add(item);
            }
        }
        return resultado;
    }

    /** Seq. msg 36. Retorna null se o item não existir. */
    public ItemCardapio buscarItem(int idItem) {
        for (ItemCardapio item : itens) {
            if (item.getId() == idItem) {
                return item;
            }
        }
        return null;
    }

    public LocalDate getSemanaReferencia() {
        return semanaReferencia;
    }
}
