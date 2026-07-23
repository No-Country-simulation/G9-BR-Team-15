# ============================
#Importação das bibliotecas necessárias
# ============================
import pandas as pd
import plotly.express as px
import plotly.graph_objects as go

# ============================
# caminho do CSV
# ============================
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent

arquivo = BASE_DIR / "data-science" / "consumo_original.csv"

df = pd.read_csv(arquivo)

print("✅ Dataset carregado com sucesso!")
print(df.head())
print(df.columns)

# ============================
# criado uma coluna mês simulada
# ============================

import numpy as np

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
# 1 - [Dashboard] Definir indicadores de eficiência energética
# ============================

# ============================
# 1 - Indicadores do Dashboard
# ============================

total_clientes = len(df)

consumo_total = df['consumo_kwh'].sum()

consumo_medio = df['consumo_kwh'].mean()

equipamentos_medios = df['quantidade_equipamentos'].mean()

tempo_medio = df['tempo_medio_uso_diario'].mean()

eficientes = (df['perfil_energetico'] == 'Eficiente').sum()

percentual_eficientes = (eficientes / total_clientes) * 100

print("========== KPIs ==========")
print(f"Clientes: {total_clientes}")
print(f"Consumo Total: {consumo_total:.2f} kWh")
print(f"Consumo Médio: {consumo_medio:.2f} kWh")
print(f"Equipamentos Médios: {equipamentos_medios:.1f}")
print(f"Tempo Médio de Uso: {tempo_medio:.1f} horas")
print(f"% Clientes Eficientes: {percentual_eficientes:.1f}%")

# ============================
# 2-[Dashboard] Criar KPI de consumo mensal (kWh)
# ============================
df.groupby("mes")["consumo_kwh"].sum()

fig = go.Figure(go.Indicator(
    mode="number+delta",
    value=consumo_medio,
    number={'suffix':" kWh"},
    title={"text":"Consumo Médio"}
))

fig.show()

# ============================
# 3- [Dashboard] Criar gráfico de consumo energético
# ============================

consumo = df.groupby("perfil_energetico")["consumo_kwh"].mean().reset_index()

fig = px.bar(
    consumo,
    x="perfil_energetico",
    y="consumo_kwh",
    color="perfil_energetico",
    title="Consumo Médio por Perfil Energético"
)

fig.show()

consumo_tipo = df.groupby("tipo_cliente")["consumo_kwh"].mean().reset_index()

fig = px.bar(
    consumo_tipo,
    x="tipo_cliente",
    y="consumo_kwh",
    color="tipo_cliente",
    title="Consumo Médio por Tipo de Cliente"
)

fig.show()

# ============================
# 4 - [Dashboard] Criar gráfico de eficiência energética
# ============================
eficiencia = df['perfil_energetico'].value_counts().reset_index()

eficiencia.columns = ['Perfil','Quantidade']

fig = px.pie(
    eficiencia,
    names='Perfil',
    values='Quantidade',
    title='Distribuição dos Perfis Energéticos'
)

fig.show()

fig = px.bar(
    eficiencia,
    x='Perfil',
    y='Quantidade',
    color='Perfil',
    title='Perfil Energético dos Clientes'
)

fig.show()

# ============================
# 5 - [Dashboard] Criar gráfico de pico de consumo
# ============================

fig = px.scatter(
    df,
    x='uso_horario_pico_horas',
    y='consumo_kwh',
    color='perfil_energetico',
    size='quantidade_equipamentos',
    hover_data=['tipo_cliente'],
    title='Consumo x Horário de Pico'
)

fig.show()


pico = df.groupby("tipo_cliente")["uso_horario_pico_horas"].mean().reset_index()

fig = px.bar(
    pico,
    x="tipo_cliente",
    y="uso_horario_pico_horas",
    color="tipo_cliente",
    title="Horas Médias de Uso no Horário de Pico"
)

fig.show()


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
