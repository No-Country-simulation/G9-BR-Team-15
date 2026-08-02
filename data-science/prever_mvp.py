
import joblib
import pandas as pd

from prever import MENSAGEM_POR_PERFIL, TARIFA_REFERENCIA_KWH  # reaproveita do modelo principal


def prever_mvp(consumo_kwh, uso_horario_pico, quantidade_equipamentos, tipo_imovel, horas_alto_consumo, modelo):
    """Recebe exatamente os 5 campos do endpoint obrigatório do edital e devolve
    categoria, probabilidade, recomendações e custo estimado mensal."""
    X_novo = pd.DataFrame([{
        "tipo_imovel": tipo_imovel,
        "consumo_kwh": consumo_kwh,
        "uso_horario_pico": uso_horario_pico,
        "quantidade_equipamentos": quantidade_equipamentos,
        "horas_alto_consumo": horas_alto_consumo,
    }])

    categoria_prevista = modelo.predict(X_novo)[0]
    probabilidade = round(max(modelo.predict_proba(X_novo)[0]), 2)

    recomendacoes = [MENSAGEM_POR_PERFIL[categoria_prevista]]
    if uso_horario_pico:
        recomendacoes.append(
            "Evite concentrar o uso de equipamentos no horário de ponta da Light (17h30-20h30)."
        )

    return {
        "categoria": categoria_prevista,
        "probabilidade": probabilidade,
        "recomendacoes": recomendacoes,
        "custo_estimado_mensal": round(consumo_kwh * TARIFA_REFERENCIA_KWH, 2),
    }


if __name__ == "__main__":
    # exemplo de utilização, igual ao formato de entrada do edital
    import os
    pasta = os.path.dirname(os.path.abspath(__file__))
    modelo = joblib.load(os.path.join(pasta, "modelo_mvp.pkl"))

    exemplo = prever_mvp(
        consumo_kwh=420,
        uso_horario_pico=True,
        quantidade_equipamentos=10,
        tipo_imovel="Residencial",
        horas_alto_consumo=8,
        modelo=modelo,
    )
    print(exemplo)
