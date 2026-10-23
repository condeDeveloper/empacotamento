package br.com.conde.empacotamento;

import java.util.Arrays;

/**
 * Um conjunto de itens para empacotar, na ordem em que chegam, e o tamanho da
 * caixa.
 *
 * <p>A ordem faz parte da instancia de proposito. Para as heuristicas que
 * decidem com o item na mao, trocar a ordem muda o resultado, e boa parte do
 * assunto deste repositorio e exatamente quanto ela muda.
 */
public record Instancia(int[] itens, int capacidade) {

    public Instancia {
        if (capacidade < 1) {
            throw new IllegalArgumentException("caixa de capacidade " + capacidade);
        }
        for (int item : itens) {
            if (item < 1 || item > capacidade) {
                throw new IllegalArgumentException("item " + item + " nao cabe numa caixa de " + capacidade);
            }
        }
    }

    public static Instancia de(int capacidade, int... itens) {
        return new Instancia(itens, capacidade);
    }

    public int quantidade() {
        return itens.length;
    }

    public int soma() {
        int total = 0;
        for (int item : itens) {
            total += item;
        }
        return total;
    }

    /**
     * O limite inferior obvio: nem que as caixas fechassem sem sobrar nada, seriam
     * precisas pelo menos estas. Nenhuma solucao pode usar menos.
     */
    public int limiteInferior() {
        return (soma() + capacidade - 1) / capacidade;
    }

    /** A mesma instancia com os itens em ordem crescente, para servir de chave. */
    public Instancia canonica() {
        int[] copia = itens.clone();
        Arrays.sort(copia);
        return new Instancia(copia, capacidade);
    }

    public String chave() {
        return capacidade + ":" + Arrays.toString(canonica().itens());
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof Instancia i
                && capacidade == i.capacidade && Arrays.equals(itens, i.itens);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(itens) + capacidade;
    }

    @Override
    public String toString() {
        return Arrays.toString(itens) + " em caixas de " + capacidade;
    }
}
