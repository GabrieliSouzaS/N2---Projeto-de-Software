package minharefeicao.dominio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidade Plano. Os métodos estáticos listarDisponiveis() e buscarPorId() correspondem à
 * lifeline «Collection» Plano do diagrama de sequência. O catálogo é carregado pela camada
 * de persistência na inicialização.
 */
public class Plano {

    private static List<Plano> catalogo = new ArrayList<>();

    private final int id;
    private final String nome;
    private final int qtdRefeicoes;
    private final String periodicidade;
    private final BigDecimal valor;

    public Plano(int id, String nome, int qtdRefeicoes, String periodicidade, BigDecimal valor) {
        this.id = id;
        this.nome = nome;
        this.qtdRefeicoes = qtdRefeicoes;
        this.periodicidade = periodicidade;
        this.valor = valor;
    }

    public static void definirCatalogo(List<Plano> planos) {
        catalogo = new ArrayList<>(planos);
    }

    /** Seq. msg 17. */
    public static List<Plano> listarDisponiveis() {
        return Collections.unmodifiableList(catalogo);
    }

    /** Seq. msg 20. Retorna null se não existir. */
    public static Plano buscarPorId(int idPlano) {
        for (Plano p : catalogo) {
            if (p.id == idPlano) {
                return p;
            }
        }
        return null;
    }

    /**
     * Seq. msg 40. O limite vale para PRATO_PRINCIPAL: o total de pratos principais do pedido
     * não pode passar de qtdRefeicoes.
     */
    public boolean permiteQuantidade(CategoriaItem categoria, int qtd) {
        if (categoria == CategoriaItem.PRATO_PRINCIPAL) {
            return qtd <= qtdRefeicoes;
        }
        return true;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getQtdRefeicoes() {
        return qtdRefeicoes;
    }

    public String getPeriodicidade() {
        return periodicidade;
    }
}
