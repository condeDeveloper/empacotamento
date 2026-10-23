package br.com.conde.empacotamento;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** Os numeros publicados no README, presos em teste. */
@DisplayName("Os numeros do README")
class MedidasTest {

    private static final Map<Heuristica, Varredura.Placar> PLACARES = new Varredura(7, 7).medir();

    private double acerto(Heuristica heuristica) {
        return Math.round(10000 * PLACARES.get(heuristica).taxaDeAcerto()) / 100.0;
    }

    @Test
    @DisplayName("tabela das heuristicas: taxa de acerto de cada uma")
    void taxasDeAcerto() {
        assertThat(acerto(Heuristica.PROXIMA)).isEqualTo(50.80);
        assertThat(acerto(Heuristica.PRIMEIRA)).isEqualTo(90.18);
        assertThat(acerto(Heuristica.MELHOR)).isEqualTo(92.05);
        assertThat(acerto(Heuristica.PIOR)).isEqualTo(86.96);
        assertThat(acerto(Heuristica.PRIMEIRA_ORDENADA)).isEqualTo(99.97);
        assertThat(acerto(Heuristica.MELHOR_ORDENADA)).isEqualTo(99.97);
    }

    @Test
    @DisplayName("as piores razoes publicadas")
    void pioresRazoes() {
        assertThat(PLACARES.get(Heuristica.PROXIMA).piorRazao()).isCloseTo(1.75, within(0.001));
        assertThat(PLACARES.get(Heuristica.PRIMEIRA).piorRazao()).isCloseTo(1.5, within(0.001));
        assertThat(PLACARES.get(Heuristica.MELHOR).piorRazao()).isCloseTo(1.5, within(0.001));
        assertThat(PLACARES.get(Heuristica.PIOR).piorRazao()).isCloseTo(5.0 / 3, within(0.001));
        assertThat(PLACARES.get(Heuristica.PRIMEIRA_ORDENADA).piorRazao())
                .isCloseTo(4.0 / 3, within(0.001));
    }

    @Test
    @DisplayName("a varredura visita 823.543 sequencias e resolve 1.716 conjuntos distintos")
    void tamanhoDaVarredura() {
        Varredura varredura = new Varredura(7, 7);
        varredura.medir();

        assertThat(varredura.instancias()).isEqualTo(823_543);
        assertThat(PLACARES.get(Heuristica.PRIMEIRA).instancias()).isEqualTo(823_543);
        assertThat(varredura.otimo().guardados()).isEqualTo(1716);
    }

    @Test
    @DisplayName("o pior caso da proxima que couber e o padrao alternado")
    void oPiorDaProxima() {
        Instancia pior = PLACARES.get(Heuristica.PROXIMA).pior();

        assertThat(Heuristica.PROXIMA.empacotar(pior)).isEqualTo(7);
        assertThat(new Otimo().caixas(pior)).isEqualTo(4);
    }

    @Test
    @DisplayName("tabela da ordem: a taxa de acerto por quantidade de itens")
    void tabelaDaOrdem() {
        assertThat(acertoCom(3)).isEqualTo(100.00);
        assertThat(acertoCom(4)).isEqualTo(97.63);
        assertThat(acertoCom(5)).isEqualTo(95.29);
        assertThat(acertoCom(6)).isEqualTo(92.73);
        assertThat(acertoCom(7)).isEqualTo(90.18);
    }

    @Test
    @DisplayName("o desperdicio total: a primeira que couber gasta 2,1% a mais de caixa")
    void desperdicio() {
        assertThat(PLACARES.get(Heuristica.PRIMEIRA).desperdicio()).isCloseTo(1.0212, within(0.0001));
        assertThat(PLACARES.get(Heuristica.PROXIMA).desperdicio()).isCloseTo(1.1132, within(0.0001));
        assertThat(PLACARES.get(Heuristica.PRIMEIRA_ORDENADA).desperdicio())
                .isCloseTo(1.0001, within(0.0001));
    }

    @Test
    @DisplayName("o medidor roda inteiro sem lancar")
    void medidorRoda() {
        Medidor.heuristicas();
        Medidor.pior();
        Medidor.ordem();
    }

    private double acertoCom(int itens) {
        return Math.round(10000 * new Varredura(itens, 7).medir()
                .get(Heuristica.PRIMEIRA).taxaDeAcerto()) / 100.0;
    }
}
