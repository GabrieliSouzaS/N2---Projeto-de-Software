package minharefeicao.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidade PreferenciaAlimentar. Os métodos estáticos correspondem à lifeline
 * «Collection» PreferenciaAlimentar do diagrama de sequência.
 */
public class PreferenciaAlimentar {

    private static List<PreferenciaAlimentar> catalogo = new ArrayList<>();

    private final int id;
    private final String nome;

    public PreferenciaAlimentar(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public static void definirCatalogo(List<PreferenciaAlimentar> preferencias) {
        catalogo = new ArrayList<>(preferencias);
    }

    /** Seq. msg 24. */
    public static List<PreferenciaAlimentar> listarTodas() {
        return Collections.unmodifiableList(catalogo);
    }

    /** Seq. msg 27. Retorna null se não existir. */
    public static PreferenciaAlimentar buscarPorId(int idPreferencia) {
        for (PreferenciaAlimentar p : catalogo) {
            if (p.id == idPreferencia) {
                return p;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PreferenciaAlimentar && ((PreferenciaAlimentar) o).id == id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
