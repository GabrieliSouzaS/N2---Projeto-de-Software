package minharefeicao.dominio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Entidade ItemCardapio (prato, acompanhamento ou sobremesa). */
public class ItemCardapio {

    private final int id;
    private final String nome;
    private final CategoriaItem categoria;
    private final int calorias;
    /** Associação "atende" (1..*): preferências alimentares que o item atende. */
    private final List<PreferenciaAlimentar> preferencias;
    /** Planos em que o item é oferecido. Vazio significa todos os planos. */
    private final Set<Integer> idsPlanos;

    public ItemCardapio(int id, String nome, CategoriaItem categoria, int calorias,
                        List<PreferenciaAlimentar> preferencias, Set<Integer> idsPlanos) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.calorias = calorias;
        this.preferencias = new ArrayList<>(preferencias);
        this.idsPlanos = new HashSet<>(idsPlanos);
    }

    /** Seq. msg 37. */
    public boolean estaDisponivelPara(Plano plano) {
        return idsPlanos.isEmpty() || idsPlanos.contains(plano.getId());
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public CategoriaItem getCategoria() {
        return categoria;
    }

    public int getCalorias() {
        return calorias;
    }

    public List<PreferenciaAlimentar> getPreferencias() {
        return preferencias;
    }
}
