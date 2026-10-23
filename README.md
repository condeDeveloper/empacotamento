# empacotamento

Empacotamento em caixas do zero em Java 21, com seis heurísticas medidas contra
**o ótimo exato** em todas as 823.543 sequências possíveis.

O juiz aqui não é um algoritmo melhor: é a definição. Uma busca exaustiva com
duas podas encontra o menor número de caixas que existe para cada instância, e
toda razão publicada é contra esse número, nunca contra outra heurística.

```
mvn -B -ntp verify
mvn -B -ntp exec:java -Dexec.args="heuristicas"   # (ou pior, ordem)
```

## As seis

7 itens de tamanho 1 a 7, caixas de capacidade 7:

| heurística | acerta o ótimo | caixas gastas / ótimas | pior razão | precisa ver tudo? |
|---|---:|---:|---:|---|
| próxima que couber | 50,80% | 1,1132 | 1,750 | não |
| primeira que couber | 90,18% | 1,0212 | 1,500 | não |
| melhor que couber | 92,05% | 1,0171 | 1,500 | não |
| pior que couber | 86,96% | 1,0281 | 1,667 | não |
| **primeira ordenada** | **99,97%** | 1,0001 | 1,333 | sim |
| **melhor ordenada** | **99,97%** | 1,0001 | 1,333 | sim |

As 823.543 sequências se reduzem a **1.716 conjuntos distintos** depois de
ordenadas, e a busca exata roda uma vez por conjunto: o ótimo não depende da
ordem de chegada, e as heurísticas dependem.

### Os piores casos, achados e não inventados

```
próxima que couber   razão 1,750   [1, 7, 1, 7, 1, 7, 1]
primeira que couber  razão 1,500   [1, 1, 1, 1, 1, 3, 5]
pior que couber      razão 1,667   [2, 3, 3, 1, 4, 4, 4]
ordenadas            razão 1,333   [2, 2, 2, 2, 3, 3, 6]
```

O pior caso da `próxima que couber` é o padrão alternado: item pequeno, item
enorme, item pequeno. Cada item grande fecha a caixa, e o pequeno que veio antes
fica sozinho num espaço de 6 que nunca mais será usado.

## Ordenar ajuda muito, e às vezes atrapalha

| itens | primeira que couber | primeira ordenada | ganho | sequências em que ordenar PIORA |
|---:|---:|---:|---:|---:|
| 3 | 100,00% | 100,00% | 0,00% | 0 |
| 4 | 97,63% | 100,00% | 2,37% | 0 |
| 5 | 95,29% | 100,00% | 4,71% | 0 |
| 6 | 92,73% | 99,99% | 7,25% | **11** |
| 7 | 90,18% | 99,97% | 9,79% | **154** |

Ordenar do maior para o menor é o conselho certo: leva a heurística de 90% para
99,97% de acerto. Mas em **154 das 823.543 sequências ela gasta mais caixas que
a versão que não ordena nada**. Não é empate: é pior.

A razão é que a heurística que não ordena às vezes tropeça numa boa solução por
acidente, colocando um item grande numa caixa que a versão ordenada já teria
enchido de pequenos. "Em média é melhor" e "é melhor" não são a mesma frase.

## A menor instância em que ordenar não basta

```
4, 3, 3, 2, 2, 2   em caixas de 8

ordenada:  [4,3] [3,2,2] [2]        3 caixas
ótimo:     [4,2,2] [3,3,2]          2 caixas
```

A soma é 16, exatamente duas caixas cheias. A versão ordenada começa pelo 4,
junta o 3 (dá 7, sobra 1, não cabe mais nada) e nunca se recupera. Existe uma
divisão perfeita e ela não a encontra.

E há um detalhe que só a varredura mostra:

| tamanho da caixa | ordenada acerta (6 itens) |
|---:|---:|
| 4 | 100,00% |
| 5 | 100,00% |
| 6 | 100,00% |
| 7 | 99,99% |
| 8 | 99,98% |

**Com caixas de até 6, ordenar nunca erra** em instância nenhuma de 6 itens. O
defeito precisa de espaço: só a partir de 7 unidades de caixa existe margem para
a sobra de uma caixa ser grande o bastante para importar e pequena o bastante
para a heurística desperdiçá-la.

## O que as medidas me corrigiram

1. **"Ordenar antes nunca piora."** Piora em 154 sequências de sete itens. Com
   até cinco itens realmente nunca piora, e é daí que vem a impressão.
2. **"Pior que couber é a pior ideia da lista."** É melhor que a `próxima que
   couber` por 36 pontos de acerto. Espalhar os itens é ruim, mas fechar a caixa
   e nunca mais voltar nela é muito pior.
3. **"Instância pequena já mostra a diferença entre as heurísticas."** Entre as
   ordenadas e o ótimo, não: com caixas de até 6 elas empatam em 100% e parecem
   perfeitas. A diferença só aparece quando a caixa cresce.
4. **"Melhor que couber é só a primeira que couber com mais trabalho."** Ela
   acerta 1,9 ponto a mais e tem o mesmo pior caso, mas as duas erram em
   instâncias diferentes: o pior caso ser igual não quer dizer que o
   comportamento seja.

## Como está organizado

- `Instancia`: os itens **na ordem em que chegam** e a capacidade. A ordem faz
  parte do problema.
- `Heuristica`: as seis, como enum. Três decidem com o item na mão; três
  precisam ver tudo antes.
- `Otimo`: a busca exata. Item vai para alguma caixa aberta ou para uma nova, o
  que varre todas as partições sem repetir por simetria. Duas podas: parar no
  limite inferior e cortar o ramo que já empatou com a melhor solução. Caixas com
  o mesmo espaço livre são tratadas como uma só.
- `Varredura`: gera todas as sequências e guarda o ótimo por conjunto ordenado.
- `Medidor`: as três tabelas acima, rodando no CI nos três sistemas.

38 testes.
