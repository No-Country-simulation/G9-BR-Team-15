import sys
from pathlib import Path
from typing import List

import joblib
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

BASE_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = BASE_DIR.parent
DATA_DIR = BASE_DIR / "data"
DATA_SCIENCE_DIR = BASE_DIR / "data-science"

if not DATA_DIR.exists():
    DATA_DIR = None

if not DATA_SCIENCE_DIR.exists():
    DATA_SCIENCE_DIR = PROJECT_ROOT / "data-science"

if DATA_SCIENCE_DIR.exists() and str(DATA_SCIENCE_DIR) not in sys.path:
    sys.path.insert(0, str(DATA_SCIENCE_DIR))

from prever import carregar_catalogo, prever
from prever_mvp import prever_mvp

app = FastAPI(title="LUMEN ML Service", version="1.0")


def _load_model(model_name: str):
    candidates = [
        BASE_DIR / model_name,
        BASE_DIR / "data-science" / model_name,
        PROJECT_ROOT / "data-science" / model_name,
        PROJECT_ROOT / model_name,
    ]
    for path in candidates:
        if path.exists():
            return joblib.load(path)
    raise FileNotFoundError(f"Modelo não encontrado: {model_name}")


modelo_principal = _load_model("modelo_energia.pkl")
df_catalogo = carregar_catalogo(str(DATA_DIR or BASE_DIR))
modelo_mvp = _load_model("modelo_mvp.pkl")


class TesteAnaliseRequest(BaseModel):
    consumo_kwh: int
    uso_horario_pico: bool
    quantidade_equipamentos: int
    tipo_imovel: str
    horas_alto_consumo: int


class TesteAnaliseResponse(BaseModel):
    categoria: str
    probabilidade: float
    recomendacoes: List[str]
    custo_estimado_mensal: float


class Equipamento(BaseModel):
    tipo: str = Field(..., description="Nome do equipamento conforme o catálogo")
    quantidade: int = Field(..., gt=0)
    horas_uso_diario: float = Field(..., ge=0, le=24)
    dias_uso_mes: int = Field(..., ge=0, le=31)


class AnaliseRequest(BaseModel):
    tipo_pessoa: str = Field(..., description="PF ou PJ")
    tipo_imovel: str = Field(..., description="Residencial, Comercial ou Industrial")
    equipamentos: List[Equipamento]


class AnaliseResponse(BaseModel):
    categoria: str
    probabilidade: float
    recomendacoes: List[str]
    consumo_estimado_kwh: float
    custo_estimado_mensal: float
    alerta_consumo_alto: bool


@app.get("/health")
def health_check():
    return {"status": "ok"}


@app.get("/")
def root():
    return {"status": "ok", "service": "LUMEN ML Service", "version": "1.0"}


@app.post("/api/v1/teste/analise-energetica", response_model=TesteAnaliseResponse)
@app.post("/teste-analise-energetica", response_model=TesteAnaliseResponse)
def analisar_eficiencia_teste(dados: TesteAnaliseRequest):
    resposta = prever_mvp(
        consumo_kwh=dados.consumo_kwh,
        uso_horario_pico=dados.uso_horario_pico,
        quantidade_equipamentos=dados.quantidade_equipamentos,
        tipo_imovel=dados.tipo_imovel,
        horas_alto_consumo=dados.horas_alto_consumo,
        modelo=modelo_mvp,
    )
    return TesteAnaliseResponse(**resposta)


@app.post("/api/v1/analise-energetica", response_model=AnaliseResponse)
@app.post("/analise-energetica", response_model=AnaliseResponse)
def analisar_eficiencia(dados: AnaliseRequest):
    lista_equipamentos_dict = [equip.model_dump() for equip in dados.equipamentos]
    try:
        resposta = prever(
            tipo_pessoa=dados.tipo_pessoa,
            tipo_imovel=dados.tipo_imovel,
            equipamentos=lista_equipamentos_dict,
            modelo=modelo_principal,
            df_catalogo=df_catalogo,
        )
    except ValueError as erro:
        raise HTTPException(status_code=400, detail=str(erro)) from erro

    return AnaliseResponse(**resposta)