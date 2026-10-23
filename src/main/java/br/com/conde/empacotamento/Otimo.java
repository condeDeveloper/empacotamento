package br.com.conde.empacotamento;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * O juiz: o menor numero de caixas que existe para a instancia.
 *
 * <p>Nao e uma heuristica melhor, e a <b>definicao</b>. A busca tenta pôr cada
 * item em cada caixa ja aberta e, no maximo, numa caixa nova, o que varre todas
 * as particoes possiveis sem repetir nenhuma por simetria.
 *
 * <p>Duas podas, e so elas, deixam a busca caber em milhoes de instancias:
 * parar quando o numero de caixas ja empatou com o limite inferior, e descartar
 * o ramo que ja gastou tanto quanto a melhor solucao conhecida.
 */
public final class Otimo {

    private final Map<String, Integer> guardados = new HashMap<>();

    /** O minimo de caixas. A instancia e normalizada, entao a ordem nao importa aqui. */
    public int caixas(Instancia instancia) {
        if (instancia.quantidade() == 0) {
            return 0;
        }
        String chave = instancia.chave();
        Integer guardado = guardados.get(chave);
        if (guardado != null) {
            return guardado;
        }
        int resposta = calcular(instancia);
        guardados.put(chave, resposta);
        return resposta;
    }

    public int guardados() {
        return guardados.size();
    }

    private int calcular(Instancia instancia) {
        // do maior para o menor: a decisao dificil vem primeiro e a poda corta mais cedo
        int[] itens = instancia.canonica().itens();
        int meio = itens.length / 2;
        for (int i = 0; i < meio; i++) {
            int guarda = itens[i];
            itens[i] = itens[itens.length - 1 - i];
            itens[itens.length - 1 - i] = guarda;
        }

        int piso = instancia.limiteInferior();
        int teto = Heuristica.PRIMEIRA_ORDENADA.empacotar(instancia);
        if (teto == piso) {
            return piso;
        }

        int[] livre = new int[itens.length];
        Arrays.fill(livre, instancia.capacidade());
        return buscar(itens, 0, livre, 0, teto, piso, instancia.capacidade());
    }

    private int buscar(int[] itens, int indice, int[] livre, int abertas,
                       int melhor, int piso, int capacidade) {
        if (indice == itens.length) {
            return Math.min(melhor, abertas);
        }
        if (abertas >= melhor) {
            return melhor;
        }
        int item = itens[indice];
        for (int caixa = 0; caixa < abertas; caixa++) {
            if (livre[caixa] < item) {
                continue;
            }
            // duas caixas com o mesmo espaco livre dao o mesmo resultado: basta uma
            boolean repetida = false;
            for (int anterior = 0; anterior < caixa; anterior++) {
                if (livre[anterior] == livre[caixa]) {
                    repetida = true;
                    break;
                }
            }
            if (repetida) {
                continue;
            }
            livre[caixa] -= item;
            melhor = buscar(itens, indice + 1, livre, abertas, melhor, piso, capacidade);
            livre[caixa] += item;
            if (melhor == piso) {
                return melhor;
            }
        }
        if (abertas + 1 < melhor) {
            livre[abertas] = capacidade - item;
            melhor = buscar(itens, indice + 1, livre, abertas + 1, melhor, piso, capacidade);
            livre[abertas] = capacidade;
        }
        return melhor;
    }
}
