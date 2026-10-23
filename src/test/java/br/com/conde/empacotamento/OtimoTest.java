package br.com.conde.empacotamento;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * O juiz e a definicao, nao um algoritmo melhor. Estes testes conferem o juiz
 * contra casos em que a resposta e obvia na mao.
 */
@DisplayName("A busca exata")
class OtimoTest {

    private final Otimo otimo = new Otimo();

    @Test
    @DisplayName("itens que fecham a caixa exata gastam exatamente o limite inferior")
    void quandoFechaCertinho() {
        assertThat(otimo.caixas(Instancia.de(10, 5, 5))).isEqualTo(1);
        assertThat(otimo.caixas(Instancia.de(10, 5, 5, 5, 5))).isEqualTo(2);
        assertThat(otimo.caixas(Instancia.de(7, 3, 4, 3, 4))).isEqualTo(2);
    }

    @Test
    @DisplayName("item do tamanho da caixa ocupa uma caixa inteira sozinho")
    void itemQueEnchACaixa() {
        assertThat(otimo.caixas(Instancia.de(5, 5, 5, 5))).isEqualTo(3);
        assertThat(otimo.caixas(Instancia.de(5, 5, 1, 1))).isEqualTo(2);
    }

    @Test
    @DisplayName("nunca devolve menos que o limite inferior nem mais que uma caixa por item")
    void ficaEntreOsLimites() {
        new Varredura(5, 5).cada(instancia -> {
            int minimo = otimo.caixas(instancia);

            assertThat(minimo).isGreaterThanOrEqualTo(instancia.limiteInferior());
            assertThat(minimo).isLessThanOrEqualTo(instancia.quantidade());
        });
    }

    @Test
    @DisplayName("nenhuma heuristica consegue gastar menos que o otimo, em instancia nenhuma")
    void ninguemBateOOtimo() {
        new Varredura(6, 6).cada(instancia -> {
            int minimo = otimo.caixas(instancia);

            for (Heuristica heuristica : Heuristica.values()) {
                assertThat(heuristica.empacotar(instancia))
                        .describedAs("%s em %s", heuristica.rotulo(), instancia)
                        .isGreaterThanOrEqualTo(minimo);
            }
        });
    }

    @Test
    @DisplayName("a ordem dos itens nao muda o otimo")
    void aOrdemNaoMudaOOtimo() {
        assertThat(otimo.caixas(Instancia.de(7, 1, 6, 2, 5)))
                .isEqualTo(otimo.caixas(Instancia.de(7, 6, 5, 2, 1)))
                .isEqualTo(otimo.caixas(Instancia.de(7, 2, 1, 5, 6)));
    }

    @Test
    @DisplayName("o resultado fica guardado por conjunto, e as 5.040 ordens pagam uma busca so")
    void guardaPorConjunto() {
        Varredura varredura = new Varredura(5, 5);
        varredura.medir();

        assertThat(varredura.otimo().guardados()).isLessThan((int) varredura.instancias());
        assertThat(varredura.otimo().guardados()).isEqualTo(126);
    }

    @Test
    @DisplayName("item maior que a caixa nao e instancia valida")
    void itemGrandeDemais() {
        assertThatThrownBy(() -> Instancia.de(5, 6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao cabe");
    }
}
