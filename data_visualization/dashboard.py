# ============================
#Importação das bibliotecas necessárias
# ============================

import pandas as pd
import plotly.express as px
import plotly.graph_objects as go
import plotly.io as pio
import numpy as np
import os

from plotly.subplots import make_subplots

# ============================
# Tema Profissional EnerSmart
# ============================

CORES = {

    "verde_principal": "#006455",

    "verde_energia": "#00A878",

    "verde_escuro": "#003B32",

    "verde_claro": "#76D7C4",

    "cinza": "#F5F8F7"
}

pio.templates["Lu | Men Dashboard"] = go.layout.Template(
    layout=go.Layout(

        font=dict(
            family="Arial",
            size=14,
            color=CORES["verde_principal"]
        ),

        title=dict(
            font=dict(
                size=22,
                color=CORES["verde_principal"]
            ),
            x=0.05
        ),

        paper_bgcolor="#F5F8F7",

        plot_bgcolor="white",

        hoverlabel=dict(
            bgcolor="white",
            font_size=13,
            font_family="Arial"
        ),

        margin=dict(
            l=50,
            r=50,
            t=80,
            b=50
        ),

        xaxis=dict(
    showgrid=True,
    gridcolor="#E8F2EF"
),

yaxis=dict(
    showgrid=False
)
    )
)

pio.templates.default="Lu | Men Dashboard"

# ============================
# Paleta
# ============================
PALETA_ENERSMART = [
    "#006455",
    "#00A878",
    "#76D7C4",
    "#003B32"
]

# ============================
# Carregar base para Dashboard
# ============================
from pathlib import Path


def carregar_base_dashboard():

    BASE_DIR = Path(__file__).resolve().parent.parent

    arquivo = BASE_DIR / "data-science" / "consumo_original.csv"

    if not arquivo.exists():
        raise FileNotFoundError(
            f"Arquivo não encontrado: {arquivo}"
        )

    df = pd.read_csv(arquivo)

    print("✅ Base carregada para Dashboard!")
    print(f"📂 Caminho: {arquivo}")
    print(f"📊 Total de registros: {len(df)}")

    return df

df = carregar_base_dashboard()

# ============================
# # Criado atributo temporal simulado para análise mensal
# ============================

np.random.seed(42)

df["mes"] = np.random.choice(
    [
        "Jan", "Fev", "Mar", "Abr",
        "Mai", "Jun", "Jul",
        "Ago", "Set", "Out",
        "Nov", "Dez"
    ],
    size=len(df)
)

# ============================
# 1 - Indicadores do Dashboard
# ============================

total_clientes = len(df)

consumo_total = df['consumo_kwh'].sum()

consumo_medio = df['consumo_kwh'].mean()

equipamentos_medios = df['quantidade_equipamentos'].mean()

tempo_medio = df['tempo_medio_uso_diario'].mean()

eficientes = (df['perfil_energetico'] == 'Eficiente').sum()

moderados = (df['perfil_energetico'] == 'Moderado').sum()

ineficientes = (df['perfil_energetico'] == 'Ineficiente').sum()

percentual_eficientes = (eficientes / total_clientes) * 100

percentual_moderados = (moderados / total_clientes) * 100

percentual_ineficientes = (ineficientes / total_clientes) * 100

print("========== KPIs ==========")
print(f"Clientes: {total_clientes}")
print(f"Consumo Total: {consumo_total:.2f} kWh")
print(f"Consumo Médio: {consumo_medio:.2f} kWh")
print(f"Equipamentos Médios: {equipamentos_medios:.1f}")
print(f"Tempo Médio de Uso: {tempo_medio:.1f} horas")
print(f"% Clientes Eficientes: {percentual_eficientes:.1f}%")
print(f"% Clientes Moderados: {percentual_moderados:.1f}%")
print(f"% Clientes Ineficientes: {percentual_ineficientes:.1f}%")


# ============================
# card de contexto para apresentação
# ============================
print("""
⚡ Lu | Men Dashboard

Objetivo:
Identificar padrões de consumo energético
e oportunidades de eficiência.

Inteligência Artificial:
Modelo de classificação energética:

🟢 Eficiente
🟡 Moderado
🔴 Ineficiente

Base analisada:
{} clientes
""".format(total_clientes))

# ============================
# Layout Dashboard
# ============================

dashboard = make_subplots(

    rows=4,
    cols=2,

    specs=[
        [{"type": "indicator"}, {"type": "bar"}],

        [{"type": "bar"}, {"type": "pie"}],

        [{"type": "bar"}, {"type": "scatter"}],

        [{"type": "bar"}, {"type": "indicator"}]
    ],

    subplot_titles=[

        "Consumo Médio por Cliente",

        "Consumo por Perfil",

        "Consumo por Cliente",

        "Distribuição dos Perfis",

        "Quantidade de Clientes",

        "Consumo x Horário Pico",

        "Horas de Pico",

        "Clientes Eficientes"
    ]
)

# ============================
# 2-[Dashboard] Criar KPI de consumo mensal (kWh)
# ============================
dashboard.add_trace(

    go.Indicator(

        mode="number",

        value=round(consumo_medio,1),

        number={
            "suffix":" kWh",
            "font":{
                "size":45,
                "color":CORES["verde_principal"]
            }
        },

        title={
            "text":"<b>Consumo Médio por Cliente</b>"        }

    ),

    row=1,

    col=1

)

# ============================
# 3- [Dashboard] Criar gráfico de consumo energético
# ============================

consumo = df.groupby("perfil_energetico")["consumo_kwh"].mean().reset_index()

fig = px.bar(
    consumo,

    y="perfil_energetico",

    x="consumo_kwh",

    orientation="h",

    color="perfil_energetico",

    color_discrete_sequence=PALETA_ENERSMART,

    text_auto=".0f",

    title="<b>Consumo Médio por Perfil Energético</b>"
)


fig.update_traces(
    textposition="outside",
    marker_line_width=1,
    marker_line_color="white"
)


fig.update_layout(

    height=450,

    showlegend=False,

    xaxis_title="Média mensal kWh",

    yaxis_title="Perfil Energético",

    bargap=0.35

)

for trace in fig.data:

    dashboard.add_trace(

        trace,

        row=1,

        col=2

    )


consumo_tipo = df.groupby("tipo_cliente")["consumo_kwh"].mean().reset_index()

fig = px.bar(

    consumo_tipo,

    y="tipo_cliente",

    x="consumo_kwh",

    orientation="h",

    color="tipo_cliente",

    color_discrete_sequence=PALETA_ENERSMART,

    text_auto=".0f",

    title="<b>Consumo Médio por Tipo de Cliente</b>"

)


fig.update_traces(

    textposition="outside",

    marker_line_width=1,

    marker_line_color="white"

)


fig.update_layout(

    height=450,

    showlegend=False,

    xaxis_title="Consumo Médio (kWh)",

    yaxis_title="Tipo de Cliente",

    bargap=0.35

)
for trace in fig.data:

    dashboard.add_trace(
        trace,
        row=2,
        col=1
    )


# ============================
# 4 - [Dashboard] Criar gráfico de eficiência energética
# ============================
eficiencia = df['perfil_energetico'].value_counts().reset_index()

eficiencia.columns = ['Perfil','Quantidade']

fig = px.pie(
    eficiencia,

    names="Perfil",

    values="Quantidade",

    hole=0.55,

    color_discrete_sequence=PALETA_ENERSMART,

    title="<b>Distribuição dos Perfis Energéticos</b>"
)


fig.update_traces(
    textinfo="percent+label"
)


fig.update_layout(
    height=450
)

for trace in fig.data:

    dashboard.add_trace(
        trace,
        row=2,
        col=2
    )

fig = px.bar(
    eficiencia,

    y="Perfil",

    x="Quantidade",

    orientation="h",

    color="Perfil",

    color_discrete_sequence=PALETA_ENERSMART,

    text_auto=True,

    title="<b>Quantidade de Clientes por Perfil Energético</b>"

)


fig.update_traces(

    textposition="outside"

)


fig.update_layout(

    height=450,

    showlegend=False,

    xaxis_title="Quantidade de Clientes",

    yaxis_title="Perfil"

)

dashboard.add_trace(

    fig.data[0],

    row=3,

    col=1

)


# ============================
# 5 - [Dashboard] Criar gráfico de pico de consumo
# ============================

fig = px.scatter(
    df,

    x="uso_horario_pico_horas",

    y="consumo_kwh",

    color="perfil_energetico",

    size="quantidade_equipamentos",

    hover_data=[
        "tipo_cliente"
    ],

    color_discrete_sequence=PALETA_ENERSMART,

    title="<b>Padrão de Consumo x Horário de Pico</b>"
)


fig.update_layout(
    height=500
)

for trace in fig.data:

    dashboard.add_trace(
        trace,
        row=3,
        col=2
    )



pico = df.groupby("tipo_cliente")["uso_horario_pico_horas"].mean().reset_index()

fig = px.bar(

    pico,

    y="tipo_cliente",

    x="uso_horario_pico_horas",

    orientation="h",

    color="tipo_cliente",

    color_discrete_sequence=PALETA_ENERSMART,

    text_auto=".1f",

    title="<b>Horas Médias de Uso no Horário de Pico</b>"

)


fig.update_traces(

    textposition="outside"

)


fig.update_layout(

    height=450,

    showlegend=False,

    xaxis_title="Horas",

    yaxis_title="Tipo de Cliente"

)

for trace in fig.data:

    dashboard.add_trace(
        trace,
        row=4,
        col=1
    )

dashboard.add_trace(

    go.Indicator(

        mode="number",

        value=round(percentual_eficientes,1),

        number={
            "suffix": "%",
            "font": {
                "size": 45,
                "color": CORES["verde_principal"]
            }
        },

        title={
            "text": "<b>Clientes Eficientes</b>"
        }

    ),

    row=4,

    col=2

)

dashboard.update_layout(

    height=1800,

    width=1400,

    title_text="<b>⚡ Lu | Men Dashboard - Eficiência Energética</b>",

    template="Lu | Men Dashboard",

    showlegend=False

)


dashboard.show()

# ============================
# Esses gráficos contam uma história clara dos dados:
# | Ordem                        | Gráfico                             | Objetivo                                               |
# | ---------------------------- | ----------------------------------- | ------------------------------------------------------ |
# | 📊 KPI                       | Total de Clientes                   | Volume analisado                                       |
# | 📊 KPI                       | Consumo Médio (kWh)                 | Indicador principal                                    |
# | 📊 KPI                       | % Clientes Eficientes               | Eficiência geral                                       |
# | 📈 Barras                    | Consumo por Tipo de Cliente         | Comparar perfis de consumidores                        |
# | 🥧 Pizza                     | Distribuição do Perfil Energético   | Mostrar a classificação da IA                          |
# | 📈 Barras                    | Consumo Médio por Perfil Energético | Relacionar consumo e eficiência                        |
# | 🔵 Dispersão                 | Horário de Pico × Consumo           | Identificar padrões de uso                             |
# | 📦 Box Plot                  | Consumo por Perfil Energético       | Visualizar dispersão e outliers                        |
# | 🔥 Heatmap                   | Correlação entre variáveis          | Apoiar a análise exploratória (EDA)                    |
# | 🌳 Importância das Variáveis | Random Forest                       | Mostrar quais fatores mais influenciam a classificação |
# ============================
# %%
