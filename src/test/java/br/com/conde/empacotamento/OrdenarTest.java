package br.com.conde.empacotamento;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ordenar do maior para o menor e o conselho que todo mundo da, e ele e bom:
 * leva a heuristica de 90% para 99,97% de acerto. O que ninguem conta e que ele
 * tambem PIORA o resultado em algumas instancias.
 */
@DisplayName("Ordenar antes de empacotar")
class OrdenarTest {

    @Test
    @DisplayName("a menor instancia em que ordenar nao basta: 4,3,3,2,2,2 em caixas de 8")
    void aMenorInstanciaQueDerrubaAOrdenada() {
        Instancia instancia = Instancia.de(8, 4, 3, 3, 2, 2, 2);

        assertThat(instancia.soma()).isEqualTo(16);
        assertThat(instancia.limiteInferior()).isEqualTo(2);
        assertThat(new Otimo().caixas(instancia)).isEqualTo(2);
        assertThat(Heuristica.PRIMEIRA_ORDENADA.empacotar(instancia)).isEqualTo(3);
        assertThat(Heuristica.MELHOR_ORDENADA.empacotar(instancia)).isEqualTo(3);
    }

    @Test
    @DisplayName("com caixa de ate seis, ordenar acerta o otimo em TODA instancia de seis itens")
    void caixaPequenaNaoDerruba() {
        for (int capacidade = 2; capacidade <= 6; capacidade++) {
            double acerto = new Varredura(6, capacidade).medir()
                    .get(Heuristica.PRIMEIRA_ORDENADA).taxaDeAcerto();

            assertThat(acerto)
                    .describedAs("caixa de %d", capacidade)
                    .isEqualTo(1.0);
        }
    }

    @Test
    @DisplayName("a partir da caixa de sete ela erra, e o erro cresce com o tamanho da caixa")
    void caixaGrandeDerruba() {
        double comSete = new Varredura(6, 7).medir().get(Heuristica.PRIMEIRA_ORDENADA).taxaDeAcerto();
        double comOito = new Varredura(6, 8).medir().get(Heuristica.PRIMEIRA_ORDENADA).taxaDeAcerto();

        assertThat(comSete).isLessThan(1.0);
        assertThat(comOito).isLessThan(comSete);
    }

    @Test
    @DisplayName("em 154 sequencias de sete itens, ordenar PIORA o resultado")
    void ordenarAsVezesPiora() {
        long piora = contar(new Varredura(7, 7));

        assertThat(piora).isEqualTo(154);
    }

    @Test
    @DisplayName("com poucos itens ordenar nunca piora: o defeito precisa de seis")
    void comPoucosItensNuncaPiora() {
        assertThat(contar(new Varredura(3, 7))).isZero();
        assertThat(contar(new Varredura(4, 7))).isZero();
        assertThat(contar(new Varredura(5, 7))).isZero();
        assertThat(contar(new Varredura(6, 7))).isEqualTo(11);
    }

    @Test
    @DisplayName("mesmo piorando as vezes, ordenar ganha no conjunto, e por muito")
    void noConjuntoOrdenarGanha() {
        var placares = new Varredura(7, 7).medir();
        double semOrdem = placares.get(Heuristica.PRIMEIRA).taxaDeAcerto();
        double comOrdem = placares.get(Heuristica.PRIMEIRA_ORDENADA).taxaDeAcerto();

        assertThat(comOrdem - semOrdem).isGreaterThan(0.09);
    }

    private long contar(Varredura varredura) {
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
