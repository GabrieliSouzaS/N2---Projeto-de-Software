package minharefeicao;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import minharefeicao.controle.CtlAssinarPlano;
import minharefeicao.fronteira.InterfaceOperadoraCartao;
import minharefeicao.fronteira.ViewAssinatura;
import minharefeicao.persistencia.RepositorioMarmitas;
import minharefeicao.servico.Notificador;

/**
 * Ponto de entrada: monta os objetos (fronteira, controle, serviço, persistência) e inicia o
 * cenário "Assinar Plano de Refeições".
 *
 * Opções: --dados=PASTA (padrão: dados), --codigo-fixo=NNNN (SMS sempre com esse código, para
 * demonstrações), --eco (repete a entrada lida), --sem-atraso (operadora responde na hora) e
 * --zerar (apaga os dados gravados em execuções anteriores).
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Path pasta = Paths.get("dados");
        String codigoFixo = null;
        boolean eco = false;
        boolean zerar = false;
        int atrasoMs = 800;
        for (String arg : args) {
            if (arg.startsWith("--dados=")) {
                pasta = Paths.get(arg.substring("--dados=".length()));
            } else if (arg.startsWith("--codigo-fixo=")) {
                codigoFixo = arg.substring("--codigo-fixo=".length());
            } else if (arg.equals("--eco")) {
                eco = true;
            } else if (arg.equals("--sem-atraso")) {
                atrasoMs = 0;
            } else if (arg.equals("--zerar")) {
                zerar = true;
            } else {
                System.err.println("Opção desconhecida: " + arg);
                return;
            }
        }

        PrintStream saida = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        RepositorioMarmitas repositorio = new RepositorioMarmitas(pasta);
        if (zerar) {
            repositorio.zerar();
            saida.println("Dados de execuções anteriores apagados.");
        }

        final String fixo = codigoFixo;
        Supplier<String> gerador = fixo != null ? () -> fixo : Notificador::codigoAleatorio;
        Notificador notificador = new Notificador(gerador, saida, pasta.resolve("sms_enviados.log"));
        InterfaceOperadoraCartao operadora = new InterfaceOperadoraCartao(atrasoMs);
        CtlAssinarPlano controle = new CtlAssinarPlano(repositorio, notificador, operadora);

        saida.println("MINHA REFEIÇÃO — Assinar Plano de Refeições");
        new ViewAssinatura(controle, System.in, saida, eco).executar();
    }
}
