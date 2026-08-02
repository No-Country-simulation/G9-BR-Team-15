"""
EnergiAI - ml-service
API FastAPI com DOIS endpoints separados, dois modelos independentes:

- POST /analise-energetica            -> Modelo MVP (prever_mvp.py). Obrigatório,
                                          formato fixo do edital, não pode ser alterado.
- POST /analise-energetica-detalhada  -> Modelo Principal (prever.py), recebe lista de
                                          equipamentos. Mais completo, usado pelo dashboard,
                                          pela análise completa e pelo processamento em lote.

Como rodar:
1. Coloque este arquivo na pasta ml-service/, junto com:
   modelo_energia.pkl, modelo_mvp.pkl, prever.py, prever_mvp.py, tabela_equipamento_catalogo.csv
2. pip install fastapi uvicorn pandas scikit-learn joblib
3. uvicorn app:app --reload --port 8000
4. Documentação automática em http://localhost:8000/docs
"""

from typing import List

import joblib
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from prever import carregar_catalogo, prever
from prever_mvp import prever_mvp

app = FastAPI(title="EnergiAI ml-service")

# carrega os dois modelos uma única vez, quando a API sobe (não recarrega a cada requisição)
modelo_principal = joblib.load("modelo_energia.pkl")
df_catalogo = carregar_catalogo(".")

modelo_mvp = joblib.load("modelo_mvp.pkl")


# =============================================================================
# ENDPOINT OBRIGATÓRIO — Modelo MVP (formato fixo do edital, não pode mudar)
# Acurácia alta (98,5%) é esperada e não representa aprendizado real -- ver README.
# =============================================================================

class AnaliseEnergeticaRequest(BaseModel):
    consumo_kwh: int
    uso_horario_pico: bool
    quantidade_equipamentos: int
    tipo_imovel: str
    horas_alto_consumo: int

    class Config:
        json_schema_extra = {
            "example": {
                "consumo_kwh": 420,
                "uso_horario_pico": True,
                "quantidade_equipamentos": 10,
                "tipo_imovel": "Casa",
                "horas_alto_consumo": 8,
            }
        }


class AnaliseEnergeticaResponse(BaseModel):
    categoria: str
    probabilidade: float
    recomendacoes: List[str]
    custo_estimado_mensal: float


@app.post("/analise-energetica", response_model=AnaliseEnergeticaResponse)
def analise_energetica(dados: AnaliseEnergeticaRequest):
    """Endpoint obrigatório do edital (Modelo MVP). Formato de entrada e saída fixos, não alterar."""
    resultado = prever_mvp(
        consumo_kwh=dados.consumo_kwh,
        uso_horario_pico=dados.uso_horario_pico,
        quantidade_equipamentos=dados.quantidade_equipamentos,
        tipo_imovel=dados.tipo_imovel,
        horas_alto_consumo=dados.horas_alto_consumo,
        modelo=modelo_mvp,
    )
    return resultado


# =============================================================================
# ENDPOINT DETALHADO — Modelo Principal (lista de equipamentos, mais rico)
# Usado pelo dashboard, pela análise completa e pelo processamento em lote.
# =============================================================================

class Equipamento(BaseModel):
    tipo: str = Field(..., description="Nome do equipamento, precisa bater com o catálogo (ex: 'Geladeira Frost Free')")
    quantidade: int = Field(..., gt=0, description="Quantidade desse equipamento que o cliente tem")
    horas_uso_diario: float = Field(..., ge=0, le=24, description="Horas de uso por dia")
    dias_uso_mes: int = Field(..., ge=0, le=31, description="Dias de uso no mês")


class AnaliseEnergeticaDetalhadaRequest(BaseModel):
    tipo_pessoa: str = Field(..., description="'PF' ou 'PJ'")
    tipo_imovel: str = Field(..., description="'Residencial', 'Comercial' ou 'Industrial'")
    equipamentos: List[Equipamento]

    class Config:
        json_schema_extra = {
            "example": {
                "tipo_pessoa": "PF",
                "tipo_imovel": "Residencial",
                "equipamentos": [
                    {"tipo": "Geladeira Frost Free", "quantidade": 1, "horas_uso_diario": 24, "dias_uso_mes": 30},
                    {"tipo": "Chuveiro Elétrico", "quantidade": 1, "horas_uso_diario": 0.5, "dias_uso_mes": 30},
                ],
            }
        }


class AnaliseEnergeticaDetalhadaResponse(BaseModel):
    categoria: str
    probabilidade: float
    recomendacoes: List[str]
    consumo_estimado_kwh: float
    custo_estimado_mensal: float
    alerta_consumo_alto: bool


@app.post("/analise-energetica-detalhada", response_model=AnaliseEnergeticaDetalhadaResponse)
def analise_energetica_detalhada(dados: AnaliseEnergeticaDetalhadaRequest):
    """Modelo Principal: recebe a lista de equipamentos do cliente. Mais preciso,
    usado pelo dashboard e pelo restante do sistema."""
    try:
        resultado = prever(
            tipo_pessoa=dados.tipo_pessoa,
            tipo_imovel=dados.tipo_imovel,
            equipamentos=[e.model_dump() for e in dados.equipamentos],
            modelo=modelo_principal,
            df_catalogo=df_catalogo,
        )
    except ValueError as erro:
        # acontece se algum "tipo" de equipamento não existir no catálogo
        raise HTTPException(status_code=400, detail=str(erro))

    return resultado


@app.get("/")
def status():
    return {"status": "ok", "servico": "EnergiAI ml-service"}
