package br.com.conde.empacotamento;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("As seis heuristicas")
class HeuristicaTest {

    @ParameterizedTest
    @EnumSource(Heuristica.class)
    @DisplayName("nenhuma gasta menos caixas que o limite inferior")
    void respeitamOLimite(Heuristica heuristica) {
        new Varredura(5, 5).cada(instancia ->
                assertThat(heuristica.empacotar(instancia))
                        .describedAs("%s em %s", heuristica.rotulo(), instancia)
                        .isGreaterThanOrEqualTo(instancia.limiteInferior()));
    }

    @ParameterizedTest
    @EnumSource(Heuristica.class)
    @DisplayName("nenhuma gasta mais que uma caixa por item")
    void naoDesperdicamAToa(Heuristica heuristica) {
        new Varredura(5, 5).cada(instancia ->
                assertThat(heuristica.empacotar(instancia))
                        .isLessThanOrEqualTo(instancia.quantidade()));
    }

    @Test
    @DisplayName("proxima que couber fecha a caixa e nunca volta nela")
    void proximaNaoVolta() {
        Instancia alternada = Instancia.de(7, 1, 7, 1, 7, 1);

        assertThat(Heuristica.PROXIMA.empacotar(alternada)).isEqualTo(5);
        assertThat(Heuristica.PRIMEIRA.empacotar(alternada)).isEqualTo(3);
    }

    @Test
    @DisplayName("melhor que couber aperta o item onde sobra menos espaco")
    void melhorAperta() {
        Instancia instancia = Instancia.de(10, 6, 3, 4);

        assertThat(Heuristica.MELHOR.empacotar(instancia)).isEqualTo(2);
        assertThat(Heuristica.PIOR.empacotar(instancia)).isEqualTo(2);
    }

    @Test
    @DisplayName("pior que couber espalha, e e isso que o derruba")
    void piorEspalha() {
        Instancia instancia = Instancia.de(7, 2, 3, 3, 1, 4, 4, 4);

        assertThat(Heuristica.PIOR.empacotar(instancia)).isEqualTo(5);
        assertThat(new Otimo().caixas(instancia)).isEqualTo(3);
    }

    @Test
    @DisplayName("so as ordenadas precisam ver todos os itens antes de comecar")
    void quemPrecisaVerTudo() {
        assertThat(Heuristica.PROXIMA.ordena()).isFalse();
        assertThat(Heuristica.PRIMEIRA.ordena()).isFalse();
        assertThat(Heuristica.MELHOR.ordena()).isFalse();
        assertThat(Heuristica.PIOR.ordena()).isFalse();
        assertThat(Heuristica.PRIMEIRA_ORDENADA.ordena()).isTrue();
        assertThat(Heuristica.MELHOR_ORDENADA.ordena()).isTrue();
    }

    @Test
    @DisplayName("nas ordenadas a ordem de chegada nao muda nada")
    void ordenadasIgnoramAChegada() {
        Instancia uma = Instancia.de(9, 5, 4, 3, 2);
        Instancia outra = Instancia.de(9, 2, 3, 4, 5);

        assertThat(Heuristica.PRIMEIRA_ORDENADA.empacotar(uma))
                .isEqualTo(Heuristica.PRIMEIRA_ORDENADA.empacotar(outra));
        assertThat(Heuristica.MELHOR_ORDENADA.empacotar(uma))
                .isEqualTo(Heuristica.MELHOR_ORDENADA.empacotar(outra));
    }

    @Test
    @DisplayName("nas que decidem na hora, a ordem muda: e por isso que a varredura usa sequencias")
    void asOutrasSentemAOrdem() {
        Instancia uma = Instancia.de(7, 1, 1, 1, 1, 1, 3, 5);
        Instancia outra = Instancia.de(7, 5, 3, 1, 1, 1, 1, 1);

        assertThat(Heuristica.PRIMEIRA.empacotar(uma))
                .isNotEqualTo(Heuristica.PRIMEIRA.empacotar(outra));
    }
}
