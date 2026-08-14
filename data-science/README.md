# data-science — EnergiAI

Parte de Ciência de Dados do projeto. Classifica o perfil energético de um cliente (Eficiente, Moderado ou Ineficiente), gera recomendações e estima o custo mensal.

## ⚠️ Existem DOIS modelos neste projeto

| | Modelo MVP (obrigatório) | Modelo Principal (detalhado) |
|---|---|---|
| Arquivos | `treino_modelo_mvp.py` + `prever_mvp.py` | `treino_modelo.py` + `prever.py` |
| Modelo salvo | `modelo_mvp.pkl` | `modelo_energia.pkl` |
| Endpoint no `ml-service` | `POST /api/v1/teste/analise-energetica` (formato fixo do edital, não pode mudar; alias `POST /teste-analise-energetica`) | `POST /api/v1/analise-energetica` (alias `POST /analise-energetica`) |
| Entrada | os 5 campos exatos do edital (`consumo_kwh`, `uso_horario_pico`, `quantidade_equipamentos`, `tipo_imovel`, `horas_alto_consumo`) | lista de equipamentos do cliente |
| Saída | categoria + probabilidade + recomendações + custo estimado | categoria + probabilidade + recomendações + custo estimado + consumo estimado + alerta |
| Acurácia | 99% (**não é um número bom** — veja abaixo) | 68,5% (Random Forest, sem vazamento de dado) |
| Usado por | só o endpoint obrigatório do edital / avaliação | **Dashboard** (`base_energetica.csv`) e a análise de Ciência de Dados |


**O dashboard usa exclusivamente o Modelo Principal.** Nada nele muda com a existência do Modelo MVP.

## Arquivos

- `treino_modelo.py` / `prever.py` — Modelo Principal (ver acima)
- `treino_modelo_mvp.py` / `prever_mvp.py` — Modelo MVP (ver acima); reaproveita `carregar_dados`, `limpar_dados`, `criar_base_energetica` e `classificar_perfil_energetico` de `treino_modelo.py`, então os dois modelos usam o mesmo rótulo de origem
- `eda_consumo.ipynb` — notebook com a análise completa (limpeza, EDA, critérios, treino, avaliação, exemplos)
- `tabela_cliente.csv`, `tabela_cliente_equipamento.csv`, `tabela_equipamento_catalogo.csv` — dados de entrada, vindos do backend

## Como rodar

```
pip install pandas scikit-learn joblib
python treino_modelo.py          # gera modelo_energia.pkl e base_energetica.csv
python treino_modelo_mvp.py   # gera modelo_mvp.pkl
```

## Modelo Principal: Random Forest

68,5% de acurácia, F1 macro 0,676 (comparado com Regressão Logística 66,5% e Árvore de Decisão 57,5%, todos testados com o mesmo pré-processamento, incluindo normalização das variáveis numéricas pra garantir comparação justa entre os modelos).

### Modelo MVP: Árvore de Decisão

99% de acurácia (comparado com Random Forest 98,5% e Regressão Logística 92%, todos testados com o mesmo pré-processamento, incluindo normalização das variáveis numéricas pra garantir comparação justa entre os modelos).



---

## Como o Backend vai usar

### `POST /api/v1/teste/analise-energetica` — endpoint obrigatório do edital

Formato fixo, não pode ser alterado:

```python
import joblib
from prever_mvp import prever_mvp

modelo_mvp = joblib.load("modelo_mvp.pkl")

resultado = prever_mvp(
    consumo_kwh=420,
    uso_horario_pico=True,
    quantidade_equipamentos=10,
    tipo_imovel="Residencial",
    horas_alto_consumo=8,
    modelo=modelo_mvp,
)
```

Retorna:
```python
{
    "categoria": "Ineficiente",
    "probabilidade": 0.81,
    "recomendacoes": ["...", "..."],
    "custo_estimado_mensal": 315.00,
}
```

### `POST /api/v1/analise-energetica` — Modelo Principal (recomendado pro resto do sistema)

A função `prever()` do `prever.py` recebe a lista de equipamentos do cliente (o mesmo formato que já é salvo em `ClienteEquipamento`):

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

Os dois endpoints rodam dentro do **ml-service** (Python/FastAPI, arquivo `main.py`) — o backend Java chama esse serviço via HTTP, não importa o `.pkl` diretamente (scikit-learn é Python, não dá pra carregar em Java).

**Bônus:** se precisar rodar a previsão do Modelo Principal pra vários clientes de uma vez (ex: gerar um relatório em lote), use `prever_em_lote(pasta=".")` — processa todos os clientes das tabelas e salva em `previsoes.csv`.

## Como o Dashboard (Data Viz) vai usar

**O dashboard usa exclusivamente o Modelo Principal.** Deve ler `base_energetica.csv` (gerado pelo `treino_modelo.py`) em vez de qualquer CSV antigo. Colunas principais disponíveis pra gráficos:
- `consumo_total_kwh`, `tipo_pessoa`, `tipo_imovel`, `perfil_energetico`
- `qtd_cozinha`, `qtd_climatizacao`, `qtd_banheiro`, `qtd_entretenimento`, `qtd_iluminacao`, `qtd_industrial`, `qtd_limpeza`, `qtd_ti` — quantidade de equipamentos por categoria, ótimo pra gráfico "de onde vem o consumo"

Se quiser os resultados já com recomendação/custo por cliente (não só a classificação), use o `previsoes.csv` gerado por `prever_em_lote()` em vez do `base_energetica.csv`.
