from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
import joblib

app = FastAPI(title="Lumen ML Service", version="1.0")

# Carrega o modelo na memória quando o contêiner sobe
# Certifique-se de que o arquivo .pkl esteja na mesma pasta ou mapeado no Docker
modelo_eficiencia = joblib.load("modelo_treinado.pkl")
modelo_eficiencia_v2 = joblib.load("modelo_treinado_v2.pkl")

#---------------------------------------------------------- classes usadas no endpoint obrigatorio
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

#------------------------------------------------------ classes utilizadas no endpoint principal
class Equipamento(BaseModel):
    tipo: str
    quantidade: int
    horas_uso_diario: float
    dias_uso_mes: int

class AnaliseRequest(BaseModel):
    tipo_pessoa: str
    tipo_imovel: str
    equipamentos: List[Equipamento]

class AnaliseResponse(BaseModel):
    categoria: str
    probabilidade: float
    recomendacoes: List[str]
    consumo_estimado_kwh: float
    custo_estimado_mensal: float
    alerta_consumo_alto: bool
#----------------------------------------------------------------- verificação de funcionamento
@app.get("/health")
def health_check():
    return {"status": "ok", "service": "ml-service"}

#------------------------------------------------------------------------ Endpoint obrigatório do hackthon
@app.post("/api/v1/teste/analise-energetica", response_model=TesteAnaliseResponse)
def analisar_eficiencia_teste(dados: TesteAnaliseRequest):
    resposta = modelo_eficiencia.prever(
        consumo_kwh = dados.consumo_kwh,
        uso_horario_pico = dados.uso_horario_pico,
        quantidade_equipamentos = dados.quantidade_equipamentos,
        tipo_imovel = dados.tipo_imovel,
        horas_alto_consumo = dados.horas_alto_consumo
    )
    return TesteAnaliseResponse(**resposta)

#----------------------------------------------------------------------- Endpoint personalisado baseado em banco de dados criado
@app.post("/api/v1/analise-energetica", response_model=AnaliseResponse)
def analisar_eficiencia(dados: AnaliseRequest):
    lista_equipamentos_dict = [equip.model_dump() for equip in dados.equipamentos]
    
    resposta = modelo_eficiencia_v2.prever(
        tipo_pessoa = dados.tipo_pessoa,
        tipo_imovel = dados.tipo_imovel,
        equipamentos = lista_equipamentos_dict
    )
    return AnaliseResponse(**resposta)