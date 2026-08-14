from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


def test_mvp_endpoint():
    payload = {
        "consumo_kwh": 420,
        "uso_horario_pico": True,
        "quantidade_equipamentos": 10,
        "tipo_imovel": "Residencial",
        "horas_alto_consumo": 8,
    }

    response = client.post("/api/v1/teste/analise-energetica", json=payload)
    assert response.status_code == 200
    body = response.json()
    assert "categoria" in body
    assert "probabilidade" in body
    assert "recomendacoes" in body
    assert "custo_estimado_mensal" in body
