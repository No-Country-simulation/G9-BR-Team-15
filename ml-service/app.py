from typing import List, Optional

import joblib
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from prever import carregar_catalogo, prever

app = FastAPI(title="EnergiAI ml-service")

# carrega o modelo e o catálogo uma única vez, quando a API sobe (não a cada requisição)
modelo = joblib.load("modelo_energia.pkl")
df_catalogo = carregar_catalogo(".")


# --- formato de entrada -----------------------------------------------------

class Equipamento(BaseModel):
    tipo: str = Field(..., description="Nome do equipamento, precisa bater com o catálogo (ex: 'Geladeira Frost Free')")
    quantidade: int = Field(..., gt=0, description="Quantidade desse equipamento que o cliente tem")
    horas_uso_diario: float = Field(..., ge=0, le=24, description="Horas de uso por dia")
    dias_uso_mes: int = Field(..., ge=0, le=31, description="Dias de uso no mês")


class AnaliseEnergeticaRequest(BaseModel):
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


# --- formato de saída (bate com o endpoint obrigatório do edital) -----------

class AnaliseEnergeticaResponse(BaseModel):
    categoria: str
    probabilidade: float
    recomendacoes: List[str]
    consumo_estimado_kwh: float
    custo_estimado_mensal: float
    alerta_consumo_alto: bool


# --- endpoint obrigatório ----------------------------------------------------

@app.post("/analise-energetica", response_model=AnaliseEnergeticaResponse)
def analise_energetica(dados: AnaliseEnergeticaRequest):
    try:
        resultado = prever(
            tipo_pessoa=dados.tipo_pessoa,
            tipo_imovel=dados.tipo_imovel,
            equipamentos=[e.model_dump() for e in dados.equipamentos],
            modelo=modelo,
            df_catalogo=df_catalogo,
        )
    except ValueError as erro:
        # acontece se algum "tipo" de equipamento não existir no catálogo
        raise HTTPException(status_code=400, detail=str(erro))

    return resultado


@app.get("/")
def status():
    return {"status": "ok", "servico": "EnergiAI ml-service"}
