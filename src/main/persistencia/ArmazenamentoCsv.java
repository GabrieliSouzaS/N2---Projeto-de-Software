package minharefeicao.persistencia;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Utilitário de leitura e gravação de arquivos CSV (separador ";", UTF-8, 1ª linha = cabeçalho). */
final class ArmazenamentoCsv {

    private ArmazenamentoCsv() {
    }

    /** Lê as linhas de dados (sem o cabeçalho). Arquivo inexistente equivale a lista vazia. */
    static List<String[]> ler(Path arquivo) {
        List<String[]> linhas = new ArrayList<>();
        if (!Files.exists(arquivo)) {
            return linhas;
        }
        try {
            List<String> todas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
            for (int i = 1; i < todas.size(); i++) {
                if (!todas.get(i).isBlank()) {
                    linhas.add(todas.get(i).split(";", -1));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao ler " + arquivo, e);
        }
        return linhas;
    }

    /** Regrava o arquivo inteiro com o cabeçalho e as linhas informadas. */
    static void escrever(Path arquivo, String cabecalho, List<String[]> linhas) {
        List<String> saida = new ArrayList<>();
        saida.add(cabecalho);
        for (String[] l : linhas) {
            saida.add(String.join(";", l));
        }
        try {
            if (arquivo.getParent() != null) {
                Files.createDirectories(arquivo.getParent());
            }
            Files.write(arquivo, saida, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao gravar " + arquivo, e);
        }
    }

    /** Insere a linha ou substitui a existente com o mesmo id (primeira coluna). */
    static void upsert(Path arquivo, String cabecalho, String[] nova) {
        List<String[]> linhas = ler(arquivo);
        boolean substituida = false;
        for (int i = 0; i < linhas.size(); i++) {
            if (linhas.get(i)[0].equals(nova[0])) {
                linhas.set(i, nova);
                substituida = true;
                break;
            }
        }
        if (!substituida) {
            linhas.add(nova);
        }
        escrever(arquivo, cabecalho, linhas);
    }

    static int proximoId(List<String[]> linhas) {
        int maior = 0;
        for (String[] l : linhas) {
            maior = Math.max(maior, Integer.parseInt(l[0]));
        }
        return maior + 1;
    }

    /** Remove separadores e quebras de linha de um texto livre antes de gravar. */
    static String limpar(String texto) {
        return texto == null ? "" : texto.replace(';', ',').replace('\n', ' ').replace('\r', ' ');
    }
}
