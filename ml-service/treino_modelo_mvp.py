"""
EnergiAI - Segundo modelo (Modelo MVP), exclusivo para o endpoint OBRIGATÓRIO do edital.

Este NÃO é o modelo principal do projeto. O modelo principal (mais completo, usado pelo
dashboard e pela análise de Ciência de Dados) continua sendo treino_modelo.py + prever.py,
sem nenhuma alteração.

Este arquivo existe só porque o edital especifica um formato de entrada fixo e mais simples
(consumo_kwh, uso_horario_pico, quantidade_equipamentos, tipo_imovel, horas_alto_consumo).

Não duplica a lógica de construção da base: reaproveita treino_modelo.py (carregar_dados,
limpar_dados, criar_base_energetica, classificar_perfil_energetico) e só deriva os 5 campos
simples a partir da MESMA base completa e do MESMO rótulo já calculados -- garante que os
dois modelos concordam sobre o que é "Eficiente/Moderado/Ineficiente", só que um usa mais
informação que o outro pra decidir.
"""

import os

import joblib
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, classification_report, f1_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.tree import DecisionTreeClassifier

import treino_modelo as tm

COLUNAS_CATEGORICAS_MVP = ["tipo_imovel"]
COLUNAS_NUMERICAS_MVP = ["consumo_kwh", "uso_horario_pico", "quantidade_equipamentos", "horas_alto_consumo"]


def criar_base_mvp(pasta=None):
    """Deriva os 5 campos do edital a partir da base completa do modelo principal.

    - consumo_kwh          <- consumo_total_kwh (arredondado pra int, como o edital pede)
    - uso_horario_pico      <- True quando a faixa de uso diário do cliente é 'Alto'
    - quantidade_equipamentos <- mesma coluna já usada no modelo principal
    - tipo_imovel           <- mesma coluna já usada no modelo principal
    - horas_alto_consumo    <- média de horas de uso diário (arredondada pra int)
    """
    if pasta is None:
        pasta = tm.pasta_do_script()
    df_cliente, df_equip, df_catalogo = tm.carregar_dados(pasta)
    df_cliente, df_equip = tm.limpar_dados(df_cliente, df_equip)
    base = tm.criar_base_energetica(df_cliente, df_equip, df_catalogo)
    base = tm.classificar_perfil_energetico(base)

    base_mvp = pd.DataFrame({
        "consumo_kwh": base["consumo_total_kwh"].round().astype(int),
        "uso_horario_pico": base["faixa_uso_diario"] == "Alto",
        "quantidade_equipamentos": base["quantidade_equipamentos"].astype(int),
        "tipo_imovel": base["tipo_imovel"],
        "horas_alto_consumo": base["horas_uso_diario_media"].round().astype(int),
        "perfil_energetico": base["perfil_energetico"],
    })
    return base_mvp


def treinar_modelo_mvp(pasta=None):
    if pasta is None:
        pasta = tm.pasta_do_script()
    base_mvp = criar_base_mvp(pasta)

    X = base_mvp[COLUNAS_CATEGORICAS_MVP + COLUNAS_NUMERICAS_MVP]
    y = base_mvp["perfil_energetico"]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    preprocessador = ColumnTransformer(transformers=[
        ("categoria", OneHotEncoder(handle_unknown="ignore"), COLUNAS_CATEGORICAS_MVP),
        ("numerica", StandardScaler(), COLUNAS_NUMERICAS_MVP),
    ])

    # mesmos 3 algoritmos comparados no modelo principal, pelo mesmo motivo: o edital
    # pede "treinamento de modelos supervisionados" (no plural) e comparação justificada.
    modelos = {
        "Random Forest": RandomForestClassifier(n_estimators=200, max_depth=6, random_state=42),
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

        print(f"--- Modelo MVP: {nome} ---")
        print(classification_report(y_test, y_pred))

    tabela_resultados = pd.DataFrame(resultados).T.sort_values("f1_macro", ascending=False)
    print("--- COMPARAÇÃO FINAL (Modelo MVP, ordenado por F1 macro) ---")
    print(tabela_resultados)
    print()
    print("IMPORTANTE: acurácias tão altas aqui são esperadas, não um bom sinal --")
    print("consumo_kwh + tipo_imovel já contêm quase toda a informação do rótulo. Ver README.")

    melhor_nome = tabela_resultados.index[0]
    melhor_pipe = pipelines_treinados[melhor_nome]

    joblib.dump(melhor_pipe, os.path.join(pasta, "modelo_mvp.pkl"))
    print(f"\nModelo MVP escolhido: {melhor_nome}")
    print(f"Salvo em: {os.path.join(pasta, 'modelo_mvp.pkl')}")

    return melhor_pipe, tabela_resultados


if __name__ == "__main__":
    treinar_modelo_mvp()
