"""
EnergiAI - Treino e comparação de modelos de classificação de perfil energético

Como rodar:
1. Coloque este arquivo DENTRO da pasta 'data-science/', junto com:
   tabela_cliente.csv, tabela_cliente_equipamento.csv, tabela_equipamento_catalogo.csv
2. pip install pandas scikit-learn joblib
3. python treino_modelo.py
   (o modelo_energia.pkl e o base_energetica.csv são salvos na mesma pasta)

O que esse script faz:
1. Carrega e limpa os dados (remove quantidade de equipamento negativa)
2. Monta a base de dados energética (uma linha por cliente, com consumo por categoria de equipamento)
3. Define o perfil energético por DESVIO-PADRÃO em relação à média (dentro de cada tipo_imovel) --
   critério estatístico real, não uma proporção imposta tipo quantil fixo
4. Treina e compara 3 modelos: Random Forest, Árvore de Decisão e Regressão Logística
5. Escolhe o melhor pela métrica F1 macro e salva em modelo_energia.pkl
"""

import os
import joblib
import pandas as pd

from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, f1_score, classification_report
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.tree import DecisionTreeClassifier

MAPA_CATEGORIA = {
    "Geladeira Frost Free": "Cozinha", "Micro-ondas": "Cozinha", "Airfryer": "Cozinha",
    "Forno Elétrico": "Cozinha", "Liquidificador": "Cozinha", "Coifa / Depurador": "Cozinha",
    "Freezer Horizontal": "Cozinha",
    "Ar Condicionado Split": "Climatizacao", "Ventilador de Coluna": "Climatizacao",
    "Chuveiro Elétrico": "Banheiro", "Torneira Elétrica": "Banheiro",
    "Televisão Smart": "Entretenimento", "Videogame Console": "Entretenimento",
    "Computador Desktop": "TI", "Servidores / TI": "TI",
    "Robô Aspirador": "Limpeza",
    "Maquinário Industrial": "Industrial",
    "Iluminação Comercial (LEDs)": "Iluminacao",
}

COLUNAS_CATEGORICAS = ["tipo_pessoa", "tipo_imovel", "faixa_uso_diario", "faixa_dias_uso"]
COLUNAS_NUMERICAS = [
    "quantidade_equipamentos", "diversidade_equipamentos",
    "qtd_banheiro", "qtd_climatizacao", "qtd_cozinha", "qtd_entretenimento",
    "qtd_iluminacao", "qtd_industrial", "qtd_limpeza", "qtd_ti",
]
ORDEM_PERFIL = ["Eficiente", "Moderado", "Ineficiente"]


def pasta_do_script():
    """Retorna a pasta onde ESTE arquivo está salvo, não importa de onde o script foi chamado.
    Isso evita o bug clássico de 'FileNotFoundError' quando alguém roda `python treino_modelo.py`
    de fora da pasta data-science (ex: da raiz do repositório)."""
    return os.path.dirname(os.path.abspath(__file__))


def carregar_dados(pasta=None):
    if pasta is None:
        pasta = pasta_do_script()
    df_cliente = pd.read_csv(os.path.join(pasta, "tabela_cliente.csv"))
    df_equip = pd.read_csv(os.path.join(pasta, "tabela_cliente_equipamento.csv"))
    df_catalogo = pd.read_csv(os.path.join(pasta, "tabela_equipamento_catalogo.csv"))
    return df_cliente, df_equip, df_catalogo


def limpar_dados(df_cliente, df_equip):
    # 'quantidade' não pode ser negativa (bug encontrado: 15 linhas com valor < 0)
    df_equip = df_equip[df_equip["quantidade"] > 0].copy()

    # 'tipo_pessoa' vem vazio em 50 dos 1000 clientes (bug encontrado nos dados).
    # Preenchendo com a moda (valor mais comum), já que não faz sentido descartar o cliente inteiro.
    if df_cliente["tipo_pessoa"].isnull().any():
        moda = df_cliente["tipo_pessoa"].mode()[0]
        df_cliente = df_cliente.copy()
        df_cliente["tipo_pessoa"] = df_cliente["tipo_pessoa"].fillna(moda)

    return df_cliente, df_equip


def criar_base_energetica(df_cliente, df_equip, df_catalogo):
    df_catalogo = df_catalogo.copy()
    df_catalogo["categoria"] = df_catalogo["tipo"].map(MAPA_CATEGORIA)

    df = df_equip.merge(df_catalogo, left_on="id_equipamento", right_on="id", how="left", suffixes=("", "_equip"))

    # consumo estimado em kWh, por linha -- usado só pra CRIAR o rótulo, não é feature direta
    df["consumo_linha_kwh"] = (df["quantidade"] * df["horas_uso_diario"] * df["dias_uso_mes"] * df["potencia_watts"]) / 1000
    consumo_cliente = (
        df.groupby("id_cliente")["consumo_linha_kwh"].sum()
        .reset_index().rename(columns={"consumo_linha_kwh": "consumo_total_kwh"})
    )

    agregado = df.groupby("id_cliente").agg(
        quantidade_equipamentos=("quantidade", "sum"),
        diversidade_equipamentos=("id_equipamento", "nunique"),
        horas_uso_diario_media=("horas_uso_diario", "mean"),
        dias_uso_mes_media=("dias_uso_mes", "mean"),
    ).reset_index()

    def faixa_horas(h):
        if h < 4:
            return "Baixo"
        elif h <= 8:
            return "Medio"
        return "Alto"

    def faixa_dias(d):
        if d < 15:
            return "Ocasional"
        elif d <= 25:
            return "Frequente"
        return "Constante"

    agregado["faixa_uso_diario"] = agregado["horas_uso_diario_media"].apply(faixa_horas)
    agregado["faixa_dias_uso"] = agregado["dias_uso_mes_media"].apply(faixa_dias)

    qtd_categoria = df.pivot_table(
        index="id_cliente", columns="categoria", values="quantidade", aggfunc="sum", fill_value=0
    ).reset_index()
    qtd_categoria.columns = ["id_cliente"] + ["qtd_" + c.lower() for c in qtd_categoria.columns[1:]]

    base = df_cliente.rename(columns={"id": "id_cliente"})
    base = base.merge(agregado, on="id_cliente", how="left")
    base = base.merge(qtd_categoria, on="id_cliente", how="left")
    base = base.merge(consumo_cliente, on="id_cliente", how="left")

    return base


def classificar_perfil_energetico(base):
    """Critério por desvio-padrão da média, dentro de cada tipo_imovel (não é quantil fixo).

    Por que agrupar por tipo_imovel: a ANEEL (REN 1.000) separa oficialmente os consumidores
    em Grupo A (alta tensão -- essencialmente indústrias e grandes comércios) e Grupo B
    (baixa tensão -- residências e pequenos comércios/indústrias), justamente porque as escalas
    de consumo são muito diferentes entre eles. A Light segue essa mesma classificação.
    Comparar um cliente Industrial com um Residencial na mesma régua não faz sentido regulatório
    nem estatístico -- por isso cada tipo_imovel é avaliado só contra seus pares.
    """
    def classificar(grupo):
        media = grupo["consumo_total_kwh"].mean()
        desvio = grupo["consumo_total_kwh"].std()

        def rotular(valor):
            if valor <= media - 0.5 * desvio:
                return "Eficiente"
            elif valor >= media + 0.5 * desvio:
                return "Ineficiente"
            return "Moderado"

        return grupo["consumo_total_kwh"].apply(rotular)

    base["perfil_energetico"] = base.groupby("tipo_imovel", group_keys=False).apply(classificar)
    return base


# valores médios reais de referência (fonte: EPE - Anuário Estatístico de Energia Elétrica;
# consumo comercial de pequeno/médio porte via Sunwise/CEMIG). Usados só como checagem de
# coerência -- os dados aqui são sintéticos, então a ESCALA absoluta não precisa bater com a
# real, mas a PROPORÇÃO entre tipos de imóvel deveria seguir a mesma direção.
CONSUMO_MEDIO_REFERENCIA_KWH = {
    "Residencial": 180,   # média nacional residencial (EPE)
    "Comercial": 400,     # pequeno/médio comércio (referência CEMIG/Sunwise)
    "Industrial": None,   # não há um valor único -- indústria eletrointensiva varia demais
}


def verificar_coerencia_com_referencias_reais(base):
    """Compara a proporção entre tipos de imóvel nos nossos dados com a proporção real
    esperada (residencial < comercial < industrial). Não corrige nada sozinho -- só avisa
    se algo estiver na direção errada, pra facilitar defender o critério na apresentação."""
    medias = base.groupby("tipo_imovel")["consumo_total_kwh"].mean()
    print("Consumo médio por tipo de imóvel (nos seus dados):")
    print(medias.round(1))

    if "Residencial" in medias and "Comercial" in medias:
        if medias["Comercial"] <= medias["Residencial"]:
            print("⚠️  Nos seus dados, Comercial não está consumindo mais que Residencial "
                  "-- isso diverge do padrão real (EPE), vale investigar.")
        else:
            razao = medias["Comercial"] / medias["Residencial"]
            print(f"✅ Comercial consome {razao:.1f}x mais que Residencial nos seus dados "
                  f"(referência real: ~{CONSUMO_MEDIO_REFERENCIA_KWH['Comercial'] / CONSUMO_MEDIO_REFERENCIA_KWH['Residencial']:.1f}x)")


def treinar_e_comparar(pasta=None):
    if pasta is None:
        pasta = pasta_do_script()
    df_cliente, df_equip, df_catalogo = carregar_dados(pasta)
    df_cliente, df_equip = limpar_dados(df_cliente, df_equip)
    base = criar_base_energetica(df_cliente, df_equip, df_catalogo)
    base = classificar_perfil_energetico(base)
    verificar_coerencia_com_referencias_reais(base)

    print("Distribuição do perfil energético (%):")
    print((base["perfil_energetico"].value_counts(normalize=True) * 100).round(1))
    print()

    X = base[COLUNAS_CATEGORICAS + COLUNAS_NUMERICAS]
    y = base["perfil_energetico"]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    # StandardScaler nas numéricas: sem isso, a Regressão Logística fica em desvantagem
    # injusta na comparação com Random Forest/Árvore (que não precisam de normalização).
    preprocessador = ColumnTransformer(
        transformers=[
            ("categoria", OneHotEncoder(handle_unknown="ignore"), COLUNAS_CATEGORICAS),
            ("numerica", StandardScaler(), COLUNAS_NUMERICAS),
        ],
    )

    modelos = {
        "Random Forest": RandomForestClassifier(n_estimators=300, max_depth=8, random_state=42),
        "Árvore de Decisão": DecisionTreeClassifier(max_depth=6, random_state=42),
        "Regressão Logística": LogisticRegression(max_iter=2000, random_state=42),
    }

    resultados = {}
    pipelines_treinados = {}

    for nome, classificador in modelos.items():
        pipe = Pipeline(steps=[("preparador", preprocessador), ("modelo", classificador)])
        pipe.fit(X_train, y_train)
        y_pred = pipe.predict(X_test)

        resultados[nome] = {
            "acuracia": accuracy_score(y_test, y_pred),
            "f1_macro": f1_score(y_test, y_pred, average="macro"),
        }
        pipelines_treinados[nome] = pipe

        print(f"--- {nome} ---")
        print(classification_report(y_test, y_pred))

    tabela_resultados = pd.DataFrame(resultados).T.sort_values("f1_macro", ascending=False)
    print("--- COMPARAÇÃO FINAL (ordenado por F1 macro) ---")
    print(tabela_resultados)

    melhor_nome = tabela_resultados.index[0]
    melhor_pipe = pipelines_treinados[melhor_nome]

    joblib.dump(melhor_pipe, os.path.join(pasta, "modelo_energia.pkl"))
    print(f"\nModelo escolhido: {melhor_nome}")
    print(f"Salvo em: {os.path.join(pasta, 'modelo_energia.pkl')}")

    return melhor_pipe, tabela_resultados


if __name__ == "__main__":
    treinar_e_comparar()
