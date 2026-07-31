# Como usar a IA do EnergiAI — guia pro resto do time

Esse documento explica pra quem não é de Ciência de Dados (Data Viz e Backend) como plugar o que foi feito nas suas partes.

**Modelo final: Random Forest** (68,5% de acurácia, F1 macro 0,676 — melhor que Regressão Logística e Árvore de Decisão, testado com comparação justa entre os 3).

---

## Dashboard

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

### O que a IA espera receber

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

### O que a IA devolve

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

Já bate com o formato que o endpoint `POST /analise-energetica` precisa devolver. `alerta_consumo_alto` é `True` quando o cliente cai em "Ineficiente" — útil se o front quiser destacar isso visualmente.

### Como chamar

```python
import joblib
from prever import prever, carregar_catalogo

modelo = joblib.load("modelo_energia.pkl")
df_catalogo = carregar_catalogo(".")

resultado = prever(tipo_pessoa, tipo_imovel, equipamentos, modelo, df_catalogo)
```

Isso deveria rodar dentro do `ml-service` (Python/FastAPI), que o backend Java chama via HTTP — não dentro do próprio backend Java, já que o modelo é scikit-learn (Python).

### Novidade: simulação de economia

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
| `treino_modelo.py` | só quem for retreinar o modelo (Ciência de Dados) |
| `modelo_energia.pkl` | quem for fazer o `ml-service` (Backend/quem cuidar da API) |
| `prever.py` | idem — é o "manual de instruções" de como usar o `.pkl` |
| `base_energetica.csv` ou `previsoes.csv` | Data Viz, pro dashboard |
| `eda_consumo.ipynb` | qualquer um que quiser entender/apresentar a análise completa |
