package br.com.conde.empacotamento;

import java.util.Locale;
import java.util.Map;

/**
 * As tabelas do README. Uso:
 * {@code mvn exec:java -Dexec.args="heuristicas"} (ou {@code pior}, {@code ordem}).
 */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String tabela = args.length > 0 ? args[0] : "tudo";
        boolean tudo = tabela.equals("tudo");
        if (tudo || tabela.equals("heuristicas")) {
            heuristicas();
        }
        if (tudo || tabela.equals("pior")) {
            pior();
        }
        if (tudo || tabela.equals("ordem")) {
            ordem();
        }
    }

    /** As seis heuristicas contra o otimo, em todas as sequencias. */
    public static void heuristicas() {
        System.out.println();
        System.out.println("HEURISTICAS  (7 itens de 1 a 7, caixas de 7: 823.543 sequencias)");
        Varredura varredura = new Varredura(7, 7);
        Map<Heuristica, Varredura.Placar> placares = varredura.medir();
        System.out.printf(Locale.ROOT, "%-20s %10s %12s %12s %10s%n",
                "heuristica", "acerta", "desperdicio", "pior razao", "ve tudo?");
        for (Heuristica heuristica : Heuristica.values()) {
            Varredura.Placar placar = placares.get(heuristica);
            System.out.printf(Locale.ROOT, "%-20s %9.2f%% %11.4f %12.3f %10s%n",
                    heuristica.rotulo(), 100 * placar.taxaDeAcerto(),
                    placar.desperdicio(), placar.piorRazao(),
                    heuristica.ordena() ? "sim" : "nao");
        }
        System.out.println();
        System.out.println("conjuntos distintos resolvidos pela busca exata: "
                + varredura.otimo().guardados());
        System.out.println();
    }

    /** A instancia que mais derruba cada heuristica. */
    public static void pior() {
        System.out.println();
        System.out.println("PIOR CASO  (a sequencia que mais afasta cada heuristica do otimo)");
        Varredura varredura = new Varredura(7, 7);
        Map<Heuristica, Varredura.Placar> placares = varredura.medir();
        for (Heuristica heuristica : Heuristica.values()) {
            Varredura.Placar placar = placares.get(heuristica);
            Instancia pior = placar.pior();
            System.out.printf(Locale.ROOT, "  %-20s razao %.3f  %s%n",
                    heuristica.rotulo(), placar.piorRazao(),
                    pior == null ? "nunca erra" : pior.toString());
        }
        System.out.println();
        System.out.println("a menor instancia em que ordenar do maior para o menor NAO basta:");
        System.out.println("  " + Instancia.de(8, 4, 3, 3, 2, 2, 2));
        System.out.println("  ordenada gasta "
                + Heuristica.PRIMEIRA_ORDENADA.empacotar(Instancia.de(8, 4, 3, 3, 2, 2, 2))
                + " caixas; o otimo gasta "
                + new Otimo().caixas(Instancia.de(8, 4, 3, 3, 2, 2, 2))
                + " (4+2+2 e 3+3+2)");
        System.out.println();
        System.out.println("por tamanho de caixa, a taxa de acerto da ordenada com 6 itens:");
        for (int capacidade = 4; capacidade <= 8; capacidade++) {
            Varredura v = new Varredura(6, capacidade);
            Varredura.Placar placar = v.medir().get(Heuristica.PRIMEIRA_ORDENADA);
            System.out.printf(Locale.ROOT, "  caixa de %d: %.2f%%%n",
                    capacidade, 100 * placar.taxaDeAcerto());
        }
        System.out.println();
    }

    /** Ordenar antes ajuda, mas nao sempre. */
    public static void ordem() {
        System.out.println();
        System.out.println("ORDEM  (ordenar do maior para o menor ajuda quanto?)");
        System.out.printf(Locale.ROOT, "%-10s %12s %12s %12s %14s%n",
                "itens", "primeira", "ordenada", "ganho", "ordenar perde");
        for (int quantidade = 3; quantidade <= 7; quantidade++) {
            Varredura varredura = new Varredura(quantidade, 7);
            Map<Heuristica, Varredura.Placar> placares = varredura.medir();
            double semOrdem = placares.get(Heuristica.PRIMEIRA).taxaDeAcerto();
            double comOrdem = placares.get(Heuristica.PRIMEIRA_ORDENADA).taxaDeAcerto();
            long perde = contarOndeOrdenarPerde(varredura);
            System.out.printf(Locale.ROOT, "%-10d %11.2f%% %11.2f%% %11.2f%% %14d%n",
                    quantidade, 100 * semOrdem, 100 * comOrdem,
                    100 * (comOrdem - semOrdem), perde);
        }
        System.out.println();
    }

    /** Em quantas sequencias a versao ordenada gasta MAIS caixas que a que nao ordena. */
    private static long contarOndeOrdenarPerde(Varredura varredura) {
        long[] conta = {0};
        varredura.cada(instancia -> {
            if (Heuristica.PRIMEIRA_ORDENADA.empacotar(instancia)
                    > Heuristica.PRIMEIRA.empacotar(instancia)) {
                conta[0]++;
            }
        });
        return conta[0];
    }
}
