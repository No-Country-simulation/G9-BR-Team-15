# Roteiro de apresentação — Ciência de Dados (EnergiAI)

## 1. Abertura (contexto)

A minha parte no EnergiAI foi pegar os dados que o backend (estrutura — cliente-equipamentos) que ele tem, e o catálogo de potência de cada equipamento — e transformar isso numa IA capaz de classificar o perfil energético de qualquer cliente (Eficiente, Moderado ou Ineficiente), com recomendações de economia e uma estimativa de custo mensal.

## 2. Como os dados foram tratados

Encontrei e corrigi dois bugs reais nos dados: 15 registros com quantidade de equipamento negativa (fisicamente impossível), e 50 clientes com o campo `tipo_pessoa` vazio. Também reconstruí a base juntando as 3 tabelas relacionais do backend numa linha por cliente, com consumo agregado por categoria de equipamento (cozinha, climatização, banheiro, TI, etc.).

## 3. Como o critério de eficiência foi definido (ponto forte pra defender)

Em vez de um número fixo arbitrário, calculei a intensidade de consumo de cada cliente e comparei ele só com clientes do **mesmo tipo de imóvel** — residencial com residencial, comercial com comercial, industrial com industrial. Não foi uma escolha aleatória: é o mesmo princípio que a ANEEL usa oficialmente pra separar consumidores em Grupo A (indústrias/grandes comércios) e Grupo B (residências/pequenos negócios). Cheguei a comparar a proporção dos meus dados com números reais publicados pela EPE — bateu quase exato (2,2x a mais de consumo em comércio vs. residência, contra ~2,2x real).

## 4. Os modelos treinados e por quê escolhi o Random Forest

O edital pedia pra comparar modelos supervisionados, então treinei três: Random Forest, Árvore de Decisão e Regressão Logística — todos com o mesmo pré-processamento, incluindo normalização das variáveis numéricas pra garantir que nenhum modelo saísse em desvantagem injusta. Um cuidado importante: garanti que o modelo nunca recebesse os números exatos usados pra calcular a resposta certa (vazamento de dado / data leakage) — cheguei a ter uma versão com 96% de acurácia que descartei porque era só o modelo decorando uma fórmula.

**Resultado final:**
| Modelo | Acurácia | F1 macro |
|---|---|---|
| Random Forest (escolhido) | 68,5% | 0,676 |
| Regressão Logística | 66,5% | 0,661 |
| Árvore de Decisão | 57,5% | 0,574 |

Escolhi pelo F1 macro, mais justo que acurácia simples quando as classes não têm exatamente o mesmo tamanho. O Random Forest também tem a vantagem de mostrar quais variáveis mais pesam na decisão, o que ajuda a explicar as recomendações.

## 5. Demonstração ao vivo (3 exemplos + recursos extras)

Pra mostrar que funciona na prática, rodei 3 exemplos — residencial, comercial e industrial. Pra cada um, o sistema devolve categoria, probabilidade, recomendação personalizada, custo estimado (tarifa de R$ 0,75/kWh) e um alerta se o consumo estiver alto. Também implementei uma simulação de economia (\"se reduzir X% do consumo, economiza tanto\") e processamento em lote, pra gerar essa análise pra todos os clientes de uma vez.


## 6. Fechamento

Com isso, a parte de Ciência de Dados cobre tudo que o edital pediu: base de dados construída, EDA, critérios de classificação justificados com base regulatória real, três modelos comparados de forma justa, avaliação com métricas adequadas, recomendações, estimativa de custo, alertas, simulação de economia e processamento em lote — e o modelo serializado (`modelo_energia.pkl`), pronto pra o backend integrar.



