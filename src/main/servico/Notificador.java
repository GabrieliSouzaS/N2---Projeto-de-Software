package minharefeicao.servico;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import minharefeicao.dominio.CodigoVerificacao;

/**
 * «service» Notificador: gera o código de verificação, cria o CodigoVerificacao e envia o SMS.
 * O envio é simulado: a mensagem aparece no console e é gravada em dados/sms_enviados.log.
 */
public class Notificador {

    private final Supplier<String> geradorDeCodigo;
    private final PrintStream saida;
    private final Path logSms;

    public Notificador(Supplier<String> geradorDeCodigo, PrintStream saida, Path logSms) {
        this.geradorDeCodigo = geradorDeCodigo;
        this.saida = saida;
        this.logSms = logSms;
    }

    /** Código de 4 dígitos aleatório (comportamento padrão). */
    public static String codigoAleatorio() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    /** Seq. msgs 4–6: gera o código, cria o CodigoVerificacao e envia o SMS ao assinante. */
    public CodigoVerificacao gerarCodigo(String celular) {
        String valor = geradorDeCodigo.get();
        CodigoVerificacao codigo = new CodigoVerificacao(valor);
        enviarSms(celular, "Minha Refeição: seu código de verificação é " + valor);
        return codigo;
    }

    private void enviarSms(String celular, String texto) {
        saida.println("[SMS para " + celular + "] " + texto);
        try {
            if (logSms.getParent() != null) {
                Files.createDirectories(logSms.getParent());
            }
            String linha = LocalDateTime.now() + ";" + celular + ";" + texto + System.lineSeparator();
            Files.writeString(logSms, linha, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            saida.println("(aviso: não foi possível gravar o log de SMS: " + e.getMessage() + ")");
        }
    }
}
