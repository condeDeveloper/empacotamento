package br.com.conde.empacotamento;

import java.util.Arrays;

/**
 * As seis maneiras de enfiar itens em caixas sem pensar muito.
 *
 * <p>Todas recebem os itens NA ORDEM em que chegam e devolvem quantas caixas
 * gastaram. Tres delas decidem com o item na mao, sem saber o que vem depois
 * (sao as que servem para fluxo que chega em tempo real); as outras tres primeiro
 * ordenam do maior para o menor, o que so e possivel quando todos os itens ja
 * estao a vista.
 */
public enum Heuristica {

    /**
     * Proxima que couber: so olha a caixa atual. Se nao couber, fecha a caixa e
     * abre outra, e nunca mais volta nela.
     */
    PROXIMA("proxima", false) {
        @Override
        int caixas(int[] itens, int capacidade) {
            int usadas = 1;
            int espaco = capacidade;
            for (int item : itens) {
                if (item > espaco) {
                    usadas++;
                    espaco = capacidade;
                }
                espaco -= item;
            }
            return usadas;
        }
    },

    /** Primeira que couber: percorre as caixas abertas na ordem e usa a primeira. */
    PRIMEIRA("primeira", false) {
        @Override
        int caixas(int[] itens, int capacidade) {
            return porEscolha(itens, capacidade, Escolha.PRIMEIRA);
        }
    },

    /** Melhor que couber: usa a caixa onde o item deixa MENOS espaco sobrando. */
    MELHOR("melhor", false) {
        @Override
        int caixas(int[] itens, int capacidade) {
            return porEscolha(itens, capacidade, Escolha.MELHOR);
        }
    },

    /** Pior que couber: usa a caixa com MAIS espaco livre. Parece absurdo, e tem motivo. */
    PIOR("pior", false) {
        @Override
        int caixas(int[] itens, int capacidade) {
            return porEscolha(itens, capacidade, Escolha.PIOR);
        }
    },

    /** Primeira que couber, com os itens ordenados do maior para o menor. */
    PRIMEIRA_ORDENADA("primeira ordenada", true) {
        @Override
        int caixas(int[] itens, int capacidade) {
            return porEscolha(decrescente(itens), capacidade, Escolha.PRIMEIRA);
        }
    },

    /** Melhor que couber, com os itens ordenados do maior para o menor. */
    MELHOR_ORDENADA("melhor ordenada", true) {
        @Override
        int caixas(int[] itens, int capacidade) {
            return porEscolha(decrescente(itens), capacidade, Escolha.MELHOR);
        }
    };

    private enum Escolha { PRIMEIRA, MELHOR, PIOR }

    private final String rotulo;
    private final boolean ordena;

    Heuristica(String rotulo, boolean ordena) {
        this.rotulo = rotulo;
        this.ordena = ordena;
    }

    public String rotulo() {
        return rotulo;
    }

    /** Ela precisa ver todos os itens antes de comecar? */
    public boolean ordena() {
        return ordena;
    }

    abstract int caixas(int[] itens, int capacidade);

    /** Quantas caixas esta heuristica gasta com estes itens, nesta ordem. */
    public int empacotar(Instancia instancia) {
        return caixas(instancia.itens(), instancia.capacidade());
    }

    private static int[] decrescente(int[] itens) {
        int[] copia = itens.clone();
        Arrays.sort(copia);
        for (int i = 0, j = copia.length - 1; i < j; i++, j--) {
            int guarda = copia[i];
            copia[i] = copia[j];
            copia[j] = guarda;
        }
        return copia;
    }

    private static int porEscolha(int[] itens, int capacidade, Escolha escolha) {
        int[] livre = new int[itens.length];
        int usadas = 0;
        for (int item : itens) {
            int alvo = -1;
            for (int caixa = 0; caixa < usadas; caixa++) {
                if (livre[caixa] < item) {
                    continue;
                }
                if (alvo < 0) {
                    alvo = caixa;
                } else if (escolha == Escolha.MELHOR && livre[caixa] < livre[alvo]) {
                    alvo = caixa;
                } else if (escolha == Escolha.PIOR && livre[caixa] > livre[alvo]) {
                    alvo = caixa;
                }
                if (escolha == Escolha.PRIMEIRA) {
                    break;
                }
            }
            if (alvo < 0) {
                alvo = usadas++;
                livre[alvo] = capacidade;
            }
            livre[alvo] -= item;
        }
        return usadas;
    }
}
