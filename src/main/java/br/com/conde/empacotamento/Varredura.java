package br.com.conde.empacotamento;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Gera TODAS as sequencias de {@code n} itens com tamanhos de 1 ate a
 * capacidade, e mede cada heuristica contra o otimo em cada uma.
 *
 * <p>Sequencias, e nao conjuntos: a ordem de chegada faz parte do problema para
 * quem decide com o item na mao. Sao {@code capacidade^n} instancias, e e por
 * isso que o tamanho fica pequeno de proposito. O otimo e guardado por conjunto
 * ordenado, entao as {@code n!} ordens da mesma instancia so pagam uma busca.
 */
public final class Varredura {

    private final int quantidade;
    private final int capacidade;
    private final Otimo otimo = new Otimo();

    public Varredura(int quantidade, int capacidade) {
        if (quantidade < 1 || capacidade < 1) {
            throw new IllegalArgumentException("precisa de pelo menos um item e uma caixa");
        }
        if (Math.pow(capacidade, quantidade) > 5_000_000) {
            throw new IllegalArgumentException(
                    "capacidade^quantidade passou de 5 milhoes de sequencias");
        }
        this.quantidade = quantidade;
        this.capacidade = capacidade;
    }

    /** O que uma heuristica fez em todas as instancias. */
    public static final class Placar {
        private long instancias;
        private long caixas;
        private long caixasOtimas;
        private long acertos;
        private double piorRazao = 1;
        private Instancia pior;

        void somar(Instancia instancia, int usadas, int minimo) {
            instancias++;
            caixas += usadas;
            caixasOtimas += minimo;
            if (usadas == minimo) {
                acertos++;
            }
            double razao = (double) usadas / minimo;
            if (razao > piorRazao) {
                piorRazao = razao;
                pior = instancia;
            }
        }

        public long instancias() {
            return instancias;
        }

        /** Em que fracao das instancias ela empatou com o otimo. */
        public double taxaDeAcerto() {
            return (double) acertos / instancias;
        }

        /** Caixas gastas no total, dividido pelas caixas do otimo no total. */
        public double desperdicio() {
            return (double) caixas / caixasOtimas;
        }

        public double piorRazao() {
            return piorRazao;
        }

        public Instancia pior() {
            return pior;
        }
    }

    public Map<Heuristica, Placar> medir() {
        Map<Heuristica, Placar> placares = new EnumMap<>(Heuristica.class);
        for (Heuristica heuristica : Heuristica.values()) {
            placares.put(heuristica, new Placar());
        }
        cada(instancia -> {
            int minimo = otimo.caixas(instancia);
            for (Heuristica heuristica : Heuristica.values()) {
                placares.get(heuristica).somar(instancia, heuristica.empacotar(instancia), minimo);
            }
        });
        return placares;
    }

    /** Chama o consumidor uma vez para cada sequencia possivel. */
    public void cada(Consumer<Instancia> consumidor) {
        int[] itens = new int[quantidade];
        gerar(itens, 0, consumidor);
    }

    private void gerar(int[] itens, int indice, Consumer<Instancia> consumidor) {
        if (indice == itens.length) {
            consumidor.accept(new Instancia(itens.clone(), capacidade));
            return;
        }
        for (int tamanho = 1; tamanho <= capacidade; tamanho++) {
            itens[indice] = tamanho;
            gerar(itens, indice + 1, consumidor);
        }
    }

    public long instancias() {
        long total = 1;
        for (int i = 0; i < quantidade; i++) {
            total *= capacidade;
        }
        return total;
    }

    public Otimo otimo() {
        return otimo;
    }
}
