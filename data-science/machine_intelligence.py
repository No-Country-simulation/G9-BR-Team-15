import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder
from sklearn.ensemble import RandomForestClassifier
from sklearn.pipeline import Pipeline

# Carregar o dataset
df = pd.read_csv('data-science/consumo_original.csv')

# Separar as colunas que a IA vai usar para aprender (X)
x = df[['tipo_cliente', 'porte_cliente', 'uso_horario_pico_horas', 'quantidade_equipamentos', 'tempo_medio_uso_diario']]

# Vou traduzi as colunas de texto para números mágicos que a IA entende melhor
preprocessor = ColumnTransformer(
    transformers=[
        ('texto_para_numero', OneHotEncoder(drop='first'), ['tipo_cliente', 'porte_cliente'])
    ], remainder='passthrough'
    
)

# ================================
# Aqui é onde vou fazer a CLASSIFICAÇÃO (Eficiente/Moderado/Ineficiente)
# ================================

# Coluna pra prever
y_classicacao = df['perfil_energetico']

# Dividindo 80% para a IA estudar e 20% para testar se ela aprendeu mesmo
x_train_c, x_test_c, y_train_c, y_test_c = train_test_split(x, y_classicacao, test_size=0.2, random_state=42) 

# Criando o modelo de classificação com o RANDOM FOREST
modelo_classificacao = Pipeline(steps=[
    ('preparador', preprocessor),
    ('ia_floresta', RandomForestClassifier(random_state=42))
])

# Treinando o modelo de classificação
modelo_classificacao.fit(x_train_c, y_train_c)

# Fazendo previsões com o modelo treinado
acuracia_classificacao = modelo_classificacao.score(x_test_c, y_test_c)
print(f"🎯 IA de Classificação treinada! Acurácia: {acuracia_classificacao * 100:.2f}%\n")

# ============================
# Espaço para a Regressão
# ============================

# Coluna pra prever
y_regressao = df['consumo_kwh']

# Dividindo os dados
x_train_k, x_test_k, y_train_k, y_test_k = train_test_split(x, y_regressao, test_size=0.2, random_state=42)
print("🛠️ Terreno da Kelly preparado com sucesso!")
print("As variáveis x_train_k e y_train_k estão prontas para a regressão.")

# O codigo para regressão começa aqui embaixo!👇