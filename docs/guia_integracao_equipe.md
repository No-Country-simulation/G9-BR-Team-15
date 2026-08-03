# Como usar a IA do EnergiAI — guia pro resto do time

Esse documento explica pra quem não é de Ciência de Dados (Data Viz e Backend) como plugar o que foi feito nas suas partes.

## ⚠️ Existem DOIS modelos, com endpoints separados no `ml-service`

- **Modelo MVP (obrigatório)** — `treino_modelo_mvp.py` / `prever_mvp.py` / `modelo_mvp.pkl`, exposto em **`POST /api/v1/teste/analise-energetica`** (com alias **`/teste-analise-energetica`**). Formato de entrada fixo, exigido pelo edital (`consumo_kwh`, `uso_horario_pico`, `quantidade_equipamentos`, `tipo_imovel`, `horas_alto_consumo`). Acurácia de 98,5%.
- **Modelo principal (detalhado)** — `treino_modelo.py` / `prever.py` / `modelo_energia.pkl`, exposto em **`POST /api/v1/analise-energetica`** (com alias **`/analise-energetica`**). Usa a lista de equipamentos do cliente, 68,5% de acurácia real (sem vazamento de dado). **É esse que o dashboard usa, e é esse que representa o trabalho de Ciência de Dados.**

Não existe escolha aqui — os dois endpoints coexistem, cada um com seu propósito. O obrigatório do edital não pode ter o formato alterado.

---

## Pro Data Viz (dashboard)

**O dashboard usa exclusivamente o Modelo Principal.** Nada muda aqui com a existência do Modelo MVP.

**⚠️ Atenção, isso é importante:** o `dashboard.py` atual lê `consumo_original.csv` e usa colunas (`tempo_medio_uso_diario`, `uso_horario_pico_horas`, `tipo_cliente`) que **não existem mais**. A base atual é outra: `base_energetica.csv`, gerada pelo `treino_modelo.py`.

### O que trocar no dashboard

| Coluna antiga (não existe mais) | Coluna nova equivalente |
|---|---|
| `consumo_kwh` | `consumo_total_kwh` |
| `tempo_medio_uso_diario` | `horas_uso_diario_media` (número) ou `faixa_uso_diario` (Baixo/Medio/Alto) |
| `uso_horario_pico_horas` | não existe mais nessa base (a tabela de equipamentos não traz mais horário de pico) |
| `tipo_cliente` | `tipo_pessoa` (PF/PJ) **e** `tipo_imovel` (Residencial/Comercial/Industrial), separados |
| `quantidade_equipamentos` | mesma coluna, mantida |
| `perfil_energetico` | mesma coluna, mantida (critério novo: desvio-padrão por tipo de imóvel) |

### Novidade: consumo por categoria de equipamento

A base tem uma coluna pra cada categoria: `qtd_cozinha`, `qtd_climatizacao`, `qtd_banheiro`, `qtd_entretenimento`, `qtd_iluminacao`, `qtd_industrial`, `qtd_limpeza`, `qtd_ti`. Ótimo material pra um gráfico "de onde vem o consumo dos clientes".

### Novidade: `previsoes.csv`

Se quiser mostrar no dashboard não só a classificação, mas também recomendação e custo estimado por cliente, use o `previsoes.csv` (gerado por `prever_em_lote()` no `prever.py`) em vez do `base_energetica.csv`. Ele já vem com `categoria`, `probabilidade`, `recomendacoes`, `consumo_estimado_kwh`, `custo_estimado_mensal` e `alerta_consumo_alto` por cliente.

### Como gerar a base pra usar no dashboard

```python
import treino_modelo as tm

df_cliente, df_equip, df_catalogo = tm.carregar_dados(".")
df_cliente, df_equip = tm.limpar_dados(df_cliente, df_equip)
base = tm.criar_base_energetica(df_cliente, df_equip, df_catalogo)
base = tm.classificar_perfil_energetico(base)
```

Ou simplesmente ler o CSV já pronto depois de rodar `python treino_modelo.py` uma vez:
```python
df = pd.read_csv("base_energetica.csv")
```

---

## Pro Backend

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
    "custo_estimado_mensal": 315.00
}
```

### `POST /api/v1/analise-energetica` — Modelo Principal (recomendado pro resto do sistema)

A função `prever()` do `prever.py` precisa da **lista de equipamentos do cliente**, porque o modelo foi treinado usando consumo por categoria de equipamento. Formato de entrada:

```python
tipo_pessoa = "PF"  # ou "PJ"
tipo_imovel = "Residencial"  # ou "Comercial" / "Industrial"
equipamentos = [
    {"tipo": "Geladeira Frost Free", "quantidade": 1, "horas_uso_diario": 24, "dias_uso_mes": 30},
    {"tipo": "Chuveiro Elétrico", "quantidade": 1, "horas_uso_diario": 0.5, "dias_uso_mes": 30},
]
```

O campo `"tipo"` de cada equipamento precisa bater com um dos nomes que já existem em `tabela_equipamento_catalogo.csv`. Isso já é o formato que a entidade `ClienteEquipamento` guarda no banco — o backend só precisa montar essa lista a partir do que já tem salvo pro cliente.

```python
import joblib
from prever import prever, carregar_catalogo

modelo = joblib.load("modelo_energia.pkl")
df_catalogo = carregar_catalogo(".")

resultado = prever(tipo_pessoa, tipo_imovel, equipamentos, modelo, df_catalogo)
```

Retorna:
```python
{
    "categoria": "Eficiente",
    "probabilidade": 0.44,
    "recomendacoes": ["...", "..."],
    "consumo_estimado_kwh": 199.5,
    "custo_estimado_mensal": 149.62,
    "alerta_consumo_alto": False
}
```

Os dois endpoints rodam dentro do `ml-service` (Python/FastAPI, arquivo `main.py`) — o backend Java chama esse serviço via HTTP, não dentro do próprio backend Java, já que o modelo é scikit-learn (Python).

### Novidade: simulação de economia (só no Modelo Principal)

Se o front quiser mostrar "quanto você economizaria reduzindo X% do consumo", use:
```python
from prever import simular_economia
simular_economia(consumo_atual_kwh=500, reducao_percentual=20)
# -> {'consumo_atual_kwh': 500, 'consumo_projetado_kwh': 400, 'economia_kwh': 100, 'economia_reais': 75.0}
```

---

## Arquivos que cada pessoa precisa

| Arquivo | Quem usa |
|---|---|
| `treino_modelo.py` / `treino_modelo_mvp.py` | só quem for retreinar o modelo (Ciência de Dados) |
| `modelo_energia.pkl` + `modelo_mvp.pkl` | quem for fazer o `ml-service` (Backend/quem cuidar da API) — precisa dos dois |
| `prever.py` + `prever_mvp.py` | idem — são o "manual de instruções" de como usar cada `.pkl` |
| `main.py` | o `ml-service`, expõe os dois endpoints |
| `base_energetica.csv` ou `previsoes.csv` | Data Viz, pro dashboard (sempre Modelo Principal) |
| `eda_consumo.ipynb` | qualquer um que quiser entender/apresentar a análise completa |
