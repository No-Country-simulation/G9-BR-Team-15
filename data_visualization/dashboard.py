# ============================================================
# ⚡ EnerSmart AI Dashboard
#
# Dashboard analítico baseado nas previsões geradas
# pelo modelo de Machine Learning.
#
# Objetivos:
# - Avaliar classificação energética dos clientes
# - Medir confiança da IA
# - Analisar consumo estimado
# - Avaliar impacto financeiro
#
# Hackathon Energy
# ============================================================


# ============================================================
# 1. Bibliotecas
# ============================================================

import pandas as pd
import plotly.express as px
import plotly.graph_objects as go
import plotly.io as pio

from plotly.subplots import make_subplots
from pathlib import Path



# ============================================================
# 2. Identidade Visual EnerSmart
# ============================================================


CORES = {

    "verde_principal": "#006455",

    "verde_energia": "#00A878",

    "verde_escuro": "#003B32",

    "verde_claro": "#76D7C4",

    "cinza": "#F5F8F7"

}



PALETA_ENERSMART = [

    CORES["verde_principal"],

    CORES["verde_energia"],

    CORES["verde_claro"],

    CORES["verde_escuro"]

]



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



        paper_bgcolor=CORES["cinza"],


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



pio.templates.default = "Lu | Men Dashboard"




# ============================================================
# 3. Carregamento da base IA
# ============================================================


def carregar_base_dashboard():

    """
    Carrega a base gerada pelo modelo ML.
    """


    BASE_DIR = Path(__file__).resolve().parent.parent


    arquivo = (

        BASE_DIR /

        "data-science" /

        "previsoes.csv"

    )



    if not arquivo.exists():

        raise FileNotFoundError(

            f"Arquivo não encontrado: {arquivo}"

        )



    df = pd.read_csv(arquivo)



    print("=" * 45)

    print("⚡ EnerSmart AI Dashboard")

    print("=" * 45)

    print("✅ Base carregada!")

    print(f"📂 Arquivo: {arquivo}")

    print(f"👥 Clientes analisados: {len(df)}")



    return df




df = carregar_base_dashboard()




# ============================================================
# 4. Validação da estrutura da base
# ============================================================


COLUNAS_OBRIGATORIAS = [

    "id_cliente",

    "categoria",

    "probabilidade",

    "consumo_estimado_kwh",

    "custo_estimado_mensal",

    "recomendacoes"

]



for coluna in COLUNAS_OBRIGATORIAS:


    if coluna not in df.columns:


        raise ValueError(

            f"Coluna obrigatória ausente: {coluna}"

        )



print("✅ Estrutura da base validada!")




# ============================================================
# 5. KPIs Inteligência Artificial
# ============================================================


total_clientes = len(df)



consumo_total = (

    df["consumo_estimado_kwh"]

    .sum()

)



consumo_medio = (

    df["consumo_estimado_kwh"]

    .mean()

)



custo_total = (

    df["custo_estimado_mensal"]

    .sum()

)



confianca_media = (

    df["probabilidade"]

    .mean()

    *100

)




print("\n========= KPIs =========")

print(f"Clientes: {total_clientes}")

print(f"Consumo previsto: {consumo_total:,.2f} kWh")

print(f"Consumo médio: {consumo_medio:,.2f} kWh")

print(f"Custo estimado: R$ {custo_total:,.2f}")

print(f"Confiança IA: {confianca_media:.2f}%")



# ============================================================
# 6. Estrutura Dashboard
# ============================================================


dashboard = make_subplots(

    rows=4,

    cols=2,


    specs=[

        [{"type":"indicator"},
         {"type":"indicator"}],


        [{"type":"pie"},
         {"type":"bar"}],


        [{"type":"bar"},
         {"type":"scatter"}],


        [{"type":"bar"},
         {"type":"table"}]

    ],


subplot_titles=[

    "",

    "",

    "Classificação Energética IA",

    "Consumo Médio Previsto",

    "Impacto Financeiro",

    "Confiança IA x Consumo",

    "Top Consumidores",

    "Recomendações"

]
)
# ============================================================
# 7. KPI - Quantidade de Clientes
# ============================================================


dashboard.add_trace(

    go.Indicator(

        mode="number",

        value=total_clientes,


        number={

            "font": {

                "size": 45,

                "color": CORES["verde_principal"]

            }

        },


        title={

            "text": "<b>Clientes analisados</b>"

        }

    ),


    row=1,

    col=1

)




# ============================================================
# 8. KPI - Confiança Média da IA
# ============================================================


dashboard.add_trace(

    go.Indicator(

        mode="number",

        value=round(confianca_media,1),


        number={

            "suffix":"%",

            "font": {

                "size":45,

                "color":CORES["verde_energia"]

            }

        },


        title={

            "text":"<b>Confiança média IA</b>"

        }

    ),


    row=1,

    col=2

)


# ============================================================
# 9. Distribuição da Classificação IA
# ============================================================


perfil = (

    df["categoria"]

    .value_counts()

    .reset_index()

)


perfil.columns = [

    "Categoria",

    "Quantidade"

]



fig = px.pie(

    perfil,

    names="Categoria",

    values="Quantidade",

    hole=0.55,

    color="Categoria",

    color_discrete_map={

        "Eficiente": "#00A878",

        "Moderado": "#76D7C4",

        "Ineficiente": "#003B32"

    },

    title="<b>Classificação Energética IA</b>"

)


fig.update_traces(

    textinfo="percent+label",

    hovertemplate=

    "<b>%{label}</b><br>"
    "Clientes: %{value}<br>"
    "Percentual: %{percent}"

)


fig.update_layout(
    showlegend=True,
    legend=dict(
        orientation="h",
        yanchor="bottom",
        y=-0.25,
        xanchor="center",
        x=0.5
    )
)


for trace in fig.data:

    trace.showlegend = True

    dashboard.add_trace(

        trace,

        row=2,

        col=1

    )

fig.update_layout(
    legend=dict(
        orientation="v",
        x=1.02,
        y=0.5
    )
)

# ============================================================
# 10. Consumo Médio Previsto por Categoria
# ============================================================


consumo_categoria = (

    df.groupby("categoria")

    ["consumo_estimado_kwh"]

    .mean()

    .reset_index()

)



fig = px.bar(

    consumo_categoria,


    x="categoria",


    y="consumo_estimado_kwh",


    color="categoria",


    color_discrete_sequence=PALETA_ENERSMART,


    text_auto=".0f",


    title="<b>Consumo Médio Previsto</b>"

)


fig.update_traces(

    textposition="outside",

    width=0.35

)


fig.update_layout(

    bargap=0.6,

    showlegend=True,

    legend_title_text="Categoria"

)

fig.data[0].showlegend = False

for trace in fig.data:

    trace.showlegend = False

    dashboard.add_trace(
        trace,
        row=2,
        col=2
    )



# ============================================================
# 11. Impacto Financeiro Estimado
# ============================================================


custo_categoria = (

    df.groupby("categoria")

    ["custo_estimado_mensal"]

    .sum()

    .reset_index()

)



fig = px.bar(

    custo_categoria,


    x="categoria",


    y="custo_estimado_mensal",


    color="categoria",


    color_discrete_sequence=PALETA_ENERSMART,


    text_auto=".0f",


    title="<b>Custo Energético Estimado</b>"

)



fig.update_traces(

    textposition="outside"

)



dashboard.add_trace(

    fig.data[0],

    row=3,

    col=1

)





# ============================================================
# 12. Explicabilidade do Modelo
# ============================================================


fig = px.scatter(

    df,

    x="probabilidade",

    y="consumo_estimado_kwh",

    color="categoria",

    size="consumo_estimado_kwh",

    size_max=25,


    hover_data=[

        "id_cliente",

        "custo_estimado_mensal"

    ],


    title="<b>Confiança IA x Consumo Previsto</b>"

)

fig.update_layout(
    showlegend=True,
    legend_title_text="Perfil IA",
    legend=dict(
        orientation="h",
        yanchor="bottom",
        y=-0.25,
        xanchor="center",
        x=0.5
    )
)


for trace in fig.data:

    trace.showlegend = True

    dashboard.add_trace(

        trace,

        row=3,

        col=2

    )




# ============================================================
# 13. Ranking dos Maiores Consumidores
# ============================================================


top_clientes = (

    df.sort_values(

        "consumo_estimado_kwh",

        ascending=False

    )

    .head(10)

)



fig = px.bar(

    top_clientes,

    x="id_cliente",

    y="consumo_estimado_kwh",

    color="categoria",


    color_discrete_map={

        "Eficiente": "#00A878",

        "Moderado": "#76D7C4",

        "Ineficiente": "#003B32"

    },


    text="consumo_estimado_kwh",


    title="<b>Top 10 Consumidores</b>"

)


fig.update_traces(

    texttemplate="%{text:.0f} kWh",

    textposition="outside",

    width=0.9

)


fig.update_layout(
    showlegend=True,
    legend_title_text="Categoria IA",
    bargap=0.15,
    legend=dict(
        orientation="h",
        yanchor="bottom",
        y=-0.25,
        xanchor="center",
        x=0.5
    )
)



fig.update_traces(

    textposition="outside"

)



for trace in fig.data:

    trace.showlegend = True

    dashboard.add_trace(

        trace,

        row=4,

        col=1

    )




# ============================================================
# 14. Tabela de Recomendações IA
# ============================================================


tabela = go.Table(


    header=dict(

        values=[

            "<b>Cliente</b>",

            "<b>Categoria</b>",

            "<b>Recomendação IA</b>"

        ],


        fill_color=CORES["verde_principal"],


        font=dict(

            color="white"

        )

    ),



    cells=dict(

        values=[


            df.head(10)["id_cliente"],


            df.head(10)["categoria"],


            df.head(10)["recomendacoes"]

        ],


        fill_color=CORES["cinza"]

    )

)



dashboard.add_trace(

    tabela,

    row=4,

    col=2

)





# ============================================================
# 15. Layout Final
# ============================================================


dashboard.update_layout(

    showlegend=False,

    height=1900,

    width=1400,

    template="Lu | Men Dashboard",

    title=dict(

        text=(
            "<b>⚡ EnerSmart AI Dashboard</b><br>"
            "<sup>Inteligência Artificial aplicada "
            "à eficiência energética</sup>"
        ),

        x=0.05,

        y=0.98

    ),


    margin=dict(

        t=130,

        b=50,

        l=50,

        r=50

    )

)


# ============================================================
# 16. Executar Dashboard
# ============================================================


if __name__ == "__main__":

    print("\n🚀 Abrindo Dashboard EnerSmart...")

    dashboard.show()