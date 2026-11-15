package minharefeicao.persistencia;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import minharefeicao.dominio.Assinante;
import minharefeicao.dominio.Assinatura;
import minharefeicao.dominio.Cardapio;
import minharefeicao.dominio.CategoriaItem;
import minharefeicao.dominio.Endereco;
import minharefeicao.dominio.ItemCardapio;
import minharefeicao.dominio.ItemPedido;
import minharefeicao.dominio.Pagamento;
import minharefeicao.dominio.Pedido;
import minharefeicao.dominio.Plano;
import minharefeicao.dominio.PreferenciaAlimentar;

/**
 * Persistência em arquivos CSV (pasta "dados"). Esta camada não aparece no diagrama de classes
 * de projeto, que modela só o domínio; ela é chamada pelo controle.
 *
 * Arquivos de catálogo (somente leitura): planos.csv, preferencias.csv, cardapio.csv.
 * Arquivos gravados na execução: assinantes.csv, assinaturas.csv, pedidos.csv,
 * itens_pedido.csv, pagamentos.csv.
 */
public class RepositorioMarmitas {

    private static final String CAB_ASSINANTES = "id;celular";
    private static final String CAB_ASSINATURAS =
            "id;idAssinante;idPlano;idPreferencia;status;protocolo;dataContratacao;logradouro;bairro;cidade;cep";
    private static final String CAB_PEDIDOS = "id;idAssinatura;status";
    private static final String CAB_ITENS = "idPedido;idItem;quantidade";
    private static final String CAB_PAGAMENTOS = "id;idAssinatura;valor;status;codigoAutorizacao;dataHora";

    private final Path pasta;

    public RepositorioMarmitas(Path pasta) {
        this.pasta = pasta;
    }

    public Path getPasta() {
        return pasta;
    }

    /** Carrega planos, preferências e cardápio dos CSVs e registra os catálogos das entidades. */
    public Cardapio carregarCatalogo() {
        List<Plano> planos = new ArrayList<>();
        for (String[] c : ArmazenamentoCsv.ler(pasta.resolve("planos.csv"))) {
            planos.add(new Plano(Integer.parseInt(c[0]), c[1], Integer.parseInt(c[2]), c[3], new BigDecimal(c[4])));
        }
        Plano.definirCatalogo(planos);

        List<PreferenciaAlimentar> preferencias = new ArrayList<>();
        for (String[] c : ArmazenamentoCsv.ler(pasta.resolve("preferencias.csv"))) {
            preferencias.add(new PreferenciaAlimentar(Integer.parseInt(c[0]), c[1]));
        }
        PreferenciaAlimentar.definirCatalogo(preferencias);

        List<ItemCardapio> itens = new ArrayList<>();
        for (String[] c : ArmazenamentoCsv.ler(pasta.resolve("cardapio.csv"))) {
            List<PreferenciaAlimentar> atendidas = new ArrayList<>();
            for (String id : c[4].split("\\|")) {
                PreferenciaAlimentar p = PreferenciaAlimentar.buscarPorId(Integer.parseInt(id.trim()));
                if (p != null) {
                    atendidas.add(p);
                }
            }
            Set<Integer> idsPlanos = new HashSet<>();
            if (!c[5].trim().equals("*")) {
                for (String id : c[5].split("\\|")) {
                    idsPlanos.add(Integer.parseInt(id.trim()));
                }
            }
            itens.add(new ItemCardapio(Integer.parseInt(c[0]), c[1], CategoriaItem.valueOf(c[2]),
                    Integer.parseInt(c[3]), atendidas, idsPlanos));
        }
        LocalDate segunda = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return new Cardapio(segunda, itens);
    }

    /**
     * Devolve o assinante já cadastrado com esse celular ou um novo, ainda não gravado (id 0).
     * O cadastro só é gravado em salvar(), depois que o celular for validado.
     */
    public Assinante obterAssinante(String celular) {
        String digitos = celular == null ? "" : celular.replaceAll("\\D", "");
        for (String[] c : ArmazenamentoCsv.ler(pasta.resolve("assinantes.csv"))) {
            if (c[1].equals(digitos)) {
                return new Assinante(Integer.parseInt(c[0]), digitos);
            }
        }
        return new Assinante(0, digitos);
    }

    /** Grava (insere ou atualiza) o assinante e, se houver, a assinatura com pedidos e pagamentos. */
    public void salvar(Assinante assinante, Assinatura assinatura) {
        Path arqAssinantes = pasta.resolve("assinantes.csv");
        if (assinante.getId() == 0) {
            assinante.definirId(ArmazenamentoCsv.proximoId(ArmazenamentoCsv.ler(arqAssinantes)));
        }
        ArmazenamentoCsv.upsert(arqAssinantes, CAB_ASSINANTES,
                new String[] {String.valueOf(assinante.getId()), assinante.getCelular()});
        if (assinatura == null) {
            return;
        }

        Path arqAssinaturas = pasta.resolve("assinaturas.csv");
        if (assinatura.getId() == 0) {
            assinatura.definirId(ArmazenamentoCsv.proximoId(ArmazenamentoCsv.ler(arqAssinaturas)));
        }
        Endereco e = assinatura.getEndereco();
        PreferenciaAlimentar pref = assinatura.getPreferencia();
        ArmazenamentoCsv.upsert(arqAssinaturas, CAB_ASSINATURAS, new String[] {
                String.valueOf(assinatura.getId()),
                String.valueOf(assinante.getId()),
                String.valueOf(assinatura.getPlano().getId()),
                pref == null ? "" : String.valueOf(pref.getId()),
                assinatura.getStatus().name(),
                assinatura.getProtocolo(),
                assinatura.getDataContratacao().toString(),
                e == null ? "" : ArmazenamentoCsv.limpar(e.getLogradouro()),
                e == null ? "" : ArmazenamentoCsv.limpar(e.getBairro()),
                e == null ? "" : ArmazenamentoCsv.limpar(e.getCidade()),
                e == null ? "" : ArmazenamentoCsv.limpar(e.getCep())});

        for (Pedido pedido : assinatura.getPedidos()) {
            salvarPedido(assinatura, pedido);
        }
        for (Pagamento pagamento : assinatura.getPagamentos()) {
            salvarPagamento(assinatura, pagamento);
        }
    }

    private void salvarPedido(Assinatura assinatura, Pedido pedido) {
        Path arqPedidos = pasta.resolve("pedidos.csv");
        if (pedido.getId() == 0) {
            pedido.definirId(ArmazenamentoCsv.proximoId(ArmazenamentoCsv.ler(arqPedidos)));
        }
        ArmazenamentoCsv.upsert(arqPedidos, CAB_PEDIDOS, new String[] {
                String.valueOf(pedido.getId()), String.valueOf(assinatura.getId()), pedido.getStatus().name()});

        Path arqItens = pasta.resolve("itens_pedido.csv");
        List<String[]> restantes = new ArrayList<>();
        for (String[] linha : ArmazenamentoCsv.ler(arqItens)) {
            if (!linha[0].equals(String.valueOf(pedido.getId()))) {
                restantes.add(linha);
            }
        }
        for (ItemPedido ip : pedido.listarItens()) {
            restantes.add(new String[] {String.valueOf(pedido.getId()),
                    String.valueOf(ip.getItem().getId()), String.valueOf(ip.getQuantidade())});
        }
        ArmazenamentoCsv.escrever(arqItens, CAB_ITENS, restantes);
    }

    private void salvarPagamento(Assinatura assinatura, Pagamento pagamento) {
        Path arq = pasta.resolve("pagamentos.csv");
        if (pagamento.getId() == 0) {
            pagamento.definirId(ArmazenamentoCsv.proximoId(ArmazenamentoCsv.ler(arq)));
        }
        ArmazenamentoCsv.upsert(arq, CAB_PAGAMENTOS, new String[] {
                String.valueOf(pagamento.getId()), String.valueOf(assinatura.getId()),
                pagamento.getValor().toPlainString(), pagamento.getStatus().name(),
                pagamento.getCodigoAutorizacao(), pagamento.getDataHora().toString()});
    }

    /** Apaga os arquivos gravados na execução (o catálogo é mantido). */
    public void zerar() {
        String[] nomes = {"assinantes.csv", "assinaturas.csv", "pedidos.csv", "itens_pedido.csv",
                "pagamentos.csv", "sms_enviados.log"};
        for (String nome : nomes) {
            try {
                Files.deleteIfExists(pasta.resolve(nome));
            } catch (IOException e) {
                throw new UncheckedIOException("Erro ao apagar " + nome, e);
            }
        }
    }
}
