import os
import joblib
import pandas as pd

TARIFA_REFERENCIA_KWH = 0.75  # R$ por kWh, valor de referência do edital

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

TODAS_CATEGORIAS = ["Banheiro", "Climatizacao", "Cozinha", "Entretenimento",
                    "Iluminacao", "Industrial", "Limpeza", "TI"]

COLUNAS_CATEGORICAS = ["tipo_pessoa", "tipo_imovel", "faixa_uso_diario", "faixa_dias_uso"]
COLUNAS_NUMERICAS = [
    "quantidade_equipamentos", "diversidade_equipamentos",
    "qtd_banheiro", "qtd_climatizacao", "qtd_cozinha", "qtd_entretenimento",
    "qtd_iluminacao", "qtd_industrial", "qtd_limpeza", "qtd_ti",
]

# Dicas por categoria. Horário de ponta real da Light/ANEEL: 17h30-20h30 em dias úteis
# (fins de semana e feriados são sempre "fora de ponta" -- mais barato). Fonte: ANEEL, Tarifa Branca.
DICAS_POR_CATEGORIA = {
    "Climatizacao": "Ajuste o ar-condicionado para 23-24°C e evite deixá-lo ligado sem necessidade.",
    "Cozinha": "Concentre o uso de forno elétrico/airfryer fora do horário de ponta da Light (17h30-20h30).",
    "Banheiro": "Reduza o tempo de banho e evite usar o chuveiro elétrico na potência máxima, especialmente entre 17h30-20h30.",
    "Iluminacao": "Troque lâmpadas convencionais por LED, que consomem bem menos.",
    "TI": "Desligue computadores e servidores quando não estiverem em uso.",
    "Entretenimento": "Evite deixar TV e videogame em modo standby por longos períodos.",
    "Industrial": "Distribua o uso do maquinário ao longo do dia -- evite concentrar em horário de ponta (17h30-20h30), quando a tarifa é mais alta.",
    "Limpeza": "Programe o uso de equipamentos de limpeza fora do horário de ponta (17h30-20h30) ou para os fins de semana, quando a tarifa é sempre mais baixa.",
}

MENSAGEM_POR_PERFIL = {
    "Eficiente": "Parabéns! Seu consumo está dentro do esperado para o seu perfil. Continue assim.",
    "Moderado": "Seu consumo está na média do seu perfil, mas ainda há espaço pra economizar.",
    "Ineficiente": "Seu consumo está acima do esperado para o seu tipo de imóvel — dá pra economizar bastante.",
}


def carregar_catalogo(pasta=None):
    if pasta is None:
        pasta = os.path.dirname(os.path.abspath(__file__))
    df_catalogo = pd.read_csv(os.path.join(pasta, "tabela_equipamento_catalogo.csv"))
    df_catalogo["categoria"] = df_catalogo["tipo"].map(MAPA_CATEGORIA)
    return df_catalogo


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


def calcular_features_cliente(tipo_pessoa, tipo_imovel, equipamentos, df_catalogo):
    """
    equipamentos: lista de dicts, cada um com:
      {'tipo': 'Ar Condicionado Split', 'quantidade': 2, 'horas_uso_diario': 6, 'dias_uso_mes': 30}
    (o 'tipo' precisa bater com o nome exato de algum equipamento do catálogo)
    """
    df = pd.DataFrame(equipamentos)
    df = df.merge(df_catalogo[["tipo", "potencia_watts", "categoria"]], on="tipo", how="left")

    if df["potencia_watts"].isnull().any():
        faltando = df[df["potencia_watts"].isnull()]["tipo"].unique()
        raise ValueError(f"Equipamento(s) não encontrado(s) no catálogo: {list(faltando)}")

    df["consumo_kwh"] = (df["quantidade"] * df["horas_uso_diario"] * df["dias_uso_mes"] * df["potencia_watts"]) / 1000

    consumo_total_kwh = df["consumo_kwh"].sum()
    quantidade_equipamentos = df["quantidade"].sum()
    diversidade_equipamentos = df["tipo"].nunique()
    horas_media = df["horas_uso_diario"].mean()
    dias_media = df["dias_uso_mes"].mean()

    linha = {
        "tipo_pessoa": tipo_pessoa,
        "tipo_imovel": tipo_imovel,
        "faixa_uso_diario": faixa_horas(horas_media),
        "faixa_dias_uso": faixa_dias(dias_media),
        "quantidade_equipamentos": quantidade_equipamentos,
        "diversidade_equipamentos": diversidade_equipamentos,
    }

    qtd_por_categoria = df.groupby("categoria")["quantidade"].sum()
    for categoria in TODAS_CATEGORIAS:
        linha[f"qtd_{categoria.lower()}"] = qtd_por_categoria.get(categoria, 0)

    # categoria que mais pesa em kWh (usada só pra gerar a recomendação, não é feature do modelo)
    consumo_por_categoria = df.groupby("categoria")["consumo_kwh"].sum()
    categoria_dominante = consumo_por_categoria.idxmax() if len(consumo_por_categoria) else None

    return linha, consumo_total_kwh, categoria_dominante


def gerar_recomendacoes(perfil, categoria_dominante):
    recomendacoes = [MENSAGEM_POR_PERFIL[perfil]]
    if categoria_dominante in DICAS_POR_CATEGORIA:
        recomendacoes.append(
            f"Sua maior fonte de consumo é a categoria '{categoria_dominante}': {DICAS_POR_CATEGORIA[categoria_dominante]}"
        )
    return recomendacoes


def prever(tipo_pessoa, tipo_imovel, equipamentos, modelo, df_catalogo):
    linha, consumo_total_kwh, categoria_dominante = calcular_features_cliente(
        tipo_pessoa, tipo_imovel, equipamentos, df_catalogo
    )

    X_novo = pd.DataFrame([linha])[COLUNAS_CATEGORICAS + COLUNAS_NUMERICAS]

    categoria_prevista = modelo.predict(X_novo)[0]
    probabilidades = modelo.predict_proba(X_novo)[0]
    probabilidade = round(max(probabilidades), 2)

    return {
        "categoria": categoria_prevista,
        "probabilidade": probabilidade,
        "recomendacoes": gerar_recomendacoes(categoria_prevista, categoria_dominante),
        "consumo_estimado_kwh": round(consumo_total_kwh, 2),
        "custo_estimado_mensal": round(consumo_total_kwh * TARIFA_REFERENCIA_KWH, 2),
        "alerta_consumo_alto": categoria_prevista == "Ineficiente",  # recurso opcional do edital
    }


def simular_economia(consumo_atual_kwh, reducao_percentual):
    """Recurso opcional do edital: simulação de cenário de economia.
    Estima quanto o cliente economizaria (em kWh e em R$) se reduzisse o consumo em X%."""
    consumo_reduzido = consumo_atual_kwh * (1 - reducao_percentual / 100)
    economia_kwh = consumo_atual_kwh - consumo_reduzido
    return {
        "consumo_atual_kwh": round(consumo_atual_kwh, 2),
        "consumo_projetado_kwh": round(consumo_reduzido, 2),
        "economia_kwh": round(economia_kwh, 2),
        "economia_reais": round(economia_kwh * TARIFA_REFERENCIA_KWH, 2),
    }


def prever_em_lote(pasta=None):
    if pasta is None:
        pasta = os.path.dirname(os.path.abspath(__file__))
    """Recurso opcional do edital: processamento em lote via CSV.
    Roda a previsão pra TODOS os clientes das 3 tabelas de uma vez (útil pro dashboard
    e pra gerar um histórico de análises). Salva o resultado em previsoes.csv."""
    df_cliente = pd.read_csv(os.path.join(pasta, "tabela_cliente.csv"))
    df_equip = pd.read_csv(os.path.join(pasta, "tabela_cliente_equipamento.csv"))
    df_catalogo = carregar_catalogo(pasta)
    modelo = joblib.load(os.path.join(pasta, "modelo_energia.pkl"))

    df_equip = df_equip[df_equip["quantidade"] > 0].copy()  # mesma limpeza do treino
    if df_cliente["tipo_pessoa"].isnull().any():  # 50 clientes vêm com tipo_pessoa vazio
        df_cliente["tipo_pessoa"] = df_cliente["tipo_pessoa"].fillna(df_cliente["tipo_pessoa"].mode()[0])

    resultados = []
    for _, cliente in df_cliente.iterrows():
        equipamentos_cliente = df_equip[df_equip["id_cliente"] == cliente["id"]]
        if equipamentos_cliente.empty:
            continue

        equipamentos = equipamentos_cliente.merge(
            df_catalogo[["id", "tipo"]], left_on="id_equipamento", right_on="id"
        )[["tipo", "quantidade", "horas_uso_diario", "dias_uso_mes"]].to_dict("records")

        resultado = prever(cliente["tipo_pessoa"], cliente["tipo_imovel"], equipamentos, modelo, df_catalogo)
        resultado["id_cliente"] = cliente["id"]
        resultados.append(resultado)

    df_resultados = pd.DataFrame(resultados)
    df_resultados.to_csv(os.path.join(pasta, "previsoes.csv"), index=False)
    print(f"{len(df_resultados)} clientes processados. Resultado salvo em previsoes.csv")
    return df_resultados


if __name__ == "__main__":
    # --- 3 exemplos de utilização (requisito mínimo do edital) ---
    modelo = joblib.load("data-science/modelo_energia.pkl")
    df_catalogo = carregar_catalogo()

    exemplo_1 = dict(
        tipo_pessoa="PF", tipo_imovel="Residencial",
        equipamentos=[
            {"tipo": "Geladeira Frost Free", "quantidade": 1, "horas_uso_diario": 24, "dias_uso_mes": 30},
            {"tipo": "Chuveiro Elétrico", "quantidade": 1, "horas_uso_diario": 0.5, "dias_uso_mes": 30},
            {"tipo": "Televisão Smart", "quantidade": 1, "horas_uso_diario": 3, "dias_uso_mes": 30},
        ],
    )

    exemplo_2 = dict(
        tipo_pessoa="PJ", tipo_imovel="Comercial",
        equipamentos=[
            {"tipo": "Ar Condicionado Split", "quantidade": 4, "horas_uso_diario": 10, "dias_uso_mes": 26},
            {"tipo": "Iluminação Comercial (LEDs)", "quantidade": 20, "horas_uso_diario": 12, "dias_uso_mes": 26},
            {"tipo": "Computador Desktop", "quantidade": 8, "horas_uso_diario": 9, "dias_uso_mes": 26},
        ],
    )

    exemplo_3 = dict(
        tipo_pessoa="PJ", tipo_imovel="Industrial",
        equipamentos=[
            {"tipo": "Maquinário Industrial", "quantidade": 6, "horas_uso_diario": 16, "dias_uso_mes": 28},
            {"tipo": "Servidores / TI", "quantidade": 3, "horas_uso_diario": 24, "dias_uso_mes": 30},
        ],
    )

    for i, exemplo in enumerate([exemplo_1, exemplo_2, exemplo_3], start=1):
        resultado = prever(exemplo["tipo_pessoa"], exemplo["tipo_imovel"], exemplo["equipamentos"], modelo, df_catalogo)
        print(f"--- Exemplo {i} ({exemplo['tipo_imovel']}) ---")
        print(resultado)
        print()

    # --- simulação de economia (recurso opcional) ---
    print("--- Simulação de economia (exemplo 3, reduzindo 20% do consumo) ---")
    consumo_ex3 = prever(exemplo_3["tipo_pessoa"], exemplo_3["tipo_imovel"], exemplo_3["equipamentos"], modelo, df_catalogo)["consumo_estimado_kwh"]
    print(simular_economia(consumo_ex3, reducao_percentual=20))
    print()

    # --- processamento em lote (recurso opcional) ---
    print("--- Processamento em lote (todos os clientes das tabelas) ---")
    prever_em_lote()
