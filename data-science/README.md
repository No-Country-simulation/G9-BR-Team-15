# data-science — EnergiAI

Parte de Ciência de Dados do projeto. Classifica o perfil energético de um cliente (Eficiente, Moderado ou Ineficiente), gera recomendações e estima o custo mensal.

## Arquivos

- `treino_modelo.py` — treina e compara 3 modelos (Random Forest, Árvore de Decisão, Regressão Logística) e salva o melhor em `modelo_energia.pkl`
- `prever.py` — usa o modelo já treinado pra prever o perfil de um cliente novo, com recomendações, custo estimado, alerta e simulação de economia
- `eda_consumo.ipynb` — notebook com a análise completa (limpeza, EDA, critérios, treino, avaliação, exemplos)
- `tabela_cliente.csv`, `tabela_cliente_equipamento.csv`, `tabela_equipamento_catalogo.csv` — dados de entrada, vindos do backend

## Como rodar

```
pip install pandas scikit-learn joblib
python treino_modelo.py
```
Isso gera `modelo_energia.pkl` e `base_energetica.csv` na mesma pasta.

## Modelo final: Random Forest

68,5% de acurácia, F1 macro 0,676 (comparado com Regressão Logística 66,5% e Árvore de Decisão 57,5%, todos testados com o mesmo pré-processamento, incluindo normalização das variáveis numéricas pra garantir comparação justa entre os modelos).

## Critério de classificação

O perfil é calculado comparando o consumo de cada cliente com a média do seu **próprio tipo de imóvel** (Residencial, Comercial ou Industrial) — segue o mesmo princípio que a ANEEL/Light usam pra separar Grupo A (indústria/grande porte) de Grupo B (residência/pequeno porte). Mais detalhes no notebook, Seção 6.

---

## Como o Backend vai usar

A função `prever()` do `prever.py` recebe a lista de equipamentos do cliente (o mesmo formato que já é salvo em `ClienteEquipamento`) e devolve a resposta pronta pro endpoint `POST /analise-energetica`:

```python
import joblib
from prever import prever, carregar_catalogo

modelo = joblib.load("modelo_energia.pkl")
df_catalogo = carregar_catalogo(".")

resultado = prever(
    tipo_pessoa="PF",              # PF ou PJ
    tipo_imovel="Residencial",     # Residencial, Comercial ou Industrial
    equipamentos=[
        {"tipo": "Geladeira Frost Free", "quantidade": 1, "horas_uso_diario": 24, "dias_uso_mes": 30},
        {"tipo": "Chuveiro Elétrico", "quantidade": 1, "horas_uso_diario": 0.5, "dias_uso_mes": 30},
    ],
    modelo=modelo,
    df_catalogo=df_catalogo,
)
```

Retorna:
```python
{
    "categoria": "Eficiente",
    "probabilidade": 0.44,
    "recomendacoes": ["...", "..."],
    "consumo_estimado_kwh": 199.5,
    "custo_estimado_mensal": 149.62,
    "alerta_consumo_alto": False,
}
```

Isso deve rodar dentro do **ml-service** (Python/FastAPI) — o backend Java chama esse serviço via HTTP, não importa o `.pkl` diretamente (scikit-learn é Python, não dá pra carregar em Java).

**Bônus:** se precisar rodar a previsão pra vários clientes de uma vez (ex: gerar um relatório em lote), use `prever_em_lote(pasta=".")` — processa todos os clientes das tabelas e salva em `previsoes.csv`.

## Como o Dashboard vai usar

O dashboard deve ler `base_energetica.csv` (gerado pelo `treino_modelo.py`) em vez de qualquer CSV antigo. Colunas principais disponíveis pra gráficos:
- `consumo_total_kwh`, `tipo_pessoa`, `tipo_imovel`, `perfil_energetico`
- `qtd_cozinha`, `qtd_climatizacao`, `qtd_banheiro`, `qtd_entretenimento`, `qtd_iluminacao`, `qtd_industrial`, `qtd_limpeza`, `qtd_ti` — quantidade de equipamentos por categoria, ótimo pra gráfico "de onde vem o consumo"

Se quiser os resultados já com recomendação/custo por cliente (não só a classificação), use o `previsoes.csv` gerado por `prever_em_lote()` em vez do `base_energetica.csv`.
