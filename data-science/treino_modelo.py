import os
import joblib
import pandas as pd



from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, f1_score, classification_report
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder
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


def carregar_dados(pasta="data-science"):

    df_cliente = pd.read_csv(os.path.join(pasta, "tabela_cliente.csv"))

    df_equip = pd.read_csv(os.path.join(pasta, "tabela_cliente_equipamento.csv"))

    df_catalogo = pd.read_csv(os.path.join(pasta, "tabela_equipamento_catalogo.csv"))

    return df_cliente, df_equip, df_catalogo


def limpar_dados(df_equip):


    return df_equip[df_equip["quantidade"] > 0].copy()


def criar_base_energetica(df_cliente, df_equip, df_catalogo):

    df_catalogo = df_catalogo.copy()

    df_catalogo["categoria"] = df_catalogo["tipo"].map(MAPA_CATEGORIA)



    df = df_equip.merge(df_catalogo, left_on="id_equipamento", right_on="id", how="left", suffixes=("", "_equip"))

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

def treinar_e_comparar(pasta="data-science"):

    df_cliente, df_equip, df_catalogo = carregar_dados(pasta)

    df_equip = limpar_dados(df_equip)

    base = criar_base_energetica(df_cliente, df_equip, df_catalogo)

    base = classificar_perfil_energetico(base)



    print("Distribuição do perfil energético (%):")

    print((base["perfil_energetico"].value_counts(normalize=True) * 100).round(1))

    print()

    X = base[COLUNAS_CATEGORICAS + COLUNAS_NUMERICAS]

    y = base["perfil_energetico"]



    X_train, X_test, y_train, y_test = train_test_split(

        X, y, test_size=0.2, random_state=42, stratify=y

    )

    preprocessador = ColumnTransformer(

        transformers=[("categoria", OneHotEncoder(handle_unknown="ignore"), COLUNAS_CATEGORICAS)],

        remainder="passthrough",

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

